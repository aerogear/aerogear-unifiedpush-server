'use strict';

var backendMod = angular.module('newadminApp.services', []).
  value('version', '0.1');

backendMod.factory('pushApplication', function ($resource) {
  return $resource('rest/applications/:appId/:verb', {
    appId: '@appId'
  }, {
    get: {
      method: 'GET'
    },
    query: {method: 'GET', isArray: true},
    create: {
      method: 'POST'
    },
    update: {
      method: 'PUT'
    },
    delete: {
      method: 'DELETE'
    },
    count: {
      method: 'GET',
      params: {verb: 'count'}
    },
    reset: {
      method: 'PUT',
      params: {verb: 'reset'}
    }
  });
});

backendMod.factory('variants', function ($resource) {
  return $resource('rest/applications/:appId/:variantType/:variantId', {
    appId: '@appId',
    variantType: '@variantType'
  }, {
    get: {
      method: 'GET'
    },
    query: {method: 'GET', isArray: true},
    create: {
      method: 'POST'
    },
    update: {
      method: 'PUT'
    },
    delete: {
      method: 'DELETE'
    },
    patch: {
      method: 'PATCH'
    },
    createWithFormData: {
      method: 'POST',
      headers: {'Content-Type': undefined},
      withCredentials: true,
      transformRequest: angular.identity
    },
    updateWithFormData: {
      method: 'PUT',
      headers: {'Content-Type': undefined},
      withCredentials: true,
      transformRequest: angular.identity
    }
  });
});

backendMod.factory('installations', function ($resource) {
  return $resource('rest/applications/:variantId/installations/:installationId', {
    variantId: '@variantId',
    installationId: '@installationId'
  }, {
    get: {
      method: 'GET',
      isArray: true
    },
    update: {
      method: 'PUT'
    }
  });
});

backendMod.factory('dashboard', function ($resource) {
  return $resource('rest/metrics/dashboard', {}, {
    get: {
      method: 'GET'
    }
  });
});

backendMod.factory('breadcrumbs', function ($rootScope, $route) {
  var BreadcrumbService = {
    breadcrumbs: [],
    get: function() {
      return this.breadcrumbs;
    },
    generateBreadcrumbs: function() {
      var routes = $route.routes,
        self = this;

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

      var getRouteByLevel = function(level) {
        var result = null;
        angular.forEach(routes, function(route) {
          if (route.crumb && route.crumb.level === level) {
            result = route;
          }
        });
        return result;
      };

      var label = function(route) {
        var label = route.crumb.label.indexOf('$') !== -1 ? $rootScope.$eval(route.crumb.label.substring(1)) : route.crumb.label;
        self.breadcrumbs.push({ label: label, path: route.path });
      };
      
      this.breadcrumbs = [];
      if ($route.current && $route.current.crumb) {
        label($route.current);
        for (var i = $route.current.crumb.level - 1; i >= 0 ; i--) {
          var route = getRouteByLevel(i);
          route.path = getRoute(route.originalPath);
          label(route);
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

  BreadcrumbService.generateBreadcrumbs();

  return BreadcrumbService;
});
