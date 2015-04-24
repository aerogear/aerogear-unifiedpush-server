angular.module('upsConsole')
  .controller('Wizard03RegisterDeviceController', function( variantModal, $router, createAppWizard ) {

    this.canActivate = function() {
      console.log('canActivate');
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

  });


