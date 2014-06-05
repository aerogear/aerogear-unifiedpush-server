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

angular.module('newadminApp').controller('DashboardController',
  function ($scope, dashboard) {
    dashboard.get({}, function (data) {
      $scope.dashboardData = data;
    });
  });

angular.module('newadminApp').controller('ActivityController',
  function ($scope, $routeParams, $modal, metrics) {

    $scope.applicationId = $routeParams.applicationId;

    if (typeof $routeParams.variantId !== 'undefined') {
      metrics.variant({id: $routeParams.variantId}, function (data) {
        $scope.pushMetrics = data;
        angular.forEach(data, function (metric) {
          metric.totalReceivers = metric.variantInformations[0].receivers;
          metric.deliveryFailed = metric.variantInformations[0].deliveryStatus;
        });
      });
    } else {
      metrics.application({id: $routeParams.applicationId}, function(data) {
        $scope.pushMetrics = data;

        function totalReceivers(data) {
          angular.forEach(data, function (metric) {
            angular.forEach(metric.variantInformations, function (variant) {
              if (!variant.deliveryStatus) {
                metric.deliveryFailed = true;
              }
              if (!metric.totalReceivers) {
                metric.totalReceivers = 0;
              }
              metric.totalReceivers += variant.receivers;
            });
          });
        }

        totalReceivers(data);
      });
    }

    $scope.expand = function (metric) {
      metric.expand = !metric.expand;
    };

    $scope.isCollapsed = function (metric) {
      return !metric.expand;
    };

    $scope.parse = function (metric) {
      return JSON.parse(metric.rawJsonMessage);
    };

    $scope.showFullRequest = function (rawJsonMessage) {
      $modal.open({
        templateUrl: 'views/dialogs/request.html',
        controller: function ($scope, $modalInstance, request) {
          $scope.request = request;

          $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
          };
        },
        resolve: {
          request: function () {
            //nasty way to get formatted json
            return JSON.stringify(JSON.parse(rawJsonMessage), null, 4);
          }
        }
      });
    };

  });
