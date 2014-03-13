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

App.InstanceIndexController = Ember.ObjectController.extend({
    actions: {
        toggleStatus: function( model ) {
        //TODO: persist to the server
            var installationPipe,
                thee = this,
                that = model,
                data = {
                    //Perhaps a better way?
                    id: model.get( "id" ),
                    alias: model.get( "alias" ),
                    category: model.get( "category" ),
                    deviceToken: model.get( "deviceToken" ),
                    deviceType: model.get( "deviceType" ),
                    operatingSystem: model.get( "operatingSystem" ),
                    osVersion: model.get( "osVersion" ),
                    platform: model.get( "platform" )
                },
                currentStatus = model.get( "enabled" );

            if( currentStatus ) {
                data.enabled = false;
            } else {
                data.enabled = true;
            }

            installationPipe = AeroGear.Pipeline({
                name: "installationPipe",
                settings: {
                    baseURL: App.baseURL + "rest/applications/",
                    authenticator: App.AeroGear.authenticator,
                    endpoint:  model.variantID + "/installations"
                }
            }).pipes.installationPipe;

            installationPipe.save( data, {
                success: function() {
                    Ember.run( this, function() {
                        that.set( "enabled", data.enabled );
                    });
                },
                error: function() {
                    Ember.run( this, function() {
                        thee.send( "error", that, "Error Toggling" );
                        that.set( "enabled", currentStatus );
                    });
                }
            });
        }
    }
});
