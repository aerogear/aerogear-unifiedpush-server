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
 A Mobile app variant
*/

App.MobileVariant = Ember.Object.extend( Ember.Validations, {
    validations: {
        name: {
            presence: true
        },
        description: {
            length: {
                maximum: 255
            }
        },
        googleKey: {
            presence: true
        },
        passphrase: {
            presence: true
        },
        certificate: {
            presence: true
        }
    },
    totalInstances: function() {
        return this.get( "installations" ).length;
    }.property(),
    vType: function() {
        if( this.get( "type" ) === "ANDROID" ) {
            return "android";
        } else if( this.get( "type" ) === "SIMPLE_PUSH" ) {
            return "simplePush";
        } else {
            return "iOS";
        }
    }.property( "type" ),
    isAndroid: function() {
        return this.get( "vType" ) === "android";
    }.property( "vType" ),
    isiOS: function() {
        return this.get( "vType" ) === "iOS";
    }.property( "vType" ),
    isSimplePush: function() {
        return this.get( "vType" ) === "simplePush";
    }.property( "vType" ),
    typeFormatted: function() {
        return this.get( "vType" ); //get rid of this method?
        // if( this.get( "googleKey" ) ) {
        //     return "Android";
        // } else if( this.get( "simplePushEndpoint" ) ) {
        //     return "SimplePush";
        // } else {
        //     return "iOS";
        // }
    }.property(),
    googleProjectNumber: function() {
        return this.get( "projectNumber" ) ? this.get( "projectNumber" ) : "N/A";
    }.property()
});

App.MobileVariant.reopenClass({
    find: function( applicationPushId, variantType, variantApplicationId ) {

        var mobileVariant,
            mobileVariantPipe = AeroGear.Pipeline({
                name: "mobileVariant",
                settings: {
                    baseURL: App.baseURL + "rest/applications/",
                    authenticator: App.AeroGear.authenticator,
                    endpoint:  applicationPushId + "/" + variantType
                }
            }).pipes.mobileVariant,
            model = this;

        if( variantApplicationId ) {
            // Looking for 1
            mobileVariant = App.MobileVariant.create();
        } else {
            //Looking for all
            mobileVariant = Ember.ArrayProxy.create( { content: [] } );
        }

        mobileVariantPipe.read({
            id: variantApplicationId
        })
        .then( function( response ) {
            if( AeroGear.isArray( response ) ) {
                response.forEach( function( data ) {
                    data.isLoaded = true;
                    data.pushApplicationID = applicationPushId;

                    //do the instance thing
                    data = model._createVariantInstanceObject( data );

                    mobileVariant.pushObject( App.MobileVariant.create( data ) );
                });
            } else {

                // Add a loading indicator
                response.isLoaded = true;
                response.pushApplicationID = applicationPushId;
                response = model._createVariantInstanceObject( response );
                mobileVariant.setProperties( response );

            }
        })
        .then( null, function( error ) {
            switch( error.status ) {
            //Possible this may never happen now that we have a beforeModel on the Router
            case 401:
                App.Router.router.transitionTo( "login" );
                break;
            default:
                //result.setProperties( { isLoaded: true, error: error } );
                break;
            }
        });

        return mobileVariant;
    },
    _createVariantInstanceObject: function( response ) {

        var variantInstance = Ember.ArrayProxy.create( { content: [] } );

        response.installations.forEach( function( value ) {
            value.pushApplicationID = response.pushApplicationID;
            value.variantID = response.variantID;
            variantInstance.pushObject( App.MobileVariantInstance.create( value ) );
        });

        response.installations = variantInstance;

        return response;

    }
});
