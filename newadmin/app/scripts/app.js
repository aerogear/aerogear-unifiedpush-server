'use strict';

var newadminMod = angular.module('newadminApp', [
        'newadminApp.services',
        'ngResource',
        'ngRoute',
        'ui.bootstrap',
        'ups.directives',
        'patternfly.notification'
    ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: MainController
      })
      .when('/detail/:applicationId', {
        templateUrl : 'views/detail.html',
        controller : DetailController
      })
      .when('/installations/:variantId', {
        templateUrl : 'views/installation.html',
        controller : InstallationController
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl : 'views/example.html',
        controller : ExampleController
      })
      .when('/compose/:applicationId', {
        templateUrl : 'views/compose.html',
        controller : ComposeController
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
                    $rootScope.loggedIn = true;
                });
        });
});


function onLoginDone($rootScope, $scope, callback) {
    if ($rootScope.loggedIn) {
        callback();
    } else {
        $scope.$on('loginDone', callback);
    }
}