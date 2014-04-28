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
    $scope.$on('loginDone', function () {
        //let's show all the applications
        $scope.applications = pushApplication.query();
    });

    $scope.items = ['item1', 'item2', 'item3'];
    $scope.open = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/dialogs/create-app.html',
            controller: 'modalController',
            resolve: {
                'items': function() { return $scope.items; }
            }
        });
        console.log('modal opened');
        modalInstance.result.then(function (response) {
            $scope.selected = response;
            console.log(response);
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };
};

var modalController = function ($scope, $modalInstance, items) {
    $scope.items = items;
    $scope.selected = {
        item: $scope.items[0]
    };

    $scope.ok = function () {
        $modalInstance.close($scope.selected.item);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};
