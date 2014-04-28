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

function MainController($scope, $modal, pushApplication) {

    // will fail when user is not logged in
    $scope.applications = pushApplication.query();

    $scope.$on('loginDone', function () {
        //let's show all the applications
        $scope.applications = pushApplication.query();
    });

    function show(application) {
        var modalInstance = $modal.open({
            templateUrl: 'views/dialogs/create-app.html',
            controller: 'modalController',
            resolve: {
                application: function () {
                    return application;
                }
            }

        });
        return modalInstance;
    }

    $scope.open = function (application) {
        var modalInstance = show(application);
        modalInstance.result.then(function (application) {
            pushApplication.create(application, function(newApp) {
                $scope.applications.push(newApp);
            });
        });
    };

    $scope.edit = function(application) {
        var modalInstance = show(application);
        modalInstance.result.then(function (application) {
            pushApplication.update(application);
        });
    }
};

function modalController($scope, $modalInstance, application) {
    $scope.application = application;
    $scope.ok = function (application) {
        $modalInstance.close(application);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};
