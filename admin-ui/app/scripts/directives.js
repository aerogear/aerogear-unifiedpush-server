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

  .directive('upsBreadcrumb', function () {
    return {
      templateUrl: 'directives/ups-breadcrumb.html',
      controller: function($scope, $compile, breadcrumbs) {
        $scope.breadcrumbs = breadcrumbs;
      }
    };
  })

  .directive('variants', function () {
    return {
      scope: {
        variants: '=',
        counts: '=',
        type: '@'
      },
      controller: function ($rootScope, $scope, $routeParams, $location) {
        $scope.expand = function (variant) {
          variant.expand = !variant.expand;
        };

        $scope.isCollapsed = function (variant) {
          return !variant.expand;
        };

        $scope.editVariant = function (variant, type) {
          $scope.$parent.editVariant(variant, type);
        };

        $scope.removeVariant = function (variant, type) {
          $scope.$parent.removeVariant(variant, type);
        };

        $scope.applicationId = $routeParams.applicationId;
        var href = $location.absUrl();
        $scope.currentLocation = href.substring(0, href.indexOf('#'));

        $scope.currentVariant = function (variant) {
          $rootScope.variant = variant;
        };

      },
      templateUrl: 'directives/variant-details.html'
    };
  })

  .directive('upsFiles', function () {
    return {
      scope: {
        'files': '=upsFiles'
      },
      restrict: 'A',
      replace: false,
      link: function ($scope, $element) {
        $element.bind('change', function (e) {
          while ($scope.files.length > 0) {
            $scope.files.pop();
          }
          for (var i in e.target.files) {
            if (typeof e.target.files[i] === 'object') {
              $scope.files.push(e.target.files[i]);
            }
          }
        });
      }
    };
  })

  .directive('upsPluralize', function () {
    return {
      scope: {
        'noun': '@',
        'count': '='
      },
      restrict: 'E',
      template:
        '<span ng-show="count > 0"><strong>{{count}}</strong> {{ noun }}<span ng-show="count > 1">s</span></span>' +
        '<span ng-show="count == 0">No {{ noun }}s</span>'
    };
  });
