angular.module('upsConsole')
  .controller('ActivityController', function ( $log, $interval, $modal, variantModal, $scope, metricsEndpoint ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;
    this.metrics = [];
    this.totalCount;
    this.currentPage = 1;
    this.currentStart = 0;
    this.currentEnd = 0;
    this.perPage = 10;
    this.searchString = '';
    this.activeSearch = '';

    var refreshInterval;

    /**
     * Fetches new data, reflecting provided page and searchString
     */
    function fetchMetrics( page, searchString ) {
      return metricsEndpoint.fetchApplicationMetrics(self.app.pushApplicationID, searchString, page, self.perPage)
        .then(function( data ) {
          self.activeSearch = searchString;
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

    /**
     * Determines whether search is active - either the user typed search string or the data doesn't reflect the search string yet.
     *
     * @return false if searchString if false and data reflects that searchString; true otherwise
     */
    this.isSearchActive = function() {
      return self.searchString || self.activeSearch;
    };

    /**
     * Fetches new data on page change
     */
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

    $scope.$on('upsNotificationSent', refreshUntilAllServed);
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
