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
    Extend the Ember.Route Method
*/
App.Route = Ember.Route.extend({
    beforeModel: function( transition ) {
        var that = this,
            loginController = this.controllerFor( "login" );

        loginController.set( "isLogged", true );
        return App.AeroGear.pipelines.pipes.ping.read().then( null, function() {
            Ember.run( function() {
                loginController.set( "isLogged", false );
                loginController.set( "previousTransition", transition );
                that.transitionTo( "login" );
            });
        });
    },
    activate: function(){
        this.send( "clearErrors" );
    }
});
