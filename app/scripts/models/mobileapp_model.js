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
 A Mobile App
*/

App.MobileApplication = Ember.Object.extend({
    totalAndroidVariants: function() {
        return this.androidApps.length;
    }.property(),
    totaliOSVariants: function() {
        return this.iosapps.length;
    }.property(),
    totalSimplePushVariants: function() {
        return this.simplePushApps.length;
    }.property(),
    totalInstances: function() {
        return 100000;
    }.property(),
    variants: function() {
        return this.get( "totalAndroidVariants" ) + this.get( "totaliOSVariants" ) + this.get( "totalSimplePushVariants" );
    }.property()
});

App.MobileApplication.reopenClass({
    find: function( applicationPushId ) {

        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            mobileApplication;

        if( applicationPushId ) {
            // Looking for 1
            mobileApplication = App.MobileApplication.create();
        } else {
            //Looking for all
            mobileApplication = Ember.ArrayProxy.create({ content: [] });
        }

        applicationPipe.read({
            id: applicationPushId,
            success: function( response ) {
                if( AeroGear.isArray( response ) ) {
                    response.forEach( function( data ) {
                        data.isLoaded = true;
                        mobileApplication.pushObject( App.MobileApplication.create( data ) );
                    });
                } else {
                    response.isLoaded = true;
                    mobileApplication.setProperties( response );
                }
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

        return mobileApplication;
    },
    remove: function( applicationPushId ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications;//,
            //that = this;

        applicationPipe.remove({
            id: applicationPushId,
            success: function( response ) {
                console.log( "remove response", response );
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
    }
});
