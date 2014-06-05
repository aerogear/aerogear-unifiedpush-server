'use strict';

angular.module('newadminApp', [
  'newadminApp.services',
  'ngResource',
  'ngRoute',
  'ui.bootstrap',
  'ups.directives',
  'patternfly.notification',
  'hljs'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainController',
        crumb: {
          level: 0,
          label: 'Applications'
        }
      })
      .when('/detail/:applicationId', {
        templateUrl: 'views/detail.html',
        controller: 'DetailController',
        crumb: {
          level: 1,
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/:applicationId/installations/:variantId', {
        templateUrl: 'views/installation.html',
        controller: 'InstallationController',
        crumb: {
          level: 2,
          label: '$ variant.name ? variant.name : "Registering Installations"'
        }
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        crumb: {
          level: 2,
          label: 'Example'
        }
      })
      .when('/example/:applicationId/:variantType', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        crumb: {
          level: 2,
          label: 'Example'
        }
      })
      .when('/compose/:applicationId', {
        templateUrl: 'views/compose.html',
        controller: 'ComposeController',
        crumb: {
          level: 2,
          label: 'Compose'
        }
      })
      .when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardController'
      })
      .otherwise({
        redirectTo: '/'
      });

  });

