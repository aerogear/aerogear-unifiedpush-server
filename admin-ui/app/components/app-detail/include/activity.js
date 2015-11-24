angular.module('upsConsole')
  .controller('ActivityController', function ( $log, $timeout, $interval, $modal, variantModal, $scope, metricsEndpoint ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;
    this.metrics = [];
    this.totalCount;
    this.currentPage = 1;
    this.currentStart = 0;
    this.currentEnd = 0;
    this.perPage = 10;
    this.searchString = '';

    var refreshInterval;

    function fetchMetrics( page, searchString ) {
      return metricsEndpoint.fetchApplicationMetrics(self.app.pushApplicationID, searchString, page, self.perPage)
        .then(function( data ) {
          self.metrics.forEach(function( originalMetric ) {
            data.pushMetrics.some(function ( newMetric ) {
              if (originalMetric.id === newMetric.id && originalMetric.$toggled) {
                newMetric.$toggled = true;
                return true;
              }
            });
          });
          self.metrics = data.pushMetrics;
          self.totalCount = data.totalItems;
          self.currentStart = self.perPage * (self.currentPage - 1) + 1;
          self.currentEnd = self.perPage * (self.currentPage - 1) + self.metrics.length;
          self.metrics.forEach(function( metric ) {
            try {
              metric.$message = JSON.parse(metric.rawJsonMessage);
            } catch (err) {
              console.log('failed to parse metric');
              metric.$message = {};
            }
            metric.variantInformations.forEach(function( variantInformation ) {
              variantInformation.$variant = getVariantByID( variantInformation.variantID );
            });
          });
        });
    }

    this.onPageChange = function ( page ) {
      fetchMetrics( page, self.searchString );
    };

    function getVariantByID ( variantID ) {
      return self.app.variants.filter(function( variant ) {
        return variant.variantID == variantID;
      })[0];
    }

    function refreshUntilAllServed() {
      fetchMetrics( self.currentPage, self.searchString )
        .then(function() {
          $log.debug('refreshed');
          var isPending = self.metrics.some(function(metric) {
            return metric.servedVariants < metric.totalVariants;
          });
          if (isPending) {
            if (!refreshInterval) {
              $log.debug('scheduling refresh');
              refreshInterval = $interval(refreshUntilAllServed, 1000);
            }
          } else {
            $log.debug('clearing refresh');
            $interval.cancel(refreshInterval);
            refreshInterval = null;
          }
        });
    }

    // initial load
    refreshUntilAllServed();

    $scope.$on('upsNotificationSent', function() {
      var timer1 = $timeout(refreshUntilAllServed, 500); // artificial delay - refresh after 0.5sec to ensure server has time to load some batches; prevents situation when totalBatches = 0 for all variants
      var timer2 = $timeout(refreshUntilAllServed, 3000); // refresh again to be double-sure ;-) note: should be addressed as part of https://issues.jboss.org/browse/AGPUSH-1513
      // destroy timeouts
      $scope.$on("$destroy", function() {
        $log.debug('cancelling refreshUntilAllServed timeouts');
        $timeout.cancel( timer1 );
        $timeout.cancel( timer2 );
      });
    });
    $scope.$on('$destroy', function () {
      if (refreshInterval) {
        $log.debug('cancelling refreshInterval');
        $interval.cancel(refreshInterval);
      }
    });

    $scope.$watch(function() { return self.searchString }, function( searchString ) {
      self.currentPage = 1;
      fetchMetrics( self.currentPage, self.searchString );
    });

  });
