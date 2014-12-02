'use strict';

angular.module('upsConsole.services')

  .config(function($httpProvider) {
    $httpProvider.interceptors.push(function($q, $interpolate, Notifications) {
      return {
        'responseError': function(rejection) {
          var messageExpr, message;
          if (rejection.status === 400 && typeof rejection.data === 'object') {
            Object.keys(rejection.data).forEach( function( key ) {
              message = rejection.data[key];
              Notifications.error( message );
            });
          } else {
            messageExpr = $interpolate('Server returned {{status}}: {{statusText}}');
            message = messageExpr( rejection );
            Notifications.error(messageExpr(rejection));
          }
          return $q.reject(rejection);
        }
      };
    });
  });
