var apps = [
        {
            "id": "402881893f62a12c013f631dc6cd0000",
            "name": "Applicfation 113fvv",
            "description": "Application 1 Description, NEAT relly long ioijwfi iwfwjf wjfwjef wjfwojf ",
            "pushApplicationID": "8a0cf1df-0bcc-4a75-8b2a-78dff2915038",
            "masterSecret": "a04f822c-2224-45d9-a203-297e37c26c93",
            "developer": "admin",
            "androidApps": [],
            "simplePushApps": [],
            "iosapps": []
        },
        {
            "id": "402881883fa49511013fa5b206ed0005",
            "name": "Cool Push App",
            "description": "Push application that i'm testing with\n",
            "pushApplicationID": "186d02c4-e9ce-4047-90c1-cde93e10c066",
            "masterSecret": "5b358dd8-c143-45ce-b675-d4068c8e0ccb",
            "developer": "admin",
            "androidApps": [
                {
                    "id": "402881883fa49511013fa5b533f60006",
                    "name": "Cool Push App Android",
                    "description": "the android Version",
                    "variantID": "c1d7a28f-70cd-4b08-b827-0ca55e1bb1f3",
                    "secret": "ef0cefd5-9fc2-45d7-a483-cfe8baac4e35",
                    "developer": "admin",
                    "instances": [
                        {
                            "id": "402881883fa49511013fa5d2aeea0007",
                            "deviceToken": "APA91bFnWmpdBL5r85PFf3_gIF54BUyfqOLKDO34UEowjOO0Zk-xvMoeQNP34WPj2V65oSA67NCKoc6yLHt9uIHNMy0ox3hr7feQgRe0jbXt1R1cluI1Pey06QsEWih0v5yW4VT5s7yuqfa83DAUf2Q2FR-yMHXdhg",
                            "deviceType": "ANDROID",
                            "mobileOperatingSystem": "android",
                            "osVersion": "2.3.5",
                            "alias": null,
                            "category": null
                        }
                    ],
                    "googleKey": "AIzaSyBIsFgx6g7ymVlCghLrdqlHiHoL40K6D5w"
                }
            ],
            "simplePushApps": [],
            "iosapps": []
        },
        {
            "id": "4028818b3fbec12d013fbee987f20000",
            "name": "New demo",
            "description": "thihgggdfg",
            "pushApplicationID": "54b3b0a5-aa6d-4033-afd7-1d072e1a95ab",
            "masterSecret": "460a4294-b8ff-48ef-b08e-6fa6a4297a5f",
            "developer": "admin",
            "androidApps": [],
            "simplePushApps": [],
            "iosapps": []
        }
    ];


var baseURL = "/ag-push/rest/";

/* Get a list of all the applications */
$.mockjax({
    type: "GET",
    url: baseURL + "applications",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps;
    }
});

/* Get just the one Applicaiton */
$.mockjax({
    type: "GET",
    url: baseURL + "applications/186d02c4-e9ce-4047-90c1-cde93e10c066",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps[1];
    }
});

/* Get the variant */
$.mockjax({
    type: "GET",
    url: baseURL + "applications/186d02c4-e9ce-4047-90c1-cde93e10c066/android/c1d7a28f-70cd-4b08-b827-0ca55e1bb1f3",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps[1].androidApps[0];
    }
});

/* Get the variant */
$.mockjax({
    type: "GET",
    url: baseURL + "applications/c1d7a28f-70cd-4b08-b827-0ca55e1bb1f3/instances/402881883fa49511013fa5d2aeea0007",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps[1].androidApps[0].instances[0];
    }
});

