/* JBoss, Home of Professional Open Source
* Copyright Red Hat, Inc., and individual contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/*
    AeroGear related things
*/
App.AeroGear = {};

App.AeroGear.authenticator = AeroGear.Auth({
    name: "authenticator",
    settings: {
        baseURL: App.baseURL + "rest/"
    }
}).modules.authenticator;


// TODO: Do i need this really,  could just create at runtime. Hmmmm.  I'll have to look
App.AeroGear.pipelines = AeroGear.Pipeline([
    {
        name: "applications",
        settings: {
            id: "pushApplicationID",
            baseURL: App.baseURL + "rest/",
            authenticator: App.AeroGear.authenticator
        }
    }
]);
