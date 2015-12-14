angular.module('upsConsole')
  .controller('Wizard01CreateAppController', function ( $rootScope, $router, variantModal, applicationsEndpoint, createAppWizard, Notifications ) {

    var self = this;

    this.application =  {};

    this.createApp = function() {
      applicationsEndpoint.create( self.application )
        .then(function( app ) {
          createAppWizard.app = app;
          Notifications.success('Application ' + self.application.name + ' successfully created');
          $rootScope.$broadcast('upsUpdateStats');
          $rootScope.$broadcast('upsNavigate', '/wizard/add-variant');
        })
        .catch(function() {
          Notifications.error('Failed to create application ' + self.application.name);
        });
    }
  })

  .factory('createAppWizard', function() {
    return {
      app: null,
      variant: null
    };
  });
