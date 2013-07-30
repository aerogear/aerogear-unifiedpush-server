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

App.MobileAppsIndexController = Ember.ArrayController.extend({
    sortProperties: [ "pushApplicationID" ],
    sortAscending: true,
    applicationPipe: App.AeroGear.pipelines.pipes.applications,
    edit: function( controller ) {
        var that = controller,
            applicationData = {
                name: controller.get( "name" ),
                id: controller.get( "pushApplicationID" ),
                description: controller.get( "description" )
            };

        this.applicationPipe.save( applicationData, {
            success: function() {
                $("form")[0].reset();
                that.transitionToRoute( "mobileApps" );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    that.transitionToRoute( "login" );
                    break;
                default:
                    that.send( "error", that, "Error Saving" );
                    break;
                }
            }
        });

    },
    cancel: function() {
        //Probably a better way
        $("form")[0].reset();

        this.transitionToRoute( "mobileApps" );
    },
    remove: function( app ) {
        var things = app,
            that = this;

        if( window.confirm( "Really Delete " + app.name + " ?" ) ) {
            this.applicationPipe.remove( app.pushApplicationID, {
                success: function() {
                    var content = that.get("model").get("content"),
                        find;

                    find = content.find( function( value ) {
                        return value.pushApplicationID === things.pushApplicationID;
                    });

                    content.removeObject( find );
                },
                error: function( error ) { // TODO: Maybe Make this a class method?
                    console.log( "error with application endpoint", error );
                    switch( error.status ) {
                    case 401:
                        App.Router.router.transitionToRoute("login");
                        break;
                    default:
                        that.send( "error", that, "Error Saving" );
                        break;
                    }
                }
            });
        }
    },
    totalApps: function() {

        // Compute the total apps for this controller
        return this.get("model").get("content").length;

    }.property( "@each" )
});

/*
    The Controller for adding/editing Mobile apps
*/
App.MobileAppsEditController = Ember.ObjectController.extend({
    needs: "mobileAppsIndex"
});
