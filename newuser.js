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
    console.log(response);
    //let's create an application
    var upsOptions = {
      host: 'localhost',
      path: '/auth/admin/realms/aerogear/users',
      port: '8080',
     //This is what changes the request to a POST request
     method: 'POST',
      headers: {
          'Content-Type': 'application/json',
          'Authorization' : 'Bearer ' + access_token
      }
    };
    var upsReq = http.request(upsOptions, upsCallback);
    upsReq.write("{\"enabled\":true,\"username\":\"bob\", \"credentials\" : [ { \"type\" : \"password\",\"value\" : \"password\" }]}");
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

var req = http.request(options, callback);
//This is the data we are posting, it needs to be a string or a buffer
req.write("username=admin&password=123&client_id=ups-client");
req.end();