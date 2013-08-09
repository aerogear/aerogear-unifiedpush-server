document.write('<div id="ember-testing-container"><div id="ember-testing"></div></div>');

Ember.testing = true;

App.rootElement = '#ember-testing';
App.setupForTesting();
App.injectTestHelpers();

function exists(selector) {
    return !!find(selector).length;
}

function missing(selector) {
    var error = "element " + selector + " found (should be missing)";
    throws(function() { find(selector); }, error);
}


$.mockjax({
    url: "http://localhost:9876/rest/auth/login",
    type: "POST",
    dataType: 'json',
    response: function( arguments ) {
        var password = JSON.parse(arguments.data).password;

        if( password === "123" ) {
            this.status = 403;
        } else if( password === "1234" ) {
            this.status = 204;
        } else {
            this.status = 401;
        }
    }
});

$.mockjax({
    url: "http://localhost:9876/rest/auth/update",
    type: "PUT",
    status: 204,
    dataType: 'json'
});

$.mockjax({
    url: "http://localhost:9876/rest/applications",
    type: "GET",
    dataType: 'json',
    response: function( arguments ) {
        this.responseText = [];
    }
});

$.mockjaxSettings.logging = false;
$.mockjaxSettings.responseTime = 0;
