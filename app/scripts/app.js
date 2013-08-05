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

window.App = Ember.Application.create({
    LOG_TRANSITIONS: true //remove in production,  for testing and debuging
});

App.baseURL = ( function() {
    var paths = window.location.pathname.split( "/" ),
        pathName = "",
        i = 0;

    for( i; i < paths.length; i++ ) {
        if( paths[ i ].length && paths[ i ].indexOf( ".html" ) < 1 ) {
            pathName += "/";
            pathName += paths[ i ];
        }
    }
    return window.location.protocol + "//" + window.location.host + pathName + "/";
})();
