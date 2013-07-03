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

App.MobileVariant = Ember.Object.extend({
    totalInstances: function() {
        return this.get("instances").length;
    }.property(),
    type: function() {
        if( this.get( "googleKey" ) ) {
            return "android";
        } else if( this.get( "pushNetworkURL" ) ) {
            return "simplePush";
        } else {
            return "iOS";
        }
    }.property(),
    typeFormatted: function() {
        if( this.get( "googleKey" ) ) {
            return "Android";
        } else if( this.get( "pushNetworkURL" ) ) {
            return "Simple Push";
        } else {
            return "iOS";
        }
    }.property()
});

App.MobileVariant.reopenClass({
    find: function( applicationPushId, variantType, variantApplicationId ) {

        var mobileVariant;

        if( variantApplicationId ) {
            // Looking for 1
            mobileVariant = App.MobileVariant.create();
        } else {
            //Looking for all
            mobileVariant = Ember.ArrayProxy.create({ content: [] });
        }

        this._fetch( mobileVariant, applicationPushId, variantType, variantApplicationId );

        return mobileVariant;
    },
    _ajaxy: function( mobileVariant, applicationPushId, variantType, variantApplicationId ) {

        var mobileVariantPipe = AeroGear.Pipeline({
            name: "mobileVariant",
            settings: {
                baseURL: "/ag-push/rest/applications/",
                authenticator: App.AeroGear.authenticator,
                endpoint:  applicationPushId + "/" + variantType
            }
        }).pipes.mobileVariant;

        return Ember.Deferred.promise( function( promise ) {
            mobileVariantPipe.read({
                id: variantApplicationId,
                success: function( response ) {
                    Ember.run( promise, promise.resolve, response );
                },
                error: function( error ) { // TODO: Maybe Make this a class method?
                    promise.reject( error );
                }
            });
        });
    },
    _fetch: function( mobileVariant, applicationPushId, variantType, variantApplicationId ) {
        this._ajaxy( mobileVariant, applicationPushId, variantType, variantApplicationId ).then( function( response ) {
            if( AeroGear.isArray( response ) ) {
                response.forEach( function( data ) {
                    data.isLoaded = true;
                    data.pushApplicationID = applicationPushId;
                    mobileVariant.pushObject( App.MobileVariant.create( data ) );
                });
            } else {

                // Add a loading indicator
                response.isLoaded = true;
                response.pushApplicationID = applicationPushId;
                // Loop Through the different Variants to create objects
                mobileVariant.setProperties( response );

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
    }
});
