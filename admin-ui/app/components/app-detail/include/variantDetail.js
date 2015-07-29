angular.module('upsConsole')
  .controller('VariantDetailController', function ( $modal, variantModal, $scope, installationsEndpoint, ContextProvider ) {

    var self = this;

    this.variant = $scope.$parent.variant;
    this.installations = [];
    this.totalCount;
    this.currentPage = 1;
    this.currentStart = 0;
    this.currentEnd = 0;
    this.perPage = 10;
    this.searchString = '';
    this.forceShowSnippets = false;
    this.contextPath = ContextProvider.contextPath();

    function fetchInstallations( page, searchString ) {
      installationsEndpoint.fetchInstallations(self.variant.variantID, searchString, page, self.perPage)
        .then(function( data ) {
          self.installations = data.installations;
          self.totalCount = data.total;
          self.currentStart = self.perPage * (self.currentPage - 1) + 1;
          self.currentEnd = self.perPage * (self.currentPage - 1) + self.installations.length;
        });
    }

    // initial page
    fetchInstallations( 1, null );

    this.onPageChange = function ( page ) {
      fetchInstallations( page, self.searchString );
    };

    $scope.$watch(function() { return self.searchString }, function( searchString ) {
      self.currentPage = 1;
      fetchInstallations( self.currentPage, self.searchString );
    });

    self.enableInstallation = function( variant, installation, enabled ) {
      var clone = angular.extend({}, installation, {enabled: enabled});
      installationsEndpoint.update({variantId: self.variant.variantID, installationId: installation.id}, clone)
        .then(function() {
          installation.enabled = enabled;
        });
    }

  });
