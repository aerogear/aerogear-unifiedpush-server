angular.module('upsConsole')
  .controller('Wizard02AddVariantController', function( $rootScope, $router, variantModal, createAppWizard, Notifications, ErrorReporter, appModal, gettextCatalog ) {

    this.canActivate = function() {
      if ( createAppWizard.app ) {
        return true;
      } else {
        $rootScope.$broadcast('upsNavigate', '/wizard/create-app');
        return false;
      }
    };

    var self = this;

    this.app = createAppWizard.app;

    this.addVariant = function() {
      return variantModal.add( this.app )
        .then(function( variant ) {
          createAppWizard.variant = variant;
          Notifications.success(gettextCatalog.getString('Variant {{name}} successfully created', {name: variant.name}));
          $rootScope.$broadcast('upsNavigate', '/wizard/register-device');
        })
        .catch(function(e) {
          ErrorReporter.error(e, gettextCatalog.getString('Failed to create variant '));
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


