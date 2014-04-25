'use strict';

var newadminMod = angular.module('newadminApp', [
        'newadminApp.services',
        'ngResource',
        'ngRoute'
    ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });

  });

//temp code to be removed once we have integrated KeyCloak
newadminMod.run(function(authz,logout, $rootScope){
    logout.logout().$promise
        .finally(function() {
            authz.login({}, {loginName:"admin",password:"123"}).$promise
                .then(function() {
                    $rootScope.$broadcast('loginDone');
                });
        });
});


