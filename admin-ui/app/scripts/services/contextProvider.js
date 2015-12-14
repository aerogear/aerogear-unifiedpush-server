'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('ContextProvider', function( $location, apiPrefix ) {
  return {
    /**
     * getter for the root context of the application
     * (the URL where application can be accessed)
     */
    contextPath: function() {
      var href = $location.absUrl();
      href = href.split('#', 1).join('');
      href = href.split('?', 1).join('');
      return href + apiPrefix;
    }
  };
});
