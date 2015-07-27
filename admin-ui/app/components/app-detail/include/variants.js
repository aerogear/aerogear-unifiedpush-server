angular.module('upsConsole')
  .controller('VariantsController', function ( $http, $rootScope, $modal, variantModal, $scope, variantsEndpoint, exporterEndpoint, importerEndpoint, Notifications, ErrorReporter, allowCreateVariant, allVariantTypes ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;

    /* split the variant types to the groups so that they can be easily access */
    function splitByType( variants ) {
      return variants
        .sort(function(a, b) {
          return a.type.localeCompare(b.type);
        })
        .reduce(function(variantList, variant) {
          var type = variant.type.match(/^windows/) ? 'windows' : variant.type;
          var variantType = variantList[type] = variantList[type] || [];
          variantType.push(variant);
          variantType.$deviceCount = (variantType.$deviceCount  || 0) + (variant.$deviceCount || 0);
          variantType.$messageCount = (variantType.$messageCount  || 0) + (variant.$messageCount || 0);
          return variantList;
        }, {});
    }
    this.byType = splitByType( this.app.variants );

    if (Object.keys(this.byType).length == 1) {
      angular.forEach(this.byType, function(variants, type) {
        if (variants.length == 1) {
          variants[0].$toggled = true;
        }
      });
    }

    this.add = function() {
      return variantModal.add( this.app )
        .then(function( variant ) {
          variant.$deviceCount = 0;
          variant.$messageCount = 0;
          self.app.variants.push( variant );
          variant.$toggled = true;
          self.byType = splitByType( self.app.variants );
          Notifications.success('Variant ' + variant.name + ' successfully created');
        })
        .catch(function(e) {
          ErrorReporter.error(e, 'Failed to create variant');
        });
    };

    this.edit = function( variant ) {
      var variantClone = angular.extend({}, variant);
      return variantModal.edit( this.app, variantClone )
        .then(function( updatedVariant ) {
          angular.extend(variant, updatedVariant);
          Notifications.success('Variant ' + variant.name + ' was successfully modified');
        })
        .catch(function( e ) {
          if ( e != 'cancel' ) {
            ErrorReporter.error(e, 'Failed to modify variant ' + variant.name + ': ' + e);
          }
        });
    };

    this.editName = function( variant ) {
      var variantClone = angular.extend({}, variant);
      return variantModal.editName( self.app, variantClone )
        .then(function( updatedVariant ) {
          angular.extend(variant, updatedVariant);
          Notifications.success('The name of ' + variant.name + ' variant was successfully changed');
        })
        .catch(function(e) {
          if ( e !== 'cancel' ) {
            ErrorReporter.error(e, 'Failed to modify variant ' + variant.name + ': ' + e);
          }
        });
    };

    this.delete = function( variant ) {
      $modal.open({
        templateUrl: 'dialogs/remove-variant.html',
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

    this.renewVariantSecret = function ( variant ) {
      $modal.open({
        templateUrl: 'dialogs/renew-variant-secret.html',
        controller: function( $scope, $modalInstance ) {
          $scope.variant = variant;
          $scope.confirm = function() {
            variantsEndpoint.reset({
                appId: self.app.pushApplicationID,
                variantType: variant.type,
                variantId: variant.variantID })
              .then(function (receivedVariant) {
                variant.secret  = receivedVariant.secret;
                $modalInstance.close( variant );
                //$rootScope.$digest();
              });
          };
          $scope.dismiss = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });
    };

    this.exportInstallations = function ( variant ) {
      $modal.open({
        templateUrl: 'dialogs/export-installations.html',
        controller: function( $scope, $modalInstance ) {
          $scope.variant = variant;
          $scope.confirm = function() {
            var params = {
              variantId: variant.variantID
            };
            exporterEndpoint.export(params, function (content) {
              var hiddenElement = document.createElement('a');

              hiddenElement.href = 'data:attachment/json,' + encodeURI(JSON.stringify(content));
              hiddenElement.target = '_blank';
              hiddenElement.download = variant.variantID + '.json';
              hiddenElement.click();

              $modalInstance.close();
              Notifications.success('Successfully exported installations');
            });
          };
          $scope.dismiss = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });
    };

    this.importInstallations = function (variant) {
      $modal.open({
        templateUrl: 'dialogs/import-installations.html',
        controller: function( $scope, $modalInstance ) {
          $scope.variant = variant;
          $scope.installations = [];
          $scope.confirm = function() {
            var fd = new FormData();
            fd.append('file', $scope.installations[0]);
            $http.defaults.headers.common.Authorization = 'Basic ' + btoa(variant.variantID+
                ':' + variant.secret);
            importerEndpoint.import(null, fd, function(){
              Notifications.success('Import processing has started');
              $modalInstance.close();
            });
          };
          $scope.dismiss = function() {
            $modalInstance.dismiss('cancel');
          };
          $scope.previewImport = function() {
            if (window.File && window.FileList && window.FileReader) {
              var importFiles = $scope.installations[0];
              var fileReader = new FileReader();
              fileReader.readAsText(importFiles);
              fileReader.onload = function(event) {
                $scope.$apply(function() {
                  try {
                    $scope.importPreview = JSON.parse(event.target.result).length;
                    $scope.incorrectFormat = false;
                  }
                  catch(e) {
                    $scope.importPreview = null;
                    $scope.incorrectFormat = true;
                  }
                });
              };
            }
          };
        }
      });
    };

    this.getWarningsForVariant = function( warnings, variant ) {
      return warnings.filter(function( warning ) {
        return warning.variant.variantID == variant.variantID;
      });
    };

    this.isAllowedToAddVariant = function() {
      return allVariantTypes.some(function( variantType ) {
        return allowCreateVariant( self.app, variantType );
      });
    };

  });
