'use strict';

/* Directives */
angular.module('ups.directives', [])

    .directive('upsNavigation', function () {
        return {
            scope: {
                current: '@'
            },
            restrict: 'E',
            replace: true,
            templateUrl: 'directives/ups-navigation.html'
        };
    })

    .directive('upsAlerts', function () {
        return {
            scope: {
            },
            controller: function($rootScope, $scope) {
                $scope.alerts = $rootScope.notifications.data;
            },
            restrict: 'E',
            replace: false,
            templateUrl: 'directives/ups-alerts.html'
        };
    })

    .directive('variants', function () {
        return {
            scope: {
                variants: '=',
                type: '@'
            },
            controller: function($scope) {
                $scope.expand = function(variant) {
                    variant.expand = !variant.expand;
                };

                $scope.isCollapsed = function(variant) {
                    return !variant.expand;
                };

                $scope.editVariant = function(variant, type) {
                    $scope.$parent.editVariant(variant, type);
                };

                $scope.removeVariant = function(variant, type) {
                    $scope.$parent.removeVariant(variant, type);
                };
            },
            templateUrl: 'directives/variant-details.html'
        };
    });
