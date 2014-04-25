'use strict';

angular.module('newadminApp')
  .controller('MainCtrl', function ($scope,pushApplication) {
        $scope.$on('loginDone', function(){
            //let's show all the applications
            $scope.applications =  pushApplication.query();
        });

  });
