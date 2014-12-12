'use strict';

var upsServices = angular.module('upsConsole.services');

upsServices.factory('breadcrumbs', function ($rootScope, $route) {
  var BreadcrumbService = {
    breadcrumbs: [],
    routes: {},
    get: function() {
      return this.breadcrumbs;
    },
    init: function() {
      var self = this;
      angular.forEach($route.routes, function(route) {
        if (route.crumb) {
          self.routes[route.crumb.id] = route;
        }
      });
    },
    generateBreadcrumbs: function() {
      var parent, self = this;

      var getRoute = function(route) {
        if ($route.current) {
          var param;
          angular.forEach($route.current.params, function (value, key) {
            if (route.indexOf(key) !== -1) {
              param = value;
            }
            if (param) {
              route = route.replace(':' + key, value);
            }
          });
          return route;
        }
      };

      var label = function(route) {
        return route.crumb.label.indexOf('$') !== -1 ? $rootScope.$eval(route.crumb.label.substring(1)) : route.crumb.label;
      };

      this.breadcrumbs = [];
      if ($route.current && $route.current.crumb) {
        self.breadcrumbs.push({ label: label($route.current), path: $route.current.path });
        parent = $route.current.crumb.parent;

        while (parent) {
          var route = self.routes[parent];
          route.path = getRoute(route.originalPath);
          self.breadcrumbs.push({ label: label(route), path: route.path });
          parent = route.crumb.parent;
        }

        self.breadcrumbs.reverse();
      }
    }
  };

  // We want to update breadcrumbs only when a route is actually changed
  // as $location.path() will get updated immediately (even if route change fails!)
  $rootScope.$on('$routeChangeSuccess', function() {
    BreadcrumbService.generateBreadcrumbs();
  });

  BreadcrumbService.init();
  BreadcrumbService.generateBreadcrumbs();

  return BreadcrumbService;
});