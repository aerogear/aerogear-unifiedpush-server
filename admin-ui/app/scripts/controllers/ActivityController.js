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

angular.module('upsConsole').controller('ActivityController',
  function ($rootScope, $routeParams, $modal, metricsEndpoint, applicationsEndpoint, breadcrumbs, data) {
    var $scope = this;

    $scope.applicationId = $routeParams.applicationId;
    $scope.currentPage = 1;
    $scope.application = data.application;
    $scope.variant = data.variant;

    breadcrumbs.generateBreadcrumbs();

    function onDetailsPage() {
      return typeof $routeParams.variantId !== 'undefined';
    }

    function applyVariantMetricsData( data ) {
      $scope.totalItems = data.totalItems;
      $scope.pushMetrics = data.pushMetrics;
    }

    function applyApplicationMetricsData( data ) {
      $scope.totalItems = data.totalItems;
      $scope.pushMetrics = data.pushMetrics;
    }

    if (onDetailsPage()) {
      applyVariantMetricsData(data);
    } else {
      applyApplicationMetricsData(data);
    }

    $scope.pageChanged = function () {
      $rootScope.isViewLoading = true;
      if (onDetailsPage()) {
        metricsEndpoint.fetchVariantMetrics($routeParams.variantId, $scope.currentPage)
          .then(applyVariantMetricsData)
          .then(function() {
            $rootScope.isViewLoading = false;
          });
      } else {
        metricsEndpoint.fetchApplicationMetrics($routeParams.applicationId, $scope.currentPage)
          .then(applyApplicationMetricsData)
          .then(function() {
            $rootScope.isViewLoading = false;
          });
      }
    };

    $scope.variantMetricInformation = function(metricsEndpoint) {
      angular.forEach(metricsEndpoint, function(variantInfo) {
        angular.forEach($scope.application.variants, function (variant) {
          if (variant.variantID === variantInfo.variantID) {
            variantInfo.name = variant.name;
          }
        });
      });
      return metricsEndpoint;
    };

    $scope.detailsPage = function() {
      return onDetailsPage();
    };

    $scope.expand = function (metric) {
      metric.expand = !metric.expand;
    };

    $scope.isCollapsed = function (metric) {
      return !metric.expand;
    };

  });
