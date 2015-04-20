'use strict';

var upsServices = angular.module('upsConsole.services');

upsServices.factory('applicationsEndpoint', function ($resource, $q) {
  return $resource('rest/applications/:appId/:verb', {
    appId: '@appId'
  }, {
    get: {
      method: 'GET'
    },
    query: {method: 'GET', isArray: true},
    create: {
      method: 'POST'
    },
    update: {
      method: 'PUT'
    },
    delete: {
      method: 'DELETE'
    },
    count: {
      method: 'GET',
      params: {verb: 'count'}
    },
    reset: {
      method: 'PUT',
      params: {verb: 'reset'}
    },
    fetch: function(pageNo) {
      var deferred = $q.defer();
      this.query({page: pageNo - 1, per_page: 8}, function (data, responseHeaders) {
        deferred.resolve({
          apps: data,
          totalItems: responseHeaders('total')
        });
      });
      return deferred.promise;
    },
    getWithMetrics: function(params) {
      params.includeDeviceCount = true;
      params.includeActivity = true;
      var deferred = $q.defer();
      this.get(params, function (app, responseHeaders) {
        app.$messageCount = parseInt(responseHeaders('activity_app_' + app.pushApplicationID), 10);
        app.$deviceCount = parseInt(responseHeaders('deviceCount_app_' + app.pushApplicationID), 10);
        app.variants.forEach(function( variant ) {
          variant.$messageCount = parseInt(responseHeaders('activity_variant_' + variant.variantID), 10);
          variant.$deviceCount = parseInt(responseHeaders('deviceCount_variant_' + variant.variantID), 10);
        });
        deferred.resolve( app );
      });
      return deferred.promise;
    }
  });
});
