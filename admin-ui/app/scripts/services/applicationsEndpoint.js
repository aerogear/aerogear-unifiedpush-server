'use strict';

var upsServices = angular.module('upsConsole.services');

upsServices.factory('applicationsEndpoint', function ($resource, $q) {
  var resource = $resource('rest/applications/:appId/:verb', {
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
    }
  });

  return {
    
    get: resource.get,

    query: resource.query,

    create: resource.create,

    update: resource.update,

    delete: resource.delete,

    count: resource.count,

    reset: resource.reset,

    fetch: function(pageNo) {
      var deferred = $q.defer();
      this.query({page: pageNo - 1, per_page: 8}, function (data, responseHeaders) {
        deferred.resolve({
          page: data,
          total: responseHeaders('total')
        });
      });
      return deferred.promise;
    }
  };
});
