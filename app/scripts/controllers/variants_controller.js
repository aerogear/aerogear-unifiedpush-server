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
    needs: "application",
    showReset: false,
    showDelete: false,
    showSend: false,
    disableVariantDeleteButton: true,
    testMessage: '{"message":{"alert":"hello"}}',
    outputTestMessage: Ember.computed.oneWay("testMessage"),
    enableVariantDelete: function() {
        var variant = this.get( "content" ).get("variantToDelete" );
        if ( variant ) {
            if ( this.get( "content" ).get( "confirmVariantName" ) === variant.name ) {
                this.set( "disableVariantDeleteButton", false );
            }
            else {
                this.set( "disableVariantDeleteButton", true );
            }
        }
    }.observes("confirmVariantName" ),
    actions: {
        toggleVariantDeleteOverlay: function( variant ) {
            if ( this.get( "showVariantDelete" ) ) {
                this.set( "showVariantDelete", false );
                this.set("confirmVariantName","");
            }
            else {
                this.set( "showVariantDelete", true );
                this.get( "model" ).set( "variantToDelete", variant );
            }
        },
        toggleResetOverlay: function() {
            if ( this.get( "showReset" ) ) {
                this.set( "showReset", false );
            }
            else {
                this.set( "showReset", true );
            }
        },
        toggleSendOverlay:function() {
            if ( this.get( "showSend" ) ) {
                this.set( "showSend", false );
            }
            else {
                this.set( "showSend", true );
            }
        },
        remove: function() {


            var things = this.get( "content" ).get("variantToDelete" ),
                that = this,
                mobileVariantPipe = AeroGear.Pipeline( {
                    name: "mobileVariant",
                    settings: {
                        baseURL: App.baseURL + "rest/applications/",
                        authenticator: App.AeroGear.authenticator,
                        endpoint: things.pushApplicationID + "/" + things.get( "vType" )
                    }
                } ).pipes.mobileVariant;

            mobileVariantPipe.remove( things.variantID, {
                success: function () {
                    Ember.run( this, function() {
                        var content = that.get( "variantList" ),
                            find;

                        find = content.find( function ( value ) {
                            return value.variantID === things.variantID;
                        } );
                        that.set("confirmVariantName","");
                        content.removeObject( find );
                    });
                },
                error: function ( error ) { // TODO: Maybe Make this a class method?
                    Ember.run(this, function(){
                        switch ( error.status ) {
                        case 401:
                            App.Router.router.transitionToRoute( "login" );
                            break;
                        default:
                            that.send( "error", that, "Error Removing" );
                            break;
                        }
                    });
                }
            });
            this.send( "toggleVariantDeleteOverlay" );
        },
        add: function( controller ) {
            var that = controller,
                thee = this,
                applicationData,
                variantType = $( "input[name=platform]:checked" ).val(),
                ajaxOptions = {
                    url: App.baseURL + "rest/applications/" + controller.get( "pushApplicationID" ) + "/" + variantType,
                    type: "POST",
                    contentType: "application/json"
                },
                model = controller.get( "model" ),
                hasErrors = false;
            this.get('controllers.application' ).set( "isProcessing", true );

            //Reset
            model.validationErrors.clear();

            hasErrors = !model.validateProperty( "name" );

            applicationData = {
                name: controller.get( "name" ),
                description: controller.get( "description" )
            };

            if( variantType === "iOS" ) {
                //run validation
                if( model.validateProperty( "passphrase" ) && model.validateProperty( "certificate" ) )
                {
                    ajaxOptions.success = function() {
                        Ember.run( this, function() {
                            thee.send( "formReset", that );
                            that.transitionToRoute( "variants", that.get( "model" ) );
                        });
                    };
                    ajaxOptions.error = function( error ) {
                        Ember.run( this, function() {
                            switch( error.status ) {
                            case 401:
                                that.transitionToRoute( "login" );
                                break;
                            default:
                                that.send( "error", that, "Error Saving" );
                                break;
                            }
                        });
                    };

                    $( "form" ).ajaxSubmit( ajaxOptions );
                } else {
                    this.send( "error", controller, model.get( "validationErrors.allMessages" ) );
                }
            } else {
                switch( variantType ) {
                case "android":
                    if ( model.validateProperty( "googleKey" ) ) {
                        applicationData.googleKey = controller.get( "googleKey" );
                        applicationData.projectNumber = controller.get( "projectNumber" );
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
                case "chrome":
                    if( model.validateProperty( "clientId" ) && model.validateProperty( "clientSecret" ) && model.validateProperty( "refreshToken" ) ) {
                        applicationData.clientId = controller.get( "clientId" );
                        applicationData.clientSecret = controller.get( "clientSecret" );
                        applicationData.refreshToken = controller.get( "refreshToken" );
                    } else {
                        hasErrors = true;
                    }
                    break;
                default:
                    break;
                }
                if( !hasErrors ) {
                    ajaxOptions.data = JSON.stringify( applicationData );
                    this.send( "saveVariants", controller, ajaxOptions );
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
                variantType = controller.get( "model" ).get( "vType" ),
                ajaxOptions = {
                    url: App.baseURL + "rest/applications/" + controller.get( "pushApplicationID" ) + "/" + variantType + "/" + controller.get( "variantID" ),
                    type: "PUT",
                    contentType: "application/json"
                },
                normalAjax = true,
                file,
                model = controller.get( "model" ),
                hasErrors = false;
            this.get('controllers.application' ).set( "isProcessing", true );
            model.validationErrors.clear();
            hasErrors = !model.validateProperty( "name" );

            switch( variantType ) {
            case "android":
                if ( model.validateProperty( "googleKey" ) ) {
                    applicationData.googleKey = controller.get( "googleKey" ); //Needs Validation Here
                    applicationData.projectNumber = controller.get( "projectNumber" );
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
            case "chrome":
                if( model.validateProperty( "clientId" ) && model.validateProperty( "clientSecret" ) && model.validateProperty( "refreshToken" ) ) {
                    applicationData.clientId = controller.get( "clientId" );
                    applicationData.clientSecret = controller.get( "clientSecret" );
                    applicationData.refreshToken = controller.get( "refreshToken" );
                } else {
                    hasErrors = true;
                }
                break;
            case "iOS":
                file = $( "form" ).find( "input[name='certificate']" ).val();
                //Better validation
                if ( !file ) {
                    ajaxOptions.type = "PATCH";
                } else {
                    if ( model.validateProperty( "passphrase" ) && model.validateProperty( "certificate" ) ) {
                        ajaxOptions.success = function() {
                                thee.send( "formReset", that );
                                that.transitionToRoute( "variants", that.get( "model" ) );
                            };

                        ajaxOptions.error = function( error ) {
                                switch ( error.status ) {
                                case 401:
                                    that.transitionToRoute( "login" );
                                    break;
                                default:
                                    that.send( "error", that, "Error Saving" );
                                    break;
                                }
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

                this.send( "saveVariants", controller, ajaxOptions );
            } else {
                this.send( "error", controller, model.get( "validationErrors.allMessages" ) );
            }
        },
        cancel: function( controller ) {
            this.send( "formReset", controller );
            controller.transitionToRoute( "variants" );
        },
        saveVariants: function( controller, ajaxOptions ) {
            var that = controller,
                thee = this;
            this.get('controllers.application' ).set( "isProcessing", true );
            ajaxOptions.success = function() {
                Ember.run( this, function() {
                    thee.send( "formReset", that );
                    that.transitionToRoute( "variants", that.get( "model" ) );
                });
            };

            ajaxOptions.error = function( error ) {
                Ember.run( this, function() {
                    switch( error.status ) {
                    case 401:
                        that.transitionToRoute( "login" );
                        break;
                    default:
                        that.send( "error", that, "Error Saving" );
                        break;
                    }
                });
            };

            $.ajax( ajaxOptions );
        },
        resetMaster: function( app ) {
            var that = this,
                mobileAppPipe = AeroGear.Pipeline( {
                    name: "mobileApp",
                    settings: {
                        baseURL: App.baseURL + "rest/applications/",
                        authenticator: App.AeroGear.authenticator,
                        endpoint: app.pushApplicationID
                    }
                }).pipes.mobileApp;
            this.get('controllers.application' ).set( "isProcessing", true );
            mobileAppPipe.save( {id: "reset"}, {
                success: function( data ) {
                    Ember.run( this, function() {
                        app.set( "masterSecret", data.masterSecret );
                        that.get('controllers.application' ).set( "isProcessing", false );
                    });
                },
                error: function( error ) { // TODO: Maybe Make this a class method?
                    Ember.run( this, function() {
                        switch ( error.status ) {
                        case 401:
                            App.Router.router.transitionToRoute( "login" );
                            break;
                        default:
                            that.send( "error", that, "Error Resetting" );
                            break;
                        }
                    });
                }
            });
            this.send( "toggleResetOverlay" );
        },
        formReset: function( controller ) {
            //figure this out better
            controller.set( "variantDescription", "" );
            controller.set( "variantName", "" );
            controller.set( "googleKey", "" );
            controller.set( "projectNumber", "" );
            controller.set( "passphrase", "" );
            //controller.set( "simplePushEndpoint", "" );
            controller.set( "production", false );
        },
        sendTestMessage: function( message ) {
            var that = this;
            $.ajax
            ({
                contentType: "application/json",
                type: "POST",
                url: 'rest/sender',
                dataType: 'json',
                username: this.get("content").get("pushApplicationID"),
                password: this.get("content").get("masterSecret"),
                data: message,
                complete: function (){
                    that.set("showSend", false);
                }
            });
        }
    }
});

App.VariantsAddController = Ember.ObjectController.extend( {
    needs: ["variantsIndex","application"]
});

App.VariantsEditController = Ember.ObjectController.extend( {
    needs: ["variantsIndex","application"]
});

App.VariantsComposeController = Ember.ObjectController.extend( {
    needs: ["variantsIndex","application"],
    options: ["Everyone", "Section"],
    conditions: ["Variant", "DeviceType", "alias","category"],
    dummyVariant: ["iPhone Premium", "Android Free"],
    showCondition: false,
    selectedOption: null,
    selectedCondition: null,
    showConditions: function() {
     if(this.get("selectedOption")=="Section"){
       this.set("showCondition",true);
     }

    }.observes("selectedOption"),
    actions: {
        addCriteria: function(){
            $('#criterias tr:last').after('<tr><td><select><option value="Variant">Variant</option><option value="DeviceType">DeviceType</option><option value="Alias">Alias</option><option value="Category">Category</option></select></td><td><input type="text" </td></tr>');
        }
    }
});
