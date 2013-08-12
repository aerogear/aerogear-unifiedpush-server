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

        if( window.confirm( "Really Delete " + variant.name + " ?" ) ) {
            var things = variant,
                that = this,
                mobileVariantPipe = AeroGear.Pipeline({
                name: "mobileVariant",
                settings: {
                    baseURL: App.baseURL + "rest/applications/",
                    authenticator: App.AeroGear.authenticator,
                    endpoint:  variant.pushApplicationID + "/" + variant.get( "vType" )
                }
            }).pipes.mobileVariant;

            mobileVariantPipe.remove( variant.variantID, {
                success: function() {
                    var content = that.get( "variantList" ),
                        find;

                    find = content.find( function( value ) {
                        return value.variantID === things.variantID;
                    });

                    content.removeObject( find );
                },
                error: function( error ) { // TODO: Maybe Make this a class method?
                    switch( error.status ) {
                    case 401:
                        App.Router.router.transitionToRoute( "login" );
                        break;
                    default:
                        that.send( "error", that, "Error Removing" );
                        break;
                    }
                }
            });
        }
    },
    add: function( controller ) {
        var that = controller,
            thee = this,
            applicationData,
            variantType =  $( "input:checked" ).val(),
            ajaxOptions = {
                url: App.baseURL + "rest/applications/" + controller.get( "pushApplicationID" ) + "/" + variantType,
                type: "POST",
                contentType: "application/json"
            },
            model = controller.get( "model" ),
            hasErrors = false;

        //Reset
        model.validationErrors.clear();

        hasErrors = !model.validateProperty( "name" );
        hasErrors = !model.validateProperty( "description" );

        applicationData = {
            name: controller.get( "name" ),
            description: controller.get( "description" )
        };

        if( variantType === "iOS" ) {
            //run validation
            if( model.validateProperty( "passphrase" ) && model.validateProperty( "certificate" ) )
            {
                ajaxOptions.success = function() {
                    thee.formReset( that );
                    that.transitionToRoute( "variants", that.get( "model" ) );
                };
                ajaxOptions.error = function( error ) {
                    switch( error.status ) {
                    case 401:
                        that.transitionToRoute( "login" );
                        break;
                    default:
                        that.send( "error", that, "Error Saving" );
                        break;
                    }
                };
                ajaxOptions.beforeSubmit = function( formData ) {
                    formData.push( { name: "production", value: that.get( "production" ) ? true : false } );
                };
                $( "form" ).ajaxSubmit( ajaxOptions );
            } else {
                this.send( "error", controller, model.get("validationErrors.allMessages") );
            }
        } else {
            switch( variantType ) {
            case "android":
                if( model.validateProperty( "googleKey" ) ) {
                    applicationData.googleKey = controller.get( "googleKey" );
                } else {
                    hasErrors = true;
                }
                break;
            // case "simplePush":
            //     if( model.validateProperty( "simplePushEndpoint" ) ) {
            //         applicationData.simplePushEndpoint = controller.get( "simplePushEndpoint" );
            //     } else {
            //         hasErrors = true;
            //     }
            //     break;
            default:
                break;
            }
            if( !hasErrors ) {
                ajaxOptions.data = JSON.stringify( applicationData );
                this.saveVariants( controller, ajaxOptions );
            } else {
                this.send( "error", controller, model.get( "validationErrors.allMessages" ) );
            }
        }
    },
    edit: function( controller ) {
        //Make this and add one
        var that = controller,
            thee = this,
            applicationData = {},
            variantType =  controller.get("model").get("vType"),
            ajaxOptions = {
                url: App.baseURL + "rest/applications/" + controller.get( "pushApplicationID" ) + "/" + variantType + "/" + controller.get( "variantID" ),
                type: "PUT",
                contentType: "application/json"
            },
            normalAjax = true,
            file,
            model = controller.get( "model" ),
            hasErrors = false;

        model.validationErrors.clear();
        hasErrors = !model.validateProperty( "name" );
        hasErrors = !model.validateProperty( "description" );

        switch( variantType ) {
        case "android":
            if( model.validateProperty( "googleKey" ) ) {
                applicationData.googleKey = controller.get( "googleKey" ); //Needs Validation Here
            } else {
                hasErrors = true;
            }
            break;
        // case "simplePush":
        //     if( model.validateProperty( "simplePushEndpoint" ) ) {
        //         applicationData.simplePushEndpoint = controller.get( "simplePushEndpoint" );
        //     } else {
        //         hasErrors = true;
        //     }
        //     break;
        case "iOS":
            file = $( "form" ).find( "input[name='certificate']" ).val();
            //Better validation
            if( !file ) {
                ajaxOptions.type =  "PATCH";
            } else {
                if( model.validateProperty( "passphrase" ) && model.validateProperty( "certificate" ) ) {
                    ajaxOptions.success = function() {
                        thee.formReset( that );
                        that.transitionToRoute( "variants", that.get( "model" ) );
                    };

                    ajaxOptions.error = function( error ) {
                        switch( error.status ) {
                        case 401:
                            that.transitionToRoute( "login" );
                            break;
                        default:
                            that.send( "error", that, "Error Saving" );
                            break;
                        }
                    };

                    ajaxOptions.beforeSubmit = function( formData ) {
                        formData.push( { name: "production", value: that.get( "production" ) ? true : false } );
                    };

                    normalAjax = false;

                    $( "form" ).ajaxSubmit( ajaxOptions );
                } else {
                    this.send( "error", controller, model.get( "validationErrors.allMessages" ) );
                }
            }
            break;
        default:
            break;
        }

        if( normalAjax && !hasErrors ) {
            applicationData.name = controller.get( "name" );
            applicationData.description = controller.get( "description" );

            ajaxOptions.data = JSON.stringify( applicationData );

            this.saveVariants( controller, ajaxOptions );
        } else {
            this.send( "error", controller, model.get( "validationErrors.allMessages" ) );
        }
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
            switch( error.status ) {
            case 401:
                that.transitionToRoute( "login" );
                break;
            default:
                that.send( "error", that, "Error Saving" );
                break;
            }
        };

        $.ajax( ajaxOptions );
    },
    formReset: function( controller ) {
        //figure this out better
        controller.set( "variantDescription", "" );
        controller.set( "variantName", "" );
        controller.set( "googleKey", "" );
        controller.set( "passphrase", "" );
        //controller.set( "simplePushEndpoint", "" );
        controller.set( "production", false );
    }
});

App.VariantsAddController = Ember.ObjectController.extend({
    needs: "variantsIndex"
});

App.VariantsEditController = Ember.ObjectController.extend({
    needs: "variantsIndex"
});
