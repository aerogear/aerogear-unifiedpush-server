'use strict';

angular.module('upsConsole', [
  'upsConsole.services',
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
        section: 'applications',
        crumb: {
          id: 'apps',
          label: 'Applications'
        }
      })
      .when('/detail/:applicationId', {
        templateUrl: 'views/detail.html',
        controller: 'DetailController',
        section: 'applications',
        crumb: {
          id: 'app-detail',
          parent: 'apps',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/:applicationId/installations/:variantId', {
        templateUrl: 'views/installation.html',
        controller: 'InstallationController',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: '$ variant.name ? variant.name : "Registering Installations"'
        }
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/example/:applicationId/:variantType', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        section: 'applications',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/compose', {
        templateUrl: 'views/compose-app.html',
        controller: 'PreComposeController',
        section: 'compose',
        crumb: {
          label: 'Send Push'
        }
      })
      .when('/compose/:applicationId', {
        templateUrl: 'views/compose.html',
        controller: 'ComposeController',
        section: 'compose',
        crumb: {
          parent: 'app-detail',
          label: 'Send Push'
        }
      })
      .when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardController',
        section: 'dashboard',
        crumb: {
          id: 'dash',
          label: 'Dashboard'
        }
      })
      .when('/activity/:applicationId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        section: 'dashboard',
        crumb: {
          id: 'activity',
          parent: 'dash',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/activity/:applicationId/:variantId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        section: 'dashboard',
        crumb: {
          parent: 'activity',
          label: '$ variant.name ? variant.name : "Current variant"'
        }
      })
      .otherwise({
        redirectTo: '/'
      });

  });

