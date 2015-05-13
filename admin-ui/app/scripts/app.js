'use strict';

/*jshint unused: false*/
(function() {

  var app = angular.module('upsConsole', [
    'ngResource',
    'ngNewRouter',
    'ngAnimate',
    'ngIdle',
    'ui.bootstrap',
    'patternfly.notification',
    'patternfly.select',
    'ngClipboard'
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
    // allow to retrieve $rootScope in views (for clarification of access scope)
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

  app.factory('docsLinks', function( $http ) {
    var result = {};
    $http.get('docs-links.json')
      .then(function( response ) {
        angular.extend( result, response.data );
      });
    return result;
  });

  app.config(function(ngClipProvider) {
    ngClipProvider.setPath('img/ZeroClipboard.swf');
  });

})();
