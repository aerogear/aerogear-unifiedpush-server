angular.module('upsConsole')
  .controller('SenderController', function( $modal, $scope, applicationsEndpoint, ContextProvider ) {

    var self = this;

    this.app = $scope.$parent.$parent.appDetail.app;

    this.contextPath = ContextProvider.contextPath();

    this.renewMasterSecret = function () {
      $modal.open({
        templateUrl: 'inline:renew-master-secret.html',
        controller: function( $scope, $modalInstance ) {
          $scope.confirm = function() {
            applicationsEndpoint.reset({appId: self.app.pushApplicationID}, function (application) {
              self.app.masterSecret = application.masterSecret;
              $modalInstance.close( application );
            });
          };
          $scope.dismiss = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });
    };
  });
