/*
This script test keycloak's direct grant feature. 
It will log in as admin, retrieve an auth token and then create a Push Application
in the UnifiedPush Server.
*/

var http = require('http'); // replace with require('https') for ssl support


var options = {
    host: 'localhost',
    path: '/auth/realms/aerogear/tokens/grants/access',
    //since we are listening on a custom port, we need to specify it by hand
    port: '8080',
    //This is what changes the request to a POST request
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
};

callback = function(response) {
    var str = ''
    response.on('data', function (chunk) {
        console.log("receiving data");
        str += chunk;
    });

    response.on('end', function () {
        var response = JSON.parse(str);
        var access_token = response.access_token;
        console.log(access_token);
        //let's create an application
        var upsOptions = {
            host: 'localhost',
            path: '/ag-push/rest/applications/',
            port: '8080',
            //This is what changes the request to a POST request
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization' : 'Bearer ' + access_token
            }
        };
        var upsReq = http.request(upsOptions, upsCallback);
        upsReq.write("{\"description\":\"test direct grant\",\"name\":\"directGrantCreatedApp\"}");
        upsReq.end();

    });
}

upsCallback = function(response) {
    var str = ''
    response.on('data', function (chunk) {
        console.log("receiving data");
        str += chunk;
    });

    response.on('end', function () {
        console.log(str);
    });
}

// this is just a test/demo
var badHabbit = "GIVE_ME_SOMETHING_SO_THAT_THE_TEST_WORKS";

var req = http.request(options, callback);
//This is the data we are posting, it needs to be a string or a buffer
req.write("username=admin&password=" + badHabbit + "&client_id=ups-client");
req.end();
