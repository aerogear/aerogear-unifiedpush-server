'use strict';

(function() {
  var app = angular.module('upsConsole', [
    'upsConsole.services',
    'ngResource',
    'ngRoute',
    'ui.bootstrap',
    'ups.directives',
    'patternfly.notification',
    'hljs'
  ]);

  /**
   * Snippet extracted from Keycloak examples
   */
  var auth = {};

  angular.element(document).ready(function () {
    var keycloakAuth = new Keycloak('keycloak.json');
    auth.loggedIn = false;

    keycloakAuth.init({ onLoad: 'login-required' }).success(function () {
      auth.loggedIn = true;
      auth.authz = keycloakAuth;
      auth.logoutUrl = keycloakAuth.authServerUrl + '/realms/aerogear/tokens/logout?redirect_uri=' + window.location.href;
      app.factory('Auth', function () {
        return auth;
      });
      angular.bootstrap(document, ['upsConsole']);
    }).error(function () {
      window.location.reload();
    });

  });

  app.factory('Auth', function () {
    return auth;
  });

  app.config(function ($routeProvider) {

    $routeProvider
      .when('/applications', {
        templateUrl: 'views/applications.html',
        controller: 'ApplicationController',
        resolve: {
          applications: function (pushApplication) {
            return pushApplication.query().$promise;
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
        controller: 'DetailController',
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
        controller: 'InstallationController',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: '$ variant.name ? variant.name : "Registering Installations"'
        }
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/example/:applicationId/:variantType', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/compose', {
        templateUrl: 'views/compose-app.html',
        controller: 'PreComposeController',
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
        controller: 'ComposeController',
        section: 'compose',
        crumb: {
          parent: 'app-detail',
          label: 'Send Push'
        }
      })
      .when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardController',
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
        controller: 'ActivityController',
        section: 'dashboard',
        crumb: {
          id: 'activity',
          parent: 'dash',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/activity/:applicationId/:variantId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
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

        if (config.url === 'rest/sender') {
          return config;
        }

        if (Auth.authz && Auth.authz.token) {
          Auth.authz.updateToken(5).success(function () {
            config.headers = config.headers || {};
            config.headers.Authorization = 'Bearer ' + Auth.authz.token;

            deferred.resolve(config);
          }).error(function () {
            //deferred.reject('Failed to refresh token');
            window.location.reload();
          });
        }
        return deferred.promise;
      }
    };
  });

  app.config(function ($httpProvider) {
    //$httpProvider.responseInterceptors.push('errorInterceptor');
    $httpProvider.interceptors.push('authInterceptor');

  });

  //Extracted from Keycloak.js props to KC team
  app.factory('errorInterceptor', function($q) {
      return function(promise) {
          return promise.then(function(response) {
              return response;
            }, function(response) {
                if (response.status === 401) {
                  console.log('session timeout?');
                  auth.loggedIn = false;
                  auth.authz = null;
                  window.location = auth.logoutUrl;
                } else if (response.status === 403) {
                  console.error('Forbidden');
                } else if (response.status === 404) {
                  console.error('Not found');
                } else if (response.status) {
                  if (response.data && response.data.errorMessage) {
                    console.log(response.data.errorMessage);
                  } else {
                    console.error('An unexpected server error has occurred');
                  }
                }
                return $q.reject(response);
              });
        };
    });

})();
