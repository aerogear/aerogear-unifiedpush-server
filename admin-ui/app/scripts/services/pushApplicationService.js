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







