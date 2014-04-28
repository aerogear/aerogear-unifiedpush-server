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
    });