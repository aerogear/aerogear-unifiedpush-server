'use strict';

var newadminMod = angular.module('newadminApp', [
        'newadminApp.services',
        'ngResource'
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
   logout.logout({},{},function(){
       authz.login({},{loginName:"admin",password:"123"},
           function(){
               $rootScope.$broadcast('loginDone', 'loginDone');
           }
       );
   });
});


