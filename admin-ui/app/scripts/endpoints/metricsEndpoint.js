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
          metric.totalVariants = metric.variantInformations.length;
          metric.servedVariants = 0;
          metric.totalReceivers = 0;
          angular.forEach(metric.variantInformations, function (variantMetric) {
            metric.totalReceivers += variantMetric.receivers;
            if (!variantMetric.deliveryStatus) {
              metric.deliveryFailed = true;
            }
            if (variantMetric.servedBatches === variantMetric.totalBatches) {
              metric.servedVariants += 1;
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
