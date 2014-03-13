var apps = [
    {
        "id": "4028818b3fe37e75013fe38200200000",
        "name": "Cool App 1",
        "description": "A Cool App for testing",
        "pushApplicationID": "a1e09fed-b04f-4588-a9c1-b94df0e49bf7",
        "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
        "developer": "admin",
        "androidVariants": [
            {
                "id": "4028818b3fe37e75013fe3828bfc0001",
                "name": "Android Version",
                "description": "An Android Variant",
                "variantID": "d0b6f731-f591-4045-a174-aa5447c862ba",
                "secret": "45e694dd-f2df-46e3-903b-83a0f690fb7e",
                "developer": "admin",
                "installations": [
                    {
                        "id": "4028818b3fe37e75013fe38a65ae0002",
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
        "simplePushVariants": [],
        "iosvariants": []
    },
    {
        "id": "4028818b3fe37e75013fe38200200000",
        "name": "Cool App 2",
        "description": "A Cool App for testing 2",
        "pushApplicationID": "a1e09fed-b04f-4588-a9c1-b94df0e49bf7",
        "masterSecret": "3ababa8f-cc35-455b-8fc1-311ffe206538",
        "developer": "admin",
        "androidVariants": [
            {
                "id": "4028818b3fe37e75013fe3828bfc0001",
                "name": "Android Version",
                "description": "An Android Variant",
                "variantID": "d0b6f731-f591-4045-a174-aa5447c862ba",
                "secret": "45e694dd-f2df-46e3-903b-83a0f690fb7e",
                "developer": "admin",
                "installations": [
                    {
                        "id": "4028818b3fe37e75013fe38a65ae0002",
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
        "simplePushVariants": [],
        "iosvariants": []
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
    url: baseURL + "applications/a1e09fed-b04f-4588-a9c1-b94df0e49bf7",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps[1];
    }
});

/* Get the variant */
$.mockjax({
    type: "GET",
    url: baseURL + "applications/a1e09fed-b04f-4588-a9c1-b94df0e49bf7/android/d0b6f731-f591-4045-a174-aa5447c862ba",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps[1].androidVariants[0];
    }
});

/* Get the variant */
$.mockjax({
    type: "GET",
    url: baseURL + "applications/d0b6f731-f591-4045-a174-aa5447c862ba/installations/4028818b3fe37e75013fe38a65ae0002",
    responseTime: 750,
    dataType: "json",
    response: function() {
        this.responseText = apps[1].androidVariants[0].installations[0];
    }
});

