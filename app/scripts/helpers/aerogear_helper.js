/*
AeroGear related things
*/
//var reallyTheBaseURL = "http://localhost:8080";
var reallyTheBaseURL = "";
App.AeroGear = {};

App.AeroGear.authenticator = AeroGear.Auth({
    name: "authenticator",
    settings: {
        baseURL: reallyTheBaseURL + "/ag-push/rest/"
    }
}).modules.authenticator;

App.AeroGear.pipelines = AeroGear.Pipeline([
    {
        name: "applications",
        settings: {
            id: "pushApplicationID",
            baseURL: reallyTheBaseURL + "/ag-push/rest/",
            authenticator: App.AeroGear.authenticator
        }
    },
    {   //Might not be needed here,  just on device?
        name: "registration",
        settings: {
            baseURL: reallyTheBaseURL + "/ag-push/rest/",
            authenticator: App.AeroGear.authenticator,
            endpoint: "registry/device"
        }
    }
]);
