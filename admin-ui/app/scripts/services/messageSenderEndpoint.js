'use strict';

var upsServices = angular.module('upsConsole.services');

upsServices.factory('messageSenderEndpoint', function ($resource) {
  return function ( applicationID, masterSecret ) {
    return $resource('rest/sender', {}, {
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
