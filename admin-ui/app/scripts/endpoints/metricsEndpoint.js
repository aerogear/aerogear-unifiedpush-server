'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('metricsEndpoint', function ($resource, $q, apiPrefix) {
  return $resource( apiPrefix + 'rest/metrics/messages/:verb/:id', { id: '@id' }, {
    application: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'application'
      }
    },
    fetchApplicationMetrics: function(applicationId, searchString, pageNo, perPage) {
      perPage = perPage || 10;
      searchString = searchString || null;
      var deferred = $q.defer();
      this.application({id: applicationId, page: pageNo - 1, per_page: perPage, sort:'desc', search: searchString}, function (data, responseHeaders) {
        angular.forEach(data, function (metric) {
          angular.forEach(metric.variantInformations, function (variant) {
            if (!variant.deliveryStatus) {
              metric.deliveryFailed = true;
            }
          });
        });
        deferred.resolve({
          totalItems: parseInt(responseHeaders('total'), 10),
          receivers: parseInt(responseHeaders('receivers'), 10),
          appOpenedCounter: parseInt(responseHeaders('appOpenedCounter'), 10),
          pushMetrics: data
        });
      });
      return deferred.promise;
    }
  });
});
