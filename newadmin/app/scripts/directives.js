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

    .directive('variants', function () {
        return {
            scope: {
                variants: '='
            },
            controller: function($scope, $window) {
                $scope.expand = function(variant) {
                    variant.expand = !variant.expand;
                };

                $scope.isCollapsed = function(variant) {
                    return !variant.expand;
                };

                var href = $window.location.href;
                $scope.currentLocation = href.substring(0, href.indexOf('#'));
            },
            templateUrl: 'directives/variant-details.html'
        };
    });
