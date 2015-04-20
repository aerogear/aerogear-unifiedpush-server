angular.module('upsConsole')
  .controller('AppDetailController', function( $routeParams, $modal, applicationsEndpoint, ContextProvider ) {

    var self = this;

    this.tab = $routeParams.tab;

    this.contextPath = ContextProvider.contextPath();

    this.canActivate = function() {
      return applicationsEndpoint.getWithMetrics({appId: $routeParams.app})
        .then(function( app ) {
          self.app = app;
          if ( !app.variants.length ) {
            self.tab = 'variants';
          }
        });
    };

    this.sendNotification = function() {
      $modal.open({
        templateUrl: 'views/dialogs/send-push-notification.html',
        controller: function( $scope, $modalInstance ) {
          $scope.send = function() {
            console.log('send');
            $modalInstance.close();
          };

          $scope.cancel = function() {
            console.log('cancel');
            $modalInstance.dismiss();
          };
        }
      });
    };

  });
