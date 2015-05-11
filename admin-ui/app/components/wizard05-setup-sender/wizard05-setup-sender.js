angular.module('upsConsole')
  .controller('Wizard05SetupSenderController', function( variantModal, $router, createAppWizard, ContextProvider, appModal ) {

    var self = this;

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

    this.app = createAppWizard.app;
    this.variant = createAppWizard.variant;
    this.contextPath = ContextProvider.contextPath();

    this.editAppName = function() {
      var appClone = angular.extend( {}, self.app );
      appModal.editName( appClone )
        .then(function( updatedApp ) {
          angular.extend( self.app, updatedApp );
        });
    };

  });


