'use strict';

var upsServices = angular.module('upsConsole.services');

upsServices.factory('installationsEndpoint', function ($resource, $q) {
  var installationsService = $resource('rest/applications/:variantId/installations/:installationId', {
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
  installationsService.fetchInstallations = function(variantId, pageNo) {
    var deferred = $q.defer();
    this.get({variantId: variantId, page: pageNo - 1, per_page: 10}, function (data, responseHeaders) {
      deferred.resolve({
        page: data,
        total: responseHeaders('total')
      });
    });
    return deferred.promise;
  };
  return installationsService;
});