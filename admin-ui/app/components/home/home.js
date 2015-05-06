angular.module('upsConsole')
  .controller('HomeController', function( $q, $modal, $router, $rootScope, applicationsEndpoint, dashboardEndpoint, appModal, Notifications ) {

    var self = this;

    this.apps = [];
    this.topNotifications = [];

    this.canActivate = function() {
      this.currentPage = 1;
      return self.fetchNewPage(1)
        .then(function() {
          if (self.totalItems < 1) {
            $router.parent.navigate('/welcome');
            return false;
          }
        });
    };

    this.activate = function() {
      dashboardEndpoint.latestActiveApps()
        .then(function( data ) {
          self.topNotifications = data;
        });
    };

    this.pageChanged = function(page) {
      self.fetchNewPage(page);
    };

    this.fetchNewPage = function(page) {
      return applicationsEndpoint.fetch(page)
        .then(function( result ) {
          self.apps = result.apps;
          self.totalItems = result.totalItems;
        });
    };

    this.changeName = function(app) {
      var appClone = angular.extend({}, app);
      appModal.editName( appClone )
        .then(function( updatedApp ) {
          angular.extend( app, updatedApp );
          Notifications.success('The name of ' + app.name + ' application was successfully changed');
        })
        .catch(function() {
          if ( e != 'cancel' ) {
            Notifications.error('Failed to modify app ' + app.name + ': ' + e);
          }
        });
    };

    this.deleteApp = function(app) {
      $modal.open({
        templateUrl: 'dialogs/remove-app.html',
        controller: function( $modalInstance, $scope ) {
          $scope.app = app;
          $scope.confirm = function() {
            applicationsEndpoint.delete({appId: app.pushApplicationID})
              .then(function () {
                $rootScope.$broadcast('upsApplicationDeleted');
                return self.fetchNewPage(self.currentPage);
              })
              .then(function() {
                $modalInstance.close();
                if (self.totalItems < 1) {
                  $router.parent.navigate('/welcome');
                }
              });
          };
          $scope.dismiss = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });
    };

  });
