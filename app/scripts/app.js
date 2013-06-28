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
    LOG_TRANSITIONS: true
});


// Serializes a form to a JavaScript Object
$.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each( a, function() {
        if ( o[ this.name ] ) {
            if ( !o[ this.name ].push ) {
                o[ this.name ] = [ o[ this.name ] ];
            }
            o[ this.name ].push( this.value || '' );
        } else {
            o[ this.name ] = this.value || '';
        }
    });
    return o;
};
