angular.module('upsConsole')
  .controller('VariantsController', function ( $modal, variantModal, $scope, variantsEndpoint ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;

    /* split the variant types to the groups so that they can be easily access */
    function splitByType( variants ) {
      return variants
        .sort(function(a, b) {
          return a.type.localeCompare(b.type);
        })
        .reduce(function(variantList, variant) {
          var variantType = variantList[variant.type] = variantList[variant.type] || [];
          variantType.push(variant);
          variantType.$deviceCount = (variantType.$deviceCount  || 0) + (variant.$deviceCount || 0);
          variantType.$messageCount = (variantType.$messageCount  || 0) + (variant.$messageCount || 0);
          return variantList;
        }, {});
    }
    this.byType = splitByType( this.app.variants );

    this.add = function() {
      return variantModal.add( this.app )
        .then(function( variant ) {
          variant.$deviceCount = 0;
          variant.$messageCount = 0;
          self.app.variants.push( variant );
          self.byType = splitByType( self.app.variants );
        });
    };

    this.edit = function( variant ) {
      var variantEditableCopy = angular.extend({}, variant)
      return variantModal.edit( this.app, variantEditableCopy )
        .then(function( variant ) {
          var variantToUpdate = self.app.variants.filter(function(v) {
            return v.variantID === variant.variantID;
          })[0];
          angular.extend(variantToUpdate, variant);
        });
    };

    this.delete = function( variant ) {
      $modal.open({
        templateUrl: 'views/dialogs/remove-variant.html',
        controller: function( $modalInstance, $scope ) {
          $scope.variant = variant;
          $scope.confirm = function() {
            variantsEndpoint.delete({
                  appId: self.app.pushApplicationID,
                  variantType: variant.type,
                  variantId: variant.variantID })
              .then(function () {
                self.app.variants = self.app.variants.filter(function( v ) {
                  return v != variant;
                });
                self.byType = splitByType( self.app.variants );
                $modalInstance.close();
              });
          };
          $scope.dismiss = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });
    };

  });
