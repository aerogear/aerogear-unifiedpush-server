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
 A Instance of Mobile App Variant
*/

App.MobileVariantInstance = Ember.Object.extend();

App.MobileVariantInstance.reopenClass({
    find: function( applicationPushId, variantType, variantApplicationId, variantApplicationInstanceId ) {

        var mobileVariantInstance;

        if( variantApplicationInstanceId ) {
            // Looking for 1
            mobileVariantInstance = App.MobileVariantInstance.create();
        } else {
            //Looking for all
            mobileVariantInstance = Ember.ArrayProxy.create({ content: [] });
        }

        this._fetch( mobileVariantInstance, applicationPushId, variantType, variantApplicationId, variantApplicationInstanceId );

        return mobileVariantInstance;
    },
    _ajaxy: function( mobileVariantInstance, applicationPushId, variantType, variantApplicationId, variantApplicationInstanceId ) {

        var mobileVariantInstancePipe = AeroGear.Pipeline({
            name: "mobileVariantInstance",
            settings: {
                baseURL: "/ag-push/rest/applications/",
                authenticator: App.AeroGear.authenticator,
                endpoint:  variantApplicationId + "/instances"
            }
        }).pipes.mobileVariantInstance;

        return Ember.Deferred.promise( function( promise ) {
            mobileVariantInstancePipe.read({
                id: variantApplicationInstanceId,
                success: function( response ) {
                    Ember.run( promise, promise.resolve, response );
                },
                error: function( error ) { // TODO: Maybe Make this a class method?
                    promise.reject( error );
                }
            });
        });
    },
    _fetch: function( mobileVariantInstance, applicationPushId, variantType, variantApplicationId, variantApplicationInstanceId ) {
        this._ajaxy( mobileVariantInstance, applicationPushId, variantType, variantApplicationId, variantApplicationInstanceId ).then( function( response ) {
            if( AeroGear.isArray( response ) ) {
                response.forEach( function( data ) {
                    data.isLoaded = true;
                    data.pushApplicationID = applicationPushId;
                    data.variantID = variantApplicationId;
                    data.variantType = variantType;
                    mobileVariantInstance.pushObject( App.MobileVariant.create( data ) );
                });
            } else {

                // Add a loading indicator
                response.isLoaded = true;
                response.pushApplicationID = applicationPushId;
                response.variantID = variantApplicationId;
                response.variantType = variantType;
                // Loop Through the different Variants to create objects
                mobileVariantInstance.setProperties( response );

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
