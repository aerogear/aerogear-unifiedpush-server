'use strict';

/* Directives */
angular.module('upsConsole')

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

  .directive('upsDoc', function ( docsLinks, $log ) {
    return {
      scope: {
        'docId': '@',
        'param': '='
      },
      restrict: 'A',
      replace: false,
      link: function ($scope, $element, attributes) {
        $element.attr('target', '_blank');
        attributes.$observe('upsDoc', function( upsDocValue ) {

          function updateHref() {
            var href = docsLinks[ upsDocValue ];
            if (href) {
              $element.attr('href', href);
            } else if (Object.keys(docsLinks).length > 0) {
              $log.warn('ups-doc: cannot resolve a link for id: ' + upsDocValue);
              return true;
            }
            return !!href; // return true if we resolved the href
          }

          if (!updateHref()) {
            var unwatch = $scope.$watch(function () {
              return docsLinks[ upsDocValue ];
            }, function () {
              if (updateHref()) {
                unwatch(); // if we finally resolve the href
              }
            });
          }
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
        variant: '='
      },
      controller: function( $scope, ContextProvider, $http, $sce, $interpolate, $timeout ) {
        $scope.clipText = $sce.trustAsHtml('Copy to clipboard');
        $scope.contextPath = ContextProvider.contextPath();
        $scope.typeEnum = {
          android:      { name: 'Android',    snippets: ['android', 'cordova'] },
          ios:          { name: 'iOS',        snippets: ['ios_objc', 'ios_swift', 'cordova']},
          windows_mpns: { name: 'Windows',    snippets: ['mpns', 'cordova'] },
          windows_wns:  { name: 'Windows',    snippets: ['wns'] },
          simplePush:   { name: 'SimplePush', snippets: ['cordova'] },
          adm:          { name: 'ADM',        snippets: ['cordova'] }
        };
        $scope.state = {
          activeSnippet: $scope.typeEnum[$scope.variant.type].snippets[0]
        };
        $scope.snippets = {
          'android': { url: 'snippets/register-device/android.java' },
          'cordova': { url: 'snippets/register-device/cordova.js' },
          'ios_objc': { url: 'snippets/register-device/ios.objc' },
          'ios_swift': { url: 'snippets/register-device/ios.swift' },
          'mpns': { url: 'snippets/register-device/mpns.cs' },
          'wns': { url: 'snippets/register-device/wns.cs' }
        };
        angular.forEach($scope.snippets, function(value, key) {
          $http.get( value.url )
            .then(function( response ) {
              $scope.snippets[key].source = $interpolate(response.data)($scope);
            });
        });
        $scope.copySnippet = function() {
          return $scope.snippets[$scope.state.activeSnippet].source;
        };
        $scope.copied = function() {
          $scope.clipText = 'Copied!';
          $timeout(function() {
            $scope.clipText = 'Copy to clipboard';
          }, 1000);
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
      controller: function( $scope, ContextProvider, $http, $sce, $interpolate, $timeout ) {
        $scope.activeSnippet = $scope.activeSnippet || 'java';
        $scope.clipText = $sce.trustAsHtml('Copy to clipboard');
        $scope.contextPath = ContextProvider.contextPath();
        $scope.snippets = {
          java: { url: 'snippets/senders/sender.java' },
          nodejs: { url: 'snippets/senders/sender-nodejs.js' },
          curl: { url: 'snippets/senders/sender-curl.sh' }
        };
        angular.forEach($scope.snippets, function(value, key) {
          $http.get( value.url )
            .then(function( response ) {
              $scope.snippets[key].source = $interpolate(response.data)($scope);
            });
        });
        $scope.copySnippet = function() {
          return $scope.snippets[$scope.activeSnippet].source;
        };
        $scope.copied = function() {
          $scope.clipText = 'Copied!';
          $timeout(function() {
            $scope.clipText = 'Copy to clipboard';
          }, 1000);
        };
      },
      restrict: 'E',
      templateUrl: 'directives/ups-sender-snippets.html'
    };
  })

  .directive('searchPf', function() {
    return {
      scope: {
      },
      restrict: 'C',
      replace: false,
      link: function ($scope, $element) {
        $element.find('.has-clear .clear').each(function() {
          if (!$(this).prev('.form-control').val()) {
            $(this).hide();
          }
        });
        // Show the clear button upon entering text in the search input
        $element.find('.has-clear .form-control').keyup(function () {
          var t = $(this);
          t.next('button').toggle(Boolean(t.val()));
        });
        // Upon clicking the clear button, empty the entered text and hide the clear button
        $element.find('.has-clear .clear').click(function () {
          $(this).prev('.form-control').val('').focus();
          $(this).hide();
        });
      }
    };
  })

  .directive('upsWizard', function() {
    return {
      scope: {
      },
      restrict: 'A',
      replace: false,
      link: function ($scope, $element) {
        var highestCol = 0;
        $element.find('.well').each(function () {
          highestCol = Math.max(highestCol, $(this).height());
        });
        $element.find('.well').height(highestCol);
      }
    };
  })

  .directive('sidebarPf', function() {
    return {
      restrict: 'C',
      link: function () {
        sidebar();
      }
    };
  })

  .directive('prettyprint', function() {
    return {
      restrict: 'C',
      link: function ($scope, $element) {
        var unwatch = $scope.$watch(function() {
          return $element.text();
        }, function( text ) {
          if (text) {
            window.requestAnimationFrame(function() {
              $scope.var = $element.html();
              $element.html(prettyPrintOne($element.html(), '', false));
            });
            unwatch();
          }
        });
      }
    };
  });
