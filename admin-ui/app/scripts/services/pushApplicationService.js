'use strict';

var backendMod = angular.module('upsConsole.services', []).
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
  return $resource('rest/applications/:appId/:variantType/:variantId/:verb', {
    appId: '@appId',
    variantType: '@variantType',
    variantId: '@variantId'
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
    },
    reset: {
      method: 'PUT',
      params: {verb: 'reset'}
    }
  });
});

backendMod.factory('messageSender', function ($resource) {
  return $resource('rest/sender', {}, {
    send: {
      method: 'POST',
      headers: {
        'aerogear-sender': 'AeroGear UnifiedPush Console'
      }
    }
  });
});

backendMod.factory('installations', function ($resource, $q) {
  var installationsService = $resource('rest/applications/:variantId/installations/:installationId', {
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
  
  installationsService.fetchInstallations = function(variantId, pageNo) {
    var deferred = $q.defer();
    this.get({variantId: variantId, page: pageNo - 1, per_page: 10}, function (data, responseHeaders) {
      deferred.resolve({
        page: data,
        total: responseHeaders('total')
      });
    });
    return deferred.promise;
  };
  
  return installationsService;
});

backendMod.factory('dashboard', function ($resource) {
  return $resource('rest/metrics/dashboard/:verb', {}, {
    totals: {
      method: 'GET'
    },
    warnings: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'warnings'
      }
    },
    topThree: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'active'
      }
    }
  });
});

backendMod.factory('metrics', function ($resource) {
  return $resource('rest/metrics/messages/:verb/:id', {
    id: '@id'
  }, {
    application: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'application'
      }
    },
    variant: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'variant'
      }
    }
  });
});


backendMod.factory('breadcrumbs', function ($rootScope, $route) {
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
