'use strict';

var backendMod = angular.module('newadminApp.services',[] ).
    value('version', '0.1');

backendMod.factory('pushApplication', function($resource) {
    return $resource('rest/applications/:appId/:count', {
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
            params : {count: 'count'}
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
        }
    });
});

backendMod.factory('installations', function($resource) {
    return $resource('/ag-push/rest/applications/:variantId/installations/', {
        variantId : '@variantId'
    }, {
        get : {
            method : 'GET',
            isArray: true
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




