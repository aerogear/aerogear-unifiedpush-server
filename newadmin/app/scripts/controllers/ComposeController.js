/*
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

function ComposeController($rootScope, $scope, $routeParams, $window, $modal, pushApplication, variants, Notifications, compose) {

    /*
     * INITIALIZATION
     */

        pushApplication.get({appId: $routeParams.applicationId}, function(application) {
            $scope.application = application;
            var href = $window.location.href;
            $scope.currentLocation = href.substring(0, href.indexOf('#'));
        });



    $scope.sendMessage = function () {
        var pushData = {"message":{"sound":"default","alert":$scope.testMessage}};
        $.ajax
        ({
            contentType: "application/json",
            type: "POST",
            url: 'rest/sender',
            dataType: 'json',
            username: $scope.application.pushApplicationID,
            password: $scope.application.masterSecret,
            data: JSON.stringify(pushData),
            complete: function (){
                //controller.set("testMessage", "");
            }
        });
    }
}
