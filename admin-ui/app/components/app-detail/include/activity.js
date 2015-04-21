angular.module('upsConsole')
  .controller('ActivityController', function ( $modal, variantModal, $scope, metricsEndpoint ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;
    this.metrics = [];
    this.totalCount = 0;
    this.currentPage = 1;

    metricsEndpoint.fetchApplicationMetrics(this.app.pushApplicationID, 1)
      .then(function( data ) {
        self.metrics = data.pushMetrics;
        self.totalCount = data.totalItems;
        self.metrics.forEach(function( metric ) {
          try {
            metric.$message = JSON.parse(metric.rawJsonMessage);
          } catch (err) {
            console.log('failed to parse metric')
            metric.$message = {};
          }
        });
        console.log(self.metrics);
      });

    this.pageChanged = function () {
      // do nothing for now
    }

  });
