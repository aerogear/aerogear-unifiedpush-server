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

  .factory('SnippetRetriever', function($http, $templateCache, $q) {
    return {
      get: function( snippetUrl ) {
        var cacheResult = $templateCache.get( snippetUrl );
        if ( cacheResult ) {
          return $q.when( { data: cacheResult } );
        }
        return $http.get( snippetUrl )
          .then(function( response ) {
            $templateCache.put( snippetUrl, response.data );
            return response;
          });
      }
    };
  })

  .factory('clientSnippets', function() {
    return {
      'android': { url: 'snippets/register-device/android.java' },
      'cordova': { url: 'snippets/register-device/cordova.js' },
      'ios_objc': { url: 'snippets/register-device/ios.objc' },
      'ios_swift': { url: 'snippets/register-device/ios.swift' },
      'dotnet': { url: 'snippets/register-device/dotnet.cs' },
      'adm': { url: 'snippets/register-device/adm.txt' }
    };
  })

  .factory('ClientSnippetService', function(SnippetRetriever, $q, clientSnippets) {
    var promises = {};
    angular.forEach(clientSnippets, function (value, key) {
      promises[key] = SnippetRetriever.get(value.url)
        .then(function(response){
          clientSnippets[key].template = response.data;
        });
    });
    return {
      populate: function (result) {
        return $q.all(promises)
          .then(function () {
            angular.forEach(clientSnippets, function (value, key) {
              if (!result[key]) {
                result[key] = {};
              }
              if (!result[key].template) {
                result[key].template = value.template;
              }
            });
            return result;
          });
      }
    };
  })

  .directive('upsClientSnippets', function (ClientSnippetService) {
    return {
      templateUrl: 'directives/ups-client-snippets.html',
      scope: {
        variant: '='
      },
      restrict: 'E',
      controller: function( $scope, ContextProvider, SnippetRetriever, $sce, $interpolate, $timeout ) {
        $scope.clipText = $sce.trustAsHtml('Copy to clipboard');
        $scope.contextPath = ContextProvider.contextPath();
        $scope.typeEnum = {
          android:      { name: 'Android',    snippets: ['android', 'cordova'] },
          ios:          { name: 'iOS',        snippets: ['ios_objc', 'ios_swift', 'cordova']},
          windows_mpns: { name: 'Windows',    snippets: ['dotnet', 'cordova'] },
          windows_wns:  { name: 'Windows',    snippets: ['dotnet', 'cordova'] },
          simplePush:   { name: 'SimplePush', snippets: ['cordova'] },
          adm:          { name: 'ADM',        snippets: ['adm'] }
        };
        $scope.state = {
          activeSnippet: $scope.typeEnum[$scope.variant.type].snippets[0]
        };
        $scope.snippets = {};
        function renderSnippets() {
          ClientSnippetService.populate($scope.snippets).then(function() {
            angular.forEach($scope.snippets, function(value, key) {
              $scope.snippets[key].source = $interpolate($scope.snippets[key].template)($scope);
            });
          });
        }
        renderSnippets();
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
        $scope.$watch('variant.secret', function() {
          renderSnippets();
        });
      }
    };
  })

  .factory('senderSnippets', function() {
    return {
      java: {
        url: 'snippets/senders/sender.java',
        show: true,
        text: {
          before: '<p>First you need to add <code>unifiedpush-java-client.jar</code> as a <a ups-doc="sender-downloads-java">dependency to your Java project</a>.</p>' +
                  '<p>Then let\'s use following snippet in your Java code to enable push notification sending.</p>',
          after:  '<p>Read more on the details of the <a ups-doc="sender-api-java">Java UPS Sender API in documentation</a>.</p>' +
                  '<p>If you have questions about this process, <a ups-doc="sender-step-by-step-java">visit the documentation for full step by step explanation</a>.</p>'
        }
      },
      nodejs: {
        url: 'snippets/senders/sender-nodejs.js',
        show: true,
        text: {
          before: '<p>First you need to download add <code>unifiedpush-node-sender</code> as a <a ups-doc="sender-downloads-nodejs">dependency to your project</a>.</p>' +
                  '<p>Then let\'s use following snippet in your Node.js code to enable push notification sending.</p>',
          after:  '<p>Read more on the details of the <a ups-doc="sender-api-nodejs">Node.js UPS Sender API in documentation</a>.</p>'
        }
      },
      curl: {
        url: 'snippets/senders/sender-curl.sh',
        show: true,
        text: {
          before: '<p>If none from the official client libs doesn\'t suit you or you just want to simply try out the notification sending, you can use REST API directly.</p>' +
                  '<p>Run following <code>curl</code> command in the shell to send notification to UPS server.</p>',
          after:  '<p>Read more on the details of the <a ups-doc="sender-api-rest">UPS REST Sender API in documentation</a>.</p>'
        }
      }
    };
  })

  .directive('upsSenderSnippets', function () {
    return {
      scope: {
        app: '=',
        activeSnippet: '@'
      },
      controller: function( $scope, ContextProvider, SnippetRetriever, $sce, $interpolate, $timeout, senderSnippets ) {

        $scope.clipText = $sce.trustAsHtml('Copy to clipboard');
        $scope.contextPath = ContextProvider.contextPath();
        $scope.snippets = senderSnippets;
        angular.forEach($scope.snippets, function(data, senderType) {
          if (data.show) {
            $scope.activeSnippet = $scope.activeSnippet || senderType;
            SnippetRetriever.get(data.url)
              .then(function (response) {
                $scope.snippets[senderType].source = $interpolate(response.data)($scope);
              });
          }
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
          }
        });
        $scope.$on('$destroy', unwatch);
      }
    };
  })

  .directive('upsBindHtmlCompile', function ($compile) {
    return function (scope, element, attrs) {
      var ensureCompileRunsOnce = scope.$watch(
        function (scope) {
          return scope.$eval(attrs.upsBindHtmlCompile);
        },
        function (value) {
          element.html(value);
          $compile(element.contents())(scope);
          ensureCompileRunsOnce();
        }
      );
    };
  });
