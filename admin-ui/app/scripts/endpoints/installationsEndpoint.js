'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('installationsEndpoint', function ($resource, $q, apiPrefix) {
  var installationsService = $resource( apiPrefix + 'rest/applications/:variantId/installations/:installationId', {
    variantId: '@variantId',
    installationId: '@installationId'
  }, {
    get: {
      method: 'GET',
      isArray: true
    },
    update: {
      method: 'PUT'
    }
  });
  installationsService.fetchInstallations = function(variantId, searchString, pageNo, perPage) {
    var deferred = $q.defer();
    this.get({variantId: variantId, search: searchString, page: pageNo - 1, per_page: perPage}, function (data, responseHeaders) {
      deferred.resolve({
        installations: data,
        total: responseHeaders('total')
      });
    });
    return deferred.promise;
  };
  return installationsService;
});
