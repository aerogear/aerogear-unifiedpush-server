/*
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

angular.module('upsConsole')
  .controller('RootController', function ($rootScope, $scope, Auth, $http, $keepalive, $idle, $log, appConfig) {

    $scope.appConfig = appConfig;

    /**
     * View loading status
     */
    $rootScope.isViewLoading = false;
    //Retrieve the current logged in username
    $rootScope.username = Auth.keycloak.idToken.preferred_username;

    $rootScope.accountManagement = function() {
      window.location = Auth.keycloak.authServerUrl + '/realms/aerogear/account?referrer=unified-push-server-js';
    };

    $rootScope.logout = function() {
      Auth.logout();
    };

    $rootScope.isProcessingData = function() {
      return $http.pendingRequests.some(function(config) {
        if (config.method !== 'GET') {
          console.log(config);
          return true;
        }
      });
    };

    $rootScope.$on('$routeChangeStart', function () {
      $rootScope.isViewLoading = true;
    });
    $rootScope.$on('$routeChangeSuccess', function (event, routeData) {
      $rootScope.isViewLoading = false;
      if (routeData.$$route && routeData.$$route.section) {
        $rootScope.section = routeData.$$route.section;
      }
    });

    /**
     * idle service, keepalive, auth token refresh
     */
    $idle.watch();
    $scope.idleCountdown = appConfig.idleWarningDuration + 1;
    $rootScope.$on('$keepalive', function() {
      Auth.keycloak.updateToken(45).success(function(refreshed) {
        if (refreshed) {
          $log.debug('token was successfully refreshed');
        } else {
          $log.debug('token is still valid');
        }
      }).error(function() {
        $log.debug('failed to refresh the token, or the session has expired');
      });
    });

    $rootScope.$on('$idleStart', function() {
      $log.debug('idleStart');

    });
    $rootScope.$on('$idleWarn', function() {
      $log.debug('idleWarn');
      $scope.idleCountdown = $scope.idleCountdown - 1;
    });
    $rootScope.$on('$idleEnd', function() {
      $log.debug('idleEnd');
      $scope.idleCountdown = appConfig.idleWarningDuration + 1;

    });
    $rootScope.$on('$idleTimeout', function() {
      $log.debug('idleTimeout');
      Auth.logout();
    });
  })

  .config( function( $keepaliveProvider, $idleProvider, appConfigProvider ) {
    var appConfig = appConfigProvider.$get();
    $idleProvider.idleDuration( appConfig.idleDuration );
    $idleProvider.warningDuration( appConfig.idleWarningDuration );
    $keepaliveProvider.interval( appConfig.keepaliveInterval );
  });
