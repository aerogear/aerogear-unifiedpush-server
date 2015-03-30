angular.module('upsConsole')
  .controller('Wizard01CreateAppController', function ( variantModal, $router, applicationsEndpoint, createAppWizard ) {

    var ctrl = this;

    this.application =  {};

    this.createApp = function() {
      applicationsEndpoint.create( ctrl.application )
        .then(function( app ) {
          console.log('created: ');
          console.log(app);
          createAppWizard.app = app;
          $router.root.navigate('/wizard/add-variant');
        })
        .catch(function() {
          console.log('error');
        });
    }
  })

  .factory('createAppWizard', function() {
    return {
      app: null,
      variant: null
    };
  });
