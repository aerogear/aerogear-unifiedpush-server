'use strict';

/* Directives */
angular.module('ups.directives', ['upsConsole.services'])

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

  .directive('variant', function () {
    return {
      scope: {
        variant: '=',
        counts: '=',
        renewSecret: '&onRenew'
      },
      controller: function ($rootScope, $scope, $routeParams, ContextProvider) {
        $scope.expand = function (variant) {
          variant.expand = !variant.expand;
        };

        $scope.isCollapsed = function (variant) {
          return !variant.expand;
        };

        $scope.currentVariant = function (variant) {
          $rootScope.variant = variant;
        };

        $scope.detailCtrl = $scope.$parent.$parent.detailCtrl;
        $scope.applicationId = $routeParams.applicationId;
        $scope.currentLocation = ContextProvider.contextPath();        
      },
      templateUrl: 'directives/variant-details.html'
    };
  })

  .directive('upsFiles', function () {
    return {
      scope: {
        'files': '=upsFiles',
        previewImport: '&'
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
          $scope.previewImport();
        });
      }
    };
  })

  .directive('upsPluralize', function () {
    return {
      scope: {
        'noun': '@',
        'count': '=',
        'zero': '@'
      },
      restrict: 'E',
      template:
        '<span ng-show="count > 0"><strong>{{count}}</strong> {{ noun }}<span ng-show="count > 1">s</span></span>' +
        '<span ng-show="count == 0">{{zero ? zero : "No"}} {{ noun }}s</span>'
    };
  });
