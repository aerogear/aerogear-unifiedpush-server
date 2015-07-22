'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('exporterEndpoint', function ($resource, apiPrefix) {
  return $resource( apiPrefix + 'rest/export/:variantId/installations', {}, {
    export: {
      method: 'GET',
      isArray: true
    }
  });
});
