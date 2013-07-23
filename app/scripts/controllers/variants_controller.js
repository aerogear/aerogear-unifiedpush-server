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

App.VariantsIndexController = Ember.ObjectController.extend({
    remove: function( variant ) {
        var things = variant,
            that = this,
            mobileVariantPipe = AeroGear.Pipeline({
            name: "mobileVariant",
            settings: {
                baseURL: "/ag-push/rest/applications/",
                authenticator: App.AeroGear.authenticator,
                endpoint:  variant.pushApplicationID + "/" + variant.get( "vType" )
            }
        }).pipes.mobileVariant;

        mobileVariantPipe.remove( variant.variantID, {
            success: function() {
                var content = that.get("variantList"),
                    find;

                find = content.find( function( value ) {
                    return value.variantID === things.variantID;
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
                    //that.transitionToRoute( "login" );
                    //result.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });
    },
    add: function( controller ) {
        var that = controller,
            applicationData = {
                name: controller.get( "variantName" ),
                description: controller.get( "variantDescription" )
            },
            applicationPushId = controller.get( "pushApplicationID" ),
            variantType =  $( "input:checked" ).val(),
            url = "/ag-push/rest/applications/" + applicationPushId + "/" + variantType,
            type = "POST",
            data,
            contentType = "application/json";

        if( variantType === "iOS" ) {

            $( "form" ).ajaxSubmit({
                beforeSubmit: function( formData ) {
                    formData.push( { name: "production", value: that.get( "production" ) ? true : false } );
                },
                type: "POST",
                url: url,
                success: function() {
                    that.transitionToRoute( "variants", that.get( "model" ) );
                },
                error: function( error ) {
                    console.log( "error saving", error );
                    switch( error.status ) {
                    case 401:
                        that.transitionToRoute( "login" );
                        break;
                    default:
                        break;
                    }
                }
            });
        } else {

            switch( variantType ) {
            case "android":
                applicationData.googleKey = controller.get( "googleKey" ); //Needs Validation Here
                data = JSON.stringify( applicationData );
                break;
            case "simplePush":
                applicationData.pushNetworkURL = controller.get( "pushNetworkURL" );
                data = JSON.stringify( applicationData );
                break;
            default:
                break;
            }

            // TODO: use aerogear pipes once we get multi part support
            // TODO: could probably switch this to aerogear since we are doing ios seperate
            $.ajax({
                "url": url,
                "type": type,
                "contentType": contentType,
                "data": data,
                success: function() {
                    that.transitionToRoute( "variants", that.get( "model" ) );
                },
                error: function( error ) {
                    console.log( "error saving", error );
                    switch( error.status ) {
                    case 401:
                        that.transitionToRoute( "login" );
                        break;
                    default:
                        break;
                    }
                }
            });
        }
    },
    edit: function( controller ) {
        //Make this and add one
        var that = controller,
            applicationData = {
                name: controller.get( "name" ),
                description: controller.get( "description" )
            },
            applicationPushId = controller.get( "pushApplicationID" ),
            variantType =  $( "input:checked" ).val(),
            url = "/ag-push/rest/applications/" + applicationPushId + "/" + variantType + "/" + controller.get("variantID"),
            type = "PUT",
            data,
            contentType = "application/json";

        switch( variantType ) {
        case "android":
            applicationData.googleKey = controller.get( "googleKey" ); //Needs Validation Here
            break;
        case "simplePush":
            applicationData.pushNetworkURL = controller.get( "pushNetworkURL" );
            break;
        case "iOS":
            type =  "PATCH";
            break;
        default:
            break;
        }

        data = JSON.stringify( applicationData );

        $.ajax({
            "url": url,
            "type": type,
            "contentType": contentType,
            "data": data,
            success: function() {
                that.transitionToRoute( "variants", that.get( "model" ) );
            },
            error: function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    that.transitionToRoute( "login" );
                    break;
                default:
                    break;
                }
            }
        });

    },
    cancel: function( controller ) {
        //Probably a better way
        controller.set( "name", "" );
        controller.set( "description", "" );

        controller.transitionToRoute( "variants" );
    }
});

App.VariantsAddController = Ember.ObjectController.extend({
    needs: "variantsIndex"
});

App.VariantsEditController = Ember.ObjectController.extend({
    needs: "variantsIndex"
});
