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
    this.searchString = '';

    function fetchMetrics( page, searchString ) {
      metricsEndpoint.fetchApplicationMetrics(self.app.pushApplicationID, searchString, page, self.perPage)
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
            metric.variantInformations.forEach(function( variantInformation ) {
              variantInformation.$variant = getVariantByID( variantInformation.variantID );
            });
          });
        });
    }

    // initial page
    fetchMetrics( 1, null );

    this.onPageChange = function ( page ) {
      fetchMetrics( page, self.searchString );
    };

    function getVariantByID ( variantID ) {
      return self.app.variants.filter(function( variant ) {
        return variant.variantID == variantID;
      })[0];
    }

    $scope.$on('upsNotificationSent', function( pushData, app ) {
      fetchMetrics( self.currentPage, self.searchString );
    });

    $scope.$watch(function() { return self.searchString }, function( searchString ) {
      self.currentPage = 1;
      fetchMetrics( self.currentPage, self.searchString );
    });

  });
