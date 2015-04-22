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
  .controller('AppController',
      function ( $rootScope, Auth, $http, $log, appConfig ) {

    var self = this;

    this.config = appConfig;

    /**
     * View loading status
     */
    this.isViewLoading = false;

    //Retrieve the current logged in username
    this.username = Auth.keycloak.idToken.preferred_username;

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

    $rootScope.$on('$routeChangeStart', function () {
      self.isViewLoading = true;
    });
    $rootScope.$on('$routeChangeSuccess', function (event, routeData) {
      self.isViewLoading = false;
    });
  });
