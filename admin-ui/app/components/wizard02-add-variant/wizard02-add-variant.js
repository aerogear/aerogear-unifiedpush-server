angular.module('upsConsole')
  .controller('Wizard02AddVariantController', function( variantModal, $router, createAppWizard, Notifications, ErrorReporter, appModal ) {

    this.canActivate = function() {
      if ( createAppWizard.app ) {
        return true;
      } else {
        $router.root.navigate('/wizard/create-app');
        return false;
      }
    };

    var self = this;

    this.app = createAppWizard.app;

    this.addVariant = function() {
      return variantModal.add( this.app )
        .then(function( variant ) {
          createAppWizard.variant = variant;
          Notifications.success('Variant ' + variant.name + ' successfully created');
          $router.root.navigate('/wizard/register-device');
        })
        .catch(function(e) {
          ErrorReporter.error(e, 'Failed to create variant ');
        });
    };

    this.editAppName = function() {
      var appClone = angular.extend( {}, self.app );
      appModal.editName( appClone )
        .then(function( updatedApp ) {
          angular.extend( self.app, updatedApp );
        });
    }

  });


