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

App.MobileVariantInstance = Ember.Object.extend({
    vType: function() {
        var platform;

        switch( this.get( "platform" ) ) {
        case "android":
            platform = "android";
            break;
        case "ios":
            platform = "iOS";
            break;
        case "simplePush":
            platform = "simplePush";
            break;
        default:
            break;
        }

        return platform;
    }.property(),
    status: function(){
        return this.get( "enabled" ) ? "Enabled" : "Disabled";
    }.property( "enabled" )
});

App.MobileVariantInstance.reopenClass({
    find: function( applicationPushId, variantType, variantApplicationId, variantApplicationInstanceId ) {

        var mobileVariantInstance,
            mobileVariantInstancePipe = AeroGear.Pipeline({
                name: "mobileVariantInstance",
                settings: {
                    baseURL: App.baseURL + "rest/applications/",
                    authenticator: App.AeroGear.authenticator,
                    endpoint:  variantApplicationId + "/installations"
                }
            }).pipes.mobileVariantInstance;

        if( variantApplicationInstanceId ) {
            // Looking for 1
            mobileVariantInstance = App.MobileVariantInstance.create();
        } else {
            //Looking for all
            mobileVariantInstance = Ember.ArrayProxy.create( { content: [] } );
        }

        mobileVariantInstancePipe.read({
            id: variantApplicationInstanceId
        })
        .then( function( response ) {
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
        })
        .then( null, function( error ) {
            switch( error.status ) {
            case 401:
                App.Router.router.transitionTo( "login" );
                break;
            default:
                break;
            }
        });

        return mobileVariantInstance;
    }
});
