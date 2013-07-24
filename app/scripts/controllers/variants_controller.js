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
            thee = this,
            applicationData = {
                name: controller.get( "variantName" ),
                description: controller.get( "variantDescription" )
            },
            variantType =  $( "input:checked" ).val(),
            ajaxOptions = {
                url: "/ag-push/rest/applications/" + controller.get( "pushApplicationID" ) + "/" + variantType,
                type: "POST",
                contentType: "application/json"
            };

        if( variantType === "iOS" ) {

            ajaxOptions.success = function() {
                thee.formReset( that );
                that.transitionToRoute( "variants", that.get( "model" ) );
            };

            ajaxOptions.error = function( error ) {
                console.log( "error saving", error );
                switch( error.status ) {
                case 401:
                    that.transitionToRoute( "login" );
                    break;
                default:
                    break;
                }
            };

            ajaxOptions.beforeSubmit = function( formData ) {
                formData.push( { name: "production", value: that.get( "production" ) ? true : false } );
            };

            $( "form" ).ajaxSubmit( ajaxOptions );
        } else {

            switch( variantType ) {
            case "android":
                applicationData.googleKey = controller.get( "googleKey" ); //Needs Validation Here
                break;
            case "simplePush":
                applicationData.pushNetworkURL = controller.get( "pushNetworkURL" );
                break;
            default:
                break;
            }

            ajaxOptions.data = JSON.stringify( applicationData );

            this.saveVariants( controller, ajaxOptions );
        }
    },
    edit: function( controller ) {
        //Make this and add one
        var applicationData = {
                name: controller.get( "name" ),
                description: controller.get( "description" )
            },
            variantType =  controller.get("model").get("vType"),
            ajaxOptions = {
                url: "/ag-push/rest/applications/" + controller.get( "pushApplicationID" ) + "/" + variantType + "/" + controller.get("variantID"),
                type: "PUT",
                contentType: "application/json"
            };

        switch( variantType ) {
        case "android":
            applicationData.googleKey = controller.get( "googleKey" ); //Needs Validation Here
            break;
        case "simplePush":
            applicationData.pushNetworkURL = controller.get( "pushNetworkURL" );
            break;
        case "iOS":
            ajaxOptions.type =  "PATCH";
            break;
        default:
            break;
        }

        ajaxOptions.data = JSON.stringify( applicationData );

        this.saveVariants( controller, ajaxOptions );
    },
    cancel: function( controller ) {
        this.formReset( controller );
        controller.transitionToRoute( "variants" );
    },
    saveVariants: function( controller, ajaxOptions ) {
        var that = controller,
            thee = this;

        ajaxOptions.success = function() {
            thee.formReset( that );
            that.transitionToRoute( "variants", that.get( "model" ) );
        };

        ajaxOptions.error = function( error ) {
            console.log( "error saving", error );
            switch( error.status ) {
            case 401:
                that.transitionToRoute( "login" );
                break;
            default:
                break;
            }
        };

        $.ajax( ajaxOptions );
    },
    formReset: function( controller ) {
        $("form")[0].reset();

        //figure this out
        controller.set( "variantName", "" );
        controller.set( "googleKey", "" );
        controller.set( "passphrase", "" );
        controller.set( "pushNetworkURL", "" );
        controller.set( "production", false );
    }
});

App.VariantsAddController = Ember.ObjectController.extend({
    needs: "variantsIndex"
});

App.VariantsEditController = Ember.ObjectController.extend({
    needs: "variantsIndex"
});
