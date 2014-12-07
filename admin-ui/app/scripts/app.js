'use strict';

/*jshint unused: false*/
(function() {

  var app = angular.module('upsConsole', [
    'upsConsole.services',
    'ngResource',
    'ngRoute',
    'ngAnimate',
    'ui.bootstrap',
    'ups.directives',
    'patternfly.notification',
    'patternfly.autofocus',
    'hljs',
    'ngIdle'
  ]);

  /**
   * Snippet extracted from Keycloak examples
   */
  var auth = {};

  angular.element(document).ready(function () {
    var keycloak = new Keycloak('config/admin-ui-keycloak.json');
    auth.loggedIn = false;

    keycloak.init({ onLoad: 'login-required' }).success(function () {
      auth.loggedIn = true;
      auth.keycloak = keycloak;
      auth.logout = function() {
        auth.loggedIn = false;
        auth.keycloak = null;
        window.location = keycloak.authServerUrl + '/realms/aerogear/tokens/logout?redirect_uri=' + window.location.href;
      };
      app.factory('Auth', function () {
        return auth;
      });
      angular.bootstrap(document, ['upsConsole']);
    }).error(function () {
      window.location.reload();
    });

  });

  app.run(function($rootScope) {
    // allow to retrieve $rootScope in views (for claritication of access scope)
    $rootScope.$rootScope = $rootScope;
  });

  app.factory('Auth', function () {
    return auth;
  });

  var appConfig = {
    logDebugEnabled: false,
    idleDuration: 300,
    idleWarningDuration : 30,
    keepaliveInterval: 5
  };

  app.provider('appConfig', function () {
    return {
      set: function (settings) {
        // allow to override configuration (e.g. in tests)
        angular.extend(appConfig, settings);
      },
      $get: function () {
        // default configuration
        return appConfig;
      }
    };
  });

  app.config(function ($routeProvider) {
    $routeProvider
      .when('/applications', {
        templateUrl: 'views/applications.html',
        controller: 'ApplicationController as appCtrl',
        resolve: {
          applications: function (pushApplication) {
            return pushApplication.fetch(1);
          }
        },
        section: 'applications',
        crumb: {
          id: 'apps',
          label: 'Applications'
        }
      })
      .when('/detail/:applicationId', {
        templateUrl: 'views/detail.html',
        controller: 'DetailController as detailCtrl',
        resolve: {
          application: function ($route, pushApplication) {
            return pushApplication.get({appId: $route.current.params.applicationId}).$promise;
          },
          counts: function ($route, pushApplication) {
            return pushApplication.count({appId: $route.current.params.applicationId}).$promise;
          }
        },
        section: 'applications',
        crumb: {
          id: 'app-detail',
          parent: 'apps',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/:applicationId/installations/:variantId', {
        templateUrl: 'views/installation.html',
        controller: 'InstallationController as installationCtrl',
        resolve: {
          data: function ($route, installations) {
            return installations.fetchInstallations($route.current.params.variantId, 1);
          }
        },
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: '$ variant.name ? variant.name : "Registering Installations"'
        }
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController as exampleCtrl',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/example/:applicationId/:variantType', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController as exampleCtrl',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/compose', {
        templateUrl: 'views/compose-app.html',
        controller: 'PreComposeController as preComposeCtrl',
        resolve: {
          applications: function (pushApplication) {
            return pushApplication.query({}).$promise;
          }
        },
        section: 'compose',
        crumb: {
          label: 'Send Push'
        }
      })
      .when('/compose/:applicationId', {
        templateUrl: 'views/compose.html',
        controller: 'ComposeController as composeCtrl',
        section: 'compose',
        crumb: {
          parent: 'app-detail',
          label: 'Send Push'
        }
      })
      .when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardController as dashboardCtrl',
        resolve: {
          totals: function (dashboard) {
            return dashboard.totals({}).$promise;
          },
          warnings: function (dashboard) {
            return dashboard.warnings({}).$promise;
          },
          topThree: function (dashboard) {
            return dashboard.topThree({}).$promise;
          }
        },
        section: 'dashboard',
        crumb: {
          id: 'dash',
          label: 'Dashboard'
        }
      })
      .when('/activity/:applicationId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController as activityCtrl',
        resolve: {
          data: function ($route, $q, pushApplication, metrics) {
            var applicationMetricsPromise = metrics.fetchApplicationMetrics($route.current.params.applicationId, 1);
            var applicationDetailPromise = pushApplication.get({appId: $route.current.params.applicationId}).$promise
              .then(function(application) {
                return { application: application };
              });
            return $q.all([applicationMetricsPromise, applicationDetailPromise])
              .then(function( data ) {
                return angular.extend( data[0], data[1] );
              });
          }
        },
        section: 'dashboard',
        crumb: {
          id: 'activity',
          parent: 'dash',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/activity/:applicationId/:variantId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController as activityCtrl',
        resolve: {
          data: function ($route, $q, metrics, pushApplication) {
            var variantMetricsPromise = metrics.fetchVariantMetrics($route.current.params.variantId, 1);
            var applicationDetailPromise = pushApplication.get({appId: $route.current.params.applicationId}).$promise
              .then(function(application) {
                // determine variant from its ID
                function findVariant() {
                  angular.forEach(application.variants, function (variant) {
                    if (variant.variantID === $route.current.params.variantId) {
                      return variant;
                    }
                  });
                }
                return {
                  application: application,
                  variant: findVariant()
                };
              });
            return $q.all([variantMetricsPromise, applicationDetailPromise])
              .then(function( data ) {
                return angular.extend( data[0], data[1] );
              });
          }
        },
        section: 'dashboard',
        crumb: {
          parent: 'activity',
          label: '$ variant.name ? variant.name : "Current variant"'
        }
      })
      .otherwise({
        redirectTo: '/dashboard'
      });
  });

  app.factory('authInterceptor', function ($q, Auth) {
    return {
      request: function (config) {
        var deferred = $q.defer();

        if (config.url === 'rest/sender' || config.url === 'rest/registry/device/importer') {
          return config;
        }

        if (Auth.keycloak && Auth.keycloak.token) {
          Auth.keycloak.updateToken(5).success(function () {
            config.headers = config.headers || {};
            config.headers.Authorization = 'Bearer ' + Auth.keycloak.token;

            deferred.resolve(config);
          }).error(function () {
            window.location.reload();
          });
        }
        return deferred.promise;
      }
    };
  });

  app.config(function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
  });

  app.config(function ($logProvider, appConfigProvider) {
    var appConfig = appConfigProvider.$get();
    $logProvider.debugEnabled( appConfig.logDebugEnabled );
  });


})();
