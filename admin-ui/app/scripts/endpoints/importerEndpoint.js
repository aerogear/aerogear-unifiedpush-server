'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('importerEndpoint', function ($resource, apiPrefix) {
  return $resource( apiPrefix + 'rest/registry/device/importer', {}, {
    import: {
      method: 'POST',
      headers: {'Content-Type': undefined},
      withCredentials: true,
      transformRequest: angular.identity
    }
  });
});
