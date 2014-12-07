'use strict';

var upsServices = angular.module('upsConsole.services');

upsServices.factory('importerEndpoint', function ($resource ) {
  return $resource('rest/registry/device/importer', {}, {
    import: {
      method: 'POST',
      headers: {'Content-Type': undefined},
      withCredentials: true,
      transformRequest: angular.identity
    }
  });
});
