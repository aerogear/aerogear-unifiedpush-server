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

  .directive('upsFiles', function () {
    return {
      scope: {
        'files': '=upsFiles',
        'onChange': '&onChange'
      },
      restrict: 'A',
      replace: false,
      link: function ($scope, $element) {
        $element.bind('change', function (e) {
          $scope.$apply(function() {
            while ($scope.files.length > 0) {
              $scope.files.pop();
            }
            for (var i in e.target.files) {
              if (typeof e.target.files[i] === 'object') {
                $scope.files.push(e.target.files[i]);
              }
            }
            $scope.onChange();
          });
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
      '<span class="ups-pluralize" ng-if="count > 0"><span class="count">{{ count }}</span> {{ noun }}{{ count > 1 ? "s" : "" }}</span>' +
      '<span class="ups-pluralize zero" ng-if="count == 0"><span class="count">{{ zero == null ? "0" : zero }}</span> {{ noun }}s</span>'
    };
  })

  .directive('upsClientSnippets', function () {
    return {
      scope: {
        variant: '=',
        activeSnippet: '@'
      },
      controller: function( $scope, ContextProvider ) {
        $scope.typeEnum = {
          android:      { name: 'Android',    snippets: ['android', 'cordova'] },
          ios:          { name: 'iOS',        snippets: ['ios-objc', 'ios-swift', 'cordova']},
          windows_mpns: { name: 'Windows',    snippets: ['mpns', 'cordova'] },
          windows_wns:  { name: 'Windows',    snippets: ['wns', 'cordova'] },
          simplePush:   { name: 'SimplePush', snippets: ['cordova'] },
          adm:          { name: 'ADM',        snippets: ['cordova'] }
        };

        $scope.contextPath = ContextProvider.contextPath();

        $scope.state = {
          activeSnippet: $scope.activeSnippet
        };

        $scope.cordovaVariantType = (function() {
          switch ($scope.variant.type) {
          case 'windows_mpns':
            return 'windows';
          default:
            return $scope.variant.type;
          }
        })();

        $scope.senderID = $scope.variant.type === 'android' ? $scope.variant.projectNumber : null;
      },
      restrict: 'E',
      templateUrl: 'directives/ups-client-snippets.html'
    };
  })

  .directive('upsSenderSnippets', function () {
    return {
      scope: {
        app: '=',
        activeSnippet: '@'
      },
      controller: function( $scope, ContextProvider ) {
        $scope.contextPath = ContextProvider.contextPath();
      },
      restrict: 'E',
      templateUrl: 'directives/ups-sender-snippets.html'
    };
  });
