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
        controller: 'MainController'
      })
      .when('/detail/:applicationId', {
        templateUrl: 'views/detail.html',
        controller: 'DetailController'
      })
      .when('/installations/:variantId', {
        templateUrl: 'views/installation.html',
        controller: 'InstallationController'
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController'
      })
      .when('/example/:applicationId/:variantType', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController'
      })
      .when('/compose/:applicationId', {
        templateUrl: 'views/compose.html',
        controller: ComposeController
      })
      .otherwise({
        redirectTo: '/'
      });

  });

