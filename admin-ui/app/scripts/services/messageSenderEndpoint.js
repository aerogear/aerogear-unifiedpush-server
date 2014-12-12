'use strict';

var upsServices = angular.module('upsConsole.services');

upsServices.factory('messageSenderEndpoint', function ($resource) {
  return $resource('rest/sender', {}, {
    send: {
      method: 'POST',
      headers: {
        'aerogear-sender': 'AeroGear UnifiedPush Console'
      }
    }
  });
});