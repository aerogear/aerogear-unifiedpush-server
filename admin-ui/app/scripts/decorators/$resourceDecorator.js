'use strict';

angular.module('upsConsole')
  .config(function( $provide ) {

    /**
     * Decorator for $resource service which helps to maintain conciseness of the code by avoiding a need to call resource.method(...).$promise.then(...),
     * because it returns a promise directly: resource.method(...).then(...).
     */
    $provide.decorator('$resource', function($delegate) {
      return function decorator(url, paramDefaults, actions) {
        var args = Array.prototype.slice.call(arguments);
        var wrappedResource = {};
        var originalActions = {};
        var actionsWithoutFunctions = args[2] = {};
        Object.keys(actions).forEach(function( methodName ) {
          var method = actions[methodName];
          originalActions[methodName] = method;
          if (!angular.isFunction(method)) {
            actionsWithoutFunctions[methodName] = method;
          }
        });
        var originalResource = $delegate.apply($delegate, args);
        Object.keys(originalActions).forEach(function( methodName ) {
          var method = originalActions[methodName];
          if (angular.isFunction(method)) {
            wrappedResource[methodName] = method;
          } else {
            wrappedResource[methodName] = function() {
              return originalResource[methodName].apply(originalResource, arguments).$promise;
            };
          }
        });
        return wrappedResource;
      };
    });
  });

