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
    /*remove: function( app ){
        App.MobileApplication.remove( app );
    },*/
    remove: function( app ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            things = app,
            that = this;

        applicationPipe.remove( app.pushApplicationID, {
            success: function( response ) {
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
                    App.Router.router.transitionTo("login");
                    break;
                default:
                    //that.transitionTo( "login" );
                    //result.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });
    },
    totalApps: function() {

        // Compute the total apps for this controller
        return this.get("model").get("content").length;

    }.property( "@each" )
});

/*
    The Mobile Apps Controller. Put "Global Events" Here
*/
App.MobileAppsController = Ember.Controller.extend({
    logout: function() {
        var that = this;
        App.AeroGear.authenticator.logout({
            contentType: "application/json",
            success: function( success ) {
                console.log( "Logged Out", success );
                that.transitionTo( "login" );
            },
            error: function( error ) {
                console.log( "Error Logging Out", error );
                that.transitionTo( "login" );
            }
        });
    }
});


// DRY THIS OUT
App.MobileAppsAddController = Ember.Controller.extend({
    add: function() {
        var applicationData = {
            name: this.get( "name" ),
            description: this.get( "description" )
        };

        this.saveMobileApplication( applicationData );
    },
    // Move this to the Model?
    saveMobileApplication: function( applicationData ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            that = this;

        applicationPipe.save( applicationData, {
            success: function() {
                that.transitionToRoute( "mobileApps" );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    that.transitionTo( "login" );
                    break;
                default:
                    break;
                }
            }
        });
    }
});

/*
    The Controller for adding/editing Mobile apps
*/
App.MobileAppsEditController = Ember.ObjectController.extend({
    edit: function() {
        var applicationData = {
            name: this.get( "name" ),
            id: this.get( "pushApplicationID" ),
            description: this.get( "description" )
        };

        this.saveMobileApplication( applicationData );
    },
    // Move this to the Model?
    saveMobileApplication: function( applicationData ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            that = this;

        applicationPipe.save( applicationData, {
            success: function( response ) {
                console.log( "Save Mobile Application", response );
                that.transitionToRoute( "mobileApps" );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    that.transitionTo( "login" );
                    break;
                default:
                    break;
                }
            }
        });
    },
    cancel: function() {
        this.transitionToRoute( "mobileApps" );
        console.log( "cancel" );
    }
});
