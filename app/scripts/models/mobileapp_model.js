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

App.MobileApplication = Ember.Object.extend( Ember.Validations, {
    validations:{
        name: {
            presence: true
        },
        description: {
            length: {
                maximum: 255
            }
        }
    },
    totalAndroidVariants: function() {
        return this.androidVariants ? this.androidVariants.get( "content" ) : [];
    }.property(),
    totaliOSVariants: function() {
        return this.iosvariants ? this.iosvariants.get( "content" ) : [];
    }.property(),
    totalSimplePushVariants: function() {
        return this.simplePushVariants ? this.simplePushVariants.get( "content" ) : [];
    }.property(),
    totalChromePackagedAppVariants: function() {
        return this.chromePackagedAppVariants ? this.chromePackagedAppVariants.get( "content" ) : [];
    }.property(),
    totalVariants: function() {
        return this.get( "totalAndroidVariants" ).length + this.get( "totaliOSVariants" ).length + this.get( "totalSimplePushVariants" ).length + this.get( "totalChromePackagedAppVariants" ).length;
    }.property(),
    variantList: function() {
        return this.get( "totalAndroidVariants" ).concat( this.get( "totaliOSVariants" ) ).concat( this.get( "totalSimplePushVariants" ) ).concat( this.get( "totalChromePackagedAppVariants" ) );
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
            mobileApplication = Ember.ArrayProxy.create( { content: [] } );
        }

        // Need to return a promise for "modelFor" to work.
        applicationPipe.read({
            id: applicationPushId
        })
        .then( function( response ) {
            Ember.run( this, function() {
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
                }
            });
        })
        .then( null, function( error ) {
            Ember.run( this, function() {
                switch( error.status ) {
                //Possible this may never happen now that we have a beforeModel on the Router
                case 401:
                    //Possibly should be done somewhere else?
                    App.Router.router.transitionTo( "login" );
                    break;
                default:
                    //console.log( "need to do something here" );
                    break;
                }
            });
        });

        return mobileApplication;
    },
    _createVariantObject: function( response ) {

        Object.keys( response ).forEach( function( data ) {
            if( AeroGear.isArray( response[ data ] ) ) {
                var proxy = Ember.ArrayProxy.create( { content: [] } );

                response[ data ].forEach( function( value ) {
                    value.pushApplicationID = response.pushApplicationID;
                    proxy.pushObject( App.MobileVariant.create( value ) );
                });

                response[ data ] = proxy;
            }
        });

        return response;

    }
});
