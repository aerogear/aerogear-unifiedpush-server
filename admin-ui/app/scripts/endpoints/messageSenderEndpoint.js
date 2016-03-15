'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('messageSenderEndpoint', function ($resource, apiPrefix) {
  return function ( applicationID, masterSecret ) {
    return $resource( apiPrefix + 'rest/sender', {}, {
      send: {
        method: 'POST',
        headers: {
          'aerogear-sender': 'AeroGear UnifiedPush Console',
          'Authorization': 'Basic ' + btoa(applicationID + ':' + masterSecret)
        }
      }
    });
  };
});
