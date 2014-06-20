'use strict';

angular.module('upsConsole.services')

  .config(function($httpProvider) {
    $httpProvider.interceptors.push(function($q, $interpolate, Notifications) {
      return {
        'responseError': function(rejection) {
          var expression = $interpolate('Server returned {{status}}: {{statusText}}');
          Notifications.error(expression(rejection));
          return $q.reject(rejection);
        }
      };
    });
  });
