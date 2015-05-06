'use strict';

angular.module('upsConsole').factory('appModal', function ($modal, applicationsEndpoint) {
  var service = {

    editName: function (app) {
      return $modal.open({
        templateUrl: 'dialogs/edit-app-name.html',
        controller: function ($scope, $modalInstance) {
          $scope.app = app;

          $scope.confirm = function () {
            var data = { name: app.name, description: app.description };
            applicationsEndpoint.update({ appId: app.pushApplicationID }, data )
              .then(function ( updatedApp ) {
                $modalInstance.close( updatedApp );
              });
          };

          $scope.dismiss = function () {
            $modalInstance.dismiss('cancel');
          }
        }
      }).result;
    }
  };

  return service;

});
