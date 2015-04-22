angular.module('upsConsole')
  .controller('ActivityController', function ( $modal, variantModal, $scope, metricsEndpoint ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;
    this.metrics = [];
    this.totalCount = 0;
    this.currentPage = 1;
    this.currentStart = 0;
    this.currentEnd = 0;
    this.perPage = 10;

    function fetchMetricsPage( page ) {
      metricsEndpoint.fetchApplicationMetrics(self.app.pushApplicationID, page, self.perPage)
        .then(function( data ) {
          self.metrics = data.pushMetrics;
          self.totalCount = data.totalItems;
          self.currentStart = self.perPage * (self.currentPage - 1) + 1;
          self.currentEnd = self.perPage * (self.currentPage - 1) + self.metrics.length;
          self.metrics.forEach(function( metric ) {
            try {
              metric.$message = JSON.parse(metric.rawJsonMessage);
            } catch (err) {
              console.log('failed to parse metric')
              metric.$message = {};
            }
          });
        });
    }

    // initial page
    fetchMetricsPage( 1 );

    this.onPageChange = function ( page ) {
      fetchMetricsPage( page );
    }

  });
