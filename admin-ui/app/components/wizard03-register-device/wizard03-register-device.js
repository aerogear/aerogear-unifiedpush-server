angular.module('upsConsole')
  .controller('Wizard03RegisterDeviceController', function( variantModal, $router, createAppWizard, appModal ) {

    this.canActivate = function() {
      if ( !createAppWizard.app ) {
        $router.root.navigate('/wizard/create-app');
        return false;
      }
      if ( !createAppWizard.variant ) {
        $router.root.navigate('/wizard/add-variant');
        return false;
      }
      return true;
    };

    var self = this;

    this.app = createAppWizard.app;
    this.variant = createAppWizard.variant;

    this.addVariant = function() {
      return variantModal.add()
        .then(function( variant ) {
          console.log('success');
          console.log(variant);
          createAppWizard.variant = variant;
          $router.root.navigate('/wizard/register-device');
        })
        .catch(function() {
          console.log('error');
        })
    };

    this.editAppName = function() {
      var appClone = angular.extend( {}, self.app );
      appModal.editName( appClone )
        .then(function( updatedApp ) {
          angular.extend( self.app, updatedApp );
        });
    };

    this.editVariant = function() {
      var variantClone = angular.extend({}, self.variant);
      return variantModal.edit( self.app, variantClone )
        .then(function( updatedVariant ) {
          angular.extend(self.variant, updatedVariant);
        });
    }

  });


