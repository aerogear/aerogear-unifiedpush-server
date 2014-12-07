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

angular.module('upsConsole').controller('ExampleController',
  function($rootScope, $routeParams, variants, pushApplication, ContextProvider) {

  var $scope = this;
  /*
   * INITIALIZATION
   */
  var params = {
    appId: $routeParams.applicationId,
    variantType: $routeParams.variantType,
    variantId: $routeParams.variantId
  };
  $scope.variantType = $routeParams.variantType;
  console.log($scope.variantType);
  $scope.applicationId = $routeParams.applicationId;
  $scope.currentLocation = ContextProvider.contextPath();

  if (typeof $routeParams.variantId !== 'undefined') {
    variants.get(params, function (variant) {
      $scope.variant = variant;
    });
  } else {
    pushApplication.get(params, function (application) {
      $scope.application = application;
    });
  }

  var activeTab = $routeParams.variantType;
  var tabs = {};

  function Tab(tabName) {
    this.name = tabName;
  }
  Object.defineProperty(Tab.prototype, 'active', {
    get: function() {
      return activeTab === this.name;
    },
    set: function(active) {
      if (active) {
        activeTab = this.name;
      }
    }
  });

  $scope.tab = function (tabName) {
    if (!tabs[tabName]) {
      tabs[tabName] = new Tab(tabName);
    }
    return tabs[tabName];
  };

  $scope.cordovaVariantType = function(variant) {
    switch (variant.type) {
    case 'windows_mpns':
      return 'windows';
    default:
      return variant.type;
    }
  };

  $scope.projectNumber = function(variant) {
    if(variant) {
      return $scope.variantType === 'android' ? ('senderID: "' + variant.projectNumber +'",') : '';
    }
  };

  /**
   *
   * Used for Cordova Push Config file snippet (adds the quotes for senderID)
   */
  $scope.projectNumberJson = function(variant) {
    if(variant) {
      return $scope.variantType === 'android' ? ('"senderID": "' + variant.projectNumber +'",') : '';
    }
  };

  $scope.simplePushUrl = function() {
    var parser = document.createElement('a');
    parser.href = ContextProvider.contextPath();

    return parser.protocol + '//' + parser.hostname + ':8443/simplepush';
  };
});
