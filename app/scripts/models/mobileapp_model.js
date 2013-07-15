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
        return this.androidVariants.get("content").length;
    }.property(),
    totaliOSVariants: function() {
        return this.iosvariants.get("content").length;
    }.property(),
    totalSimplePushVariants: function() {
        return this.simplePushVariants.get("content").length;
    }.property(),
    totalVariants: function() {
        return this.get( "totalAndroidVariants" ) + this.get( "totaliOSVariants" ) + this.get( "totalSimplePushVariants" );
    }.property(),
    variantList: function() {
        return this.androidVariants.get("content").concat( this.iosvariants.get("content") ).concat( this.simplePushVariants.get("content") );
    }.property(),
    status: function() {
        return ( this.get( "totalVariants" ) ) ? "Active" : "Inactive";
    }.property(),
    descriptionFormatted: function() {
        return this.description.substr( 0, 50 );
    }.property()
});

App.MobileApplication.reopenClass({
    find: function( applicationPushId ) {

        var mobileApplication,
            applicationPipe = App.AeroGear.pipelines.pipes.applications,
            model = this;

        if( applicationPushId ) {
            // Looking for 1
            mobileApplication = App.MobileApplication.create();
        } else {
            //Looking for all
            mobileApplication = Ember.ArrayProxy.create({ content: [] });
        }

        // Need to return a promise for "modelFor" to work.  The formmating looks off here
        applicationPipe.read({
            id: applicationPushId
        }).then( function( response ) {
            if( AeroGear.isArray( response ) ) {
                response.forEach( function( data ) {
                    data.isLoaded = true;
                    data = model._createVariantObject( data );
                    mobileApplication.pushObject( App.MobileApplication.create( data ) );
                });
            } else {
                // Add a loading indicator
                response.isLoaded = true;
                // Loop Through the different Variants to create objects
                mobileApplication.setProperties( model._createVariantObject( response ) );
                console.log( mobileApplication );
            }
        }).then( null, function( error ) {
            console.log( "error with application endpoint", error );
            switch( error.status ) {
            case 401:
                App.Router.router.transitionTo("login");
                break;
            default:
            //that.transitionToRoute( "login" );
            //result.setProperties( { isLoaded: true, error: error } );
                break;
            }
        });

        return mobileApplication;
    },
    _createVariantObject: function( response ) {

        // TODO: DRY this out
        var androidVariants = Ember.ArrayProxy.create({ content: [] }),
            iosVariants = Ember.ArrayProxy.create({ content: [] }),
            simplePushVariants = Ember.ArrayProxy.create({ content: [] });

        response.androidVariants.forEach( function( value ) {
            value.pushApplicationID = response.pushApplicationID;
            androidVariants.pushObject( App.MobileVariant.create( value ) );
        });

        response.androidVariants = androidVariants;

        response.iosvariants.forEach( function( value ) {
            value.pushApplicationID = response.pushApplicationID;
            iosVariants.pushObject( App.MobileVariant.create( value ) );
        });

        response.iosvariants = iosVariants;

        response.simplePushVariants.forEach( function( value ) {
            value.pushApplicationID = response.pushApplicationID;
            simplePushVariants.pushObject( App.MobileVariant.create( value ) );
        });

        response.simplePushVariants = simplePushVariants;

        return response;

    }
});
