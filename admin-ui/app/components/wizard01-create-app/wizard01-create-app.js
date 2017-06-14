angular.module('upsConsole')
  .controller('Wizard01CreateAppController', function ( $rootScope, $router, variantModal, applicationsEndpoint, createAppWizard, Notifications, gettextCatalog ) {

    var self = this;

    this.application =  {};

    this.createApp = function() {
      applicationsEndpoint.create( self.application )
        .then(function( app ) {
          createAppWizard.app = app;
          Notifications.success(gettextCatalog.getString('Application {{name}} successfully created', {name: self.application.name}));
          $rootScope.$broadcast('upsUpdateStats');
          $rootScope.$broadcast('upsNavigate', '/wizard/add-variant');
        })
        .catch(function() {
          Notifications.error(gettextCatalog.getString('Failed to create application {{name}}', {name: self.application.name}));
        });
    }
  })

  .factory('createAppWizard', function() {
    return {
      app: null,
      variant: null
    };
  });
