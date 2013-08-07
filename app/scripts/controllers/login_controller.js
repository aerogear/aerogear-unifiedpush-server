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

App.LoginController = Ember.ObjectController.extend({
    loginIn: true,
    relog: false,
    login: function() {
        var that = this,
            user = this.get( "model" );

        //Validate the form fields with Ember Validations
        user.validateProperty( "loginName" );
        user.validateProperty( "password" );

        if( !user.get( "isValid" ) ) {
            this.send( "error", this, user.get( "validationErrors.allMessages" ) );
        } else {
            // Use AeroGear Authenticator to login
            App.AeroGear.authenticator.login( JSON.stringify( { loginName: this.get( "loginName" ), password: this.get( "password" ) } ), {
                contentType: "application/json",
                success: function() {
                    // Successful Login, now go to /mobileApps
                    that.set( "relog", true );
                    that.transitionToRoute( "mobileApps" );
                },
                error: function( response ) {
                    if( response.status === 403 ) {
                        //change the password
                        that.set( "oldPassword", that.get( "password" ) );
                        that.set( "password", "" );
                        that.set( "loginIn", false );
                    } else {
                        that.send( "error", that, "Login Error" );
                    }
                }
            });
        }
    },
    //Only Temporary until we can get the user create scripts
    other: function() {
        //need to send to the update endpoint
        var that = this,
            password = this.get( "password" ),
            loginName = this.get( "loginName" ),
            user = this.get( "model" ),
            data;

        user.validateProperty( "password" );
        user.validateProperty( "confirmPassword" );

        if( user.get( "isValid" ) ) {
            data = JSON.stringify( { loginName: loginName, password: this.get( "oldPassword" ), newPassword: password } );

            $.ajax({
                url: App.baseURL + "rest/auth/update",
                type: "PUT",
                data: data,
                contentType: "application/json",
                success: function() {
                    // User Must login Again
                    that.set( "password", "" );
                    that.set( "oldPassord", "" );
                    that.set( "loginIn", true );
                    that.set( "relog", true );
                },
                error: function() {
                    that.send( "error", that, "Save Error" );
                }
            });
        } else {
            this.send( "error", this, user.get( "validationErrors.allMessages" ) );
        }
    }
});
