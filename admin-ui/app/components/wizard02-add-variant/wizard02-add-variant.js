angular.module('upsConsole')
  .controller('Wizard02AddVariantController', function( variantModal, $router, createAppWizard, Notifications ) {

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
          createAppWizard.variant = variant;
          Notifications.success('Variant ' + variant.name + ' successfully created');
          $router.root.navigate('/wizard/register-device');
        })
        .catch(function() {
          Notifications.error('Failed to create variant ' + variant.name);
        });
    };

  });


