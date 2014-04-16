'use strict';

angular.module('newadminApp' ).
    value('version', '0.1');

newadminApp.factory('pushApplication', function($resource) {
    return $resource('/rest/applications/:appId', {
        appId : '@appId'
    }, {
        get : {
            method : 'GET'
        },
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

newadminApp.factory('variants', function($resource) {
    return $resource('/rest/applications/:appId/:variantType', {
        appId : '@appId',
        variantType: '@variantType'
    }, {
        get : {
            method : 'GET'
        },
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




