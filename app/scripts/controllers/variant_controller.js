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
App.VariantIndexController = Ember.ObjectController.extend({
    needs: "application",
    showReset: false,
    actions: {
        toggleResetOverlay: function(){
            if(this.get( "showReset" )){
                this.set( "showReset", false );
            }
            else{
                this.set( "showReset", true );
            }
        },
        reset: function( variant ) {
            this.get('controllers.application' ).set( "isProcessing", true );
            var things = variant,
                that = this,
                mobileVariantPipe = AeroGear.Pipeline({
                    name: "mobileVariant",
                    settings: {
                        baseURL: App.baseURL + "rest/applications/",
                        authenticator: App.AeroGear.authenticator,
                        endpoint:  variant.pushApplicationID + "/" + variant.get( "vType" ) + "/" + variant.variantID
                    }
                }).pipes.mobileVariant;
            mobileVariantPipe.save( {id: "reset" }, {
                success: function( data ) {
                    Ember.run( this, function() {
                        things.set("secret",data.secret);
                        that.get('controllers.application' ).set( "isProcessing", false );
                    });

                },
                error: function( error ) { // TODO: Maybe Make this a class method?
                    Ember.run( this, function() {
                        switch( error.status ) {
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
        }
    }
});

App.VariantSnippetsController = Ember.ObjectController.extend( {
    needs: ["variantIndex","application"],
    showCordova: false,
    showNative: true,
    actions: {
        toggleNative: function(){
            this.set( "showNative", true );
            this.set( "showCordova", false );
        },
        toggleCordova: function() {
            this.set( "showNative", false );
            this.set( "showCordova", true );
        }
    }
});