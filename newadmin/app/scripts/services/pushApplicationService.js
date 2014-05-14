'use strict';

var backendMod = angular.module('newadminApp.services',[] ).
    value('version', '0.1');

backendMod.factory('pushApplication', function($resource) {
    return $resource('rest/applications/:appId/:verb', {
        appId : '@appId'
    }, {
        get : {
            method : 'GET'
        },
        query:  {method:'GET', isArray:true},
        create : {
            method : 'POST'
        },
        update : {
            method : 'PUT'
        },
        delete : {
            method: 'DELETE'
        },
        count : {
            method : 'GET',
            params : {verb: 'count'}
        },
        reset : {
            method : 'PUT',
            params : {verb: 'reset'}
        }
    });
});

backendMod.factory('variants', function($resource) {
    return $resource('/ag-push/rest/applications/:appId/:variantType/:variantId', {
        appId : '@appId',
        variantType: '@variantType'
    }, {
        get : {
            method : 'GET'
        },
        query:  {method:'GET', isArray:true},
        create : {
            method : 'POST'
        },
        update : {
            method : 'PUT'
        },
        delete : {
            method: 'DELETE'
        },
        patch: {
            method : 'PATCH'
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

backendMod.factory('installations', function($resource) {
    return $resource('/ag-push/rest/applications/:variantId/installations/:installationId', {
        variantId : '@variantId',
        installationId : '@installationId'
    }, {
        get : {
            method : 'GET',
            isArray: true
        },
        update : {
            method : 'PUT'
        }
    });
});

backendMod.factory('compose', function($resource) {
    return $resource('rest/sender/:appId', {
        appId : '@appId'
    }, {
       send : {
            method : 'POST'
        }
    });
});

//to be removed after KC integration
backendMod.factory('authz', function($resource) {
    return $resource('/ag-push/rest/auth/login', {

    }, {
         login : {
            method : 'POST'
        }
    });
});

backendMod.factory('logout', function($resource) {
    return $resource('/ag-push/rest/auth/logout', {

    }, {
        logout : {
            method : 'POST'
        }
    });
});




