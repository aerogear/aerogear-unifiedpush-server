'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('exporterEndpoint', function ($resource ) {
  return $resource('rest/export/:variantId/installations', {}, {
    export: {
      method: 'GET',
      isArray: true
    }
  });
});
