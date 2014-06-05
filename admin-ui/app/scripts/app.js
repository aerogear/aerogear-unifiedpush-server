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
          id: 'apps',
          label: 'Applications'
        }
      })
      .when('/detail/:applicationId', {
        templateUrl: 'views/detail.html',
        controller: 'DetailController',
        crumb: {
          id: 'app-detail',
          parent: 'apps',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/:applicationId/installations/:variantId', {
        templateUrl: 'views/installation.html',
        controller: 'InstallationController',
        crumb: {
          parent: 'app-detail',
          label: '$ variant.name ? variant.name : "Registering Installations"'
        }
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/example/:applicationId/:variantType', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/compose/:applicationId', {
        templateUrl: 'views/compose.html',
        controller: 'ComposeController',
        crumb: {
          parent: 'app-detail',
          label: 'Send Push'
        }
      })
      .when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardController',
        crumb: {
          id: 'dash',
          label: 'Dashboard'
        }
<<<<<<< HEAD
      })
      .when('/activity/:applicationId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        crumb: {
          id: 'activity',
          parent: 'dash',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/activity/:applicationId/:variantId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        crumb: {
          parent: 'activity',
          label: '$ variant.name ? variant.name : "Current variant"'
        }
=======
>>>>>>> support for multiple roots on breadcrumbs
      })
      .when('/activity/:applicationId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        crumb: {
          id: 'activity',
          parent: 'dash',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/activity/:applicationId/:variantId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        crumb: {
          parent: 'activity',
          label: '$ variant.name ? variant.name : "Current variant"'
        }
      })
      .otherwise({
        redirectTo: '/'
      });

  });

