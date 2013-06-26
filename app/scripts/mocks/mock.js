var apps = [
    {
        "id": "402881893f62a12c013f631dc6cd0000",
        "name": "Application 1",
        "description": "Application 1 Description",
        "pushApplicationID": "8a0cf1df-0bcc-4a75-8b2a-78dff2915038",
        "masterSecret": "a04f822c-2224-45d9-a203-297e37c26c93",
        "developer": "admin",
        "androidApps": [{
            "id": "402881893f62a12c013f66f676c80001",
            "name": "Application1Android",
            "description": "An Android Variant of the Mobile App",
            "variantID": "32cede3c-27df-4eac-b807-65c31fbfeabc",
            "secret": "66063664-4a87-4b02-8c0b-1d2b0cec0726",
            "developer": "admin",
            "instances": [],
            "googleKey": "1234567890"
        }],
        "simplePushApps": [],
        "iosapps": []
    }
];

var baseURL = "/ag-push/rest/";

$.mockjax({
    type: "GET",
    url: baseURL + "applications",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps;
    }
});


$.mockjax({
    type: "POST",
    url: baseURL + "applications",
    responseTime: 750,
    dataType: "json",
    response: function() {
        apps.push( {
            "id": "402881893f62a12c013f631dc6cd0000",
            "name": "Application 1",
            "description": "Application 1 Description",
            "pushApplicationID": "8a0cf1df-0bcc-4a75-8b2a-78dff2915038",
            "masterSecret": "a04f822c-2224-45d9-a203-297e37c26c93",
            "developer": "admin",
            "androidApps": [{
                "id": "402881893f62a12c013f66f676c80001",
                "name": "Application1Android",
                "description": "An Android Variant of the Mobile App",
                "variantID": "32cede3c-27df-4eac-b807-65c31fbfeabc",
                "secret": "66063664-4a87-4b02-8c0b-1d2b0cec0726",
                "developer": "admin",
                "instances": [],
                "googleKey": "1234567890"
            }],
            "simplePushApps": [],
            "iosapps": []
        });
        this.responseText = apps;
    }
});

