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
  .controller('AppController', function ( $rootScope, $scope, Auth, $http, $interval, $timeout, $log, appConfig, dashboardEndpoint ) {

    var self = this;

    this.config = appConfig;
    this.warnings = [];

    //Retrieve the current logged in username
    function getUsername() {
      return Auth.keycloak.idTokenParsed.preferred_username;
    }
    this.username = getUsername();
    $scope.$watch(getUsername, function( newValue ) {
      self.username = newValue;
    });

    this.logout = function() {
      Auth.logout();
    };

    this.goToAccountManagement = function() {
      window.location = Auth.keycloak.authServerUrl + '/realms/aerogear/account?referrer=unified-push-server-js';
    };

    this.havePendingRequests = function() {
      return $http.pendingRequests.some(function(config) {
        if (config.method !== 'GET') {
          return true;
        }
      });
    };

    // load warnings and update them periodically and when notification is sent
    function updateWarnings() {
      dashboardEndpoint.warnings().then(function( warnings ) {
        self.warnings = warnings;
      });
    }
    updateWarnings();
    $interval(updateWarnings, 30000);
    $scope.$on('upsNotificationSent', function() {
      $timeout(updateWarnings, 1500);
      $timeout(updateWarnings, 5000);
      $timeout(updateWarnings, 10000);
    });
  });
