angular.module('upsConsole')
  .controller('Wizard02AddVariantController', function( variantModal, $router, createAppWizard ) {

    this.canActivate = function() {
      console.log('canActivate');
      if ( createAppWizard.app ) {
        return true;
      } else {
        $router.root.navigate('/wizard/create-app');
        return false;
      }
    };

    this.app = createAppWizard.app;

    this.addVariant = function() {
      return variantModal.add( this.app )
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


