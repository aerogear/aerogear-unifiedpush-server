'use strict';

var newadminMod = angular.module('newadminApp', [
        'newadminApp.services',
        'ngResource',
        'ngRoute',
        'ui.bootstrap'
    ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainController'
      })
      .when('/detail/:applicationId', {
        templateUrl : 'views/detail.html',
        controller : DetailController
      })
      .when('/installations/:variantId', {
        templateUrl : 'views/installation.html',
        controller : InstallationController
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


