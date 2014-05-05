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

function DetailController($rootScope, $scope, $routeParams, $window, $modal, pushApplication, variants, Notifications) {

    /*
     * INITIALIZATION
     */
    onLoginDone($rootScope, $scope, function() {
        pushApplication.get({appId: $routeParams.applicationId}, function(application) {
            $scope.application = application;
            var href = $window.location.href;
            $scope.currentLocation = href.substring(0, href.indexOf('#'));
        });
    });


    /*
     * PUBLIC METHODS
     */

    $scope.addVariant = function (variant) {
        var modalInstance = show(variant, null, 'create-variant.html');
        modalInstance.result.then(function (result) {
            var variant = result.variant;
            var variantType = result.variantType;
            var params = $.extend({}, {
                appId: $scope.application.pushApplicationID,
                variantType: variantType
            });

            variants.create(params, variant, function(newVariant) {
                var osVariants = $scope.application[variantType + 'Variants'];
                osVariants.push(newVariant);
                Notifications.success('Successfully created variant');
            }, function() {
                Notifications.danger('Something went wrong...');
            });
        });
    };

    $scope.editVariant = function(variant, variantType) {
        var modalInstance = show(variant, variantType, 'create-variant.html');
        modalInstance.result.then(function (result) {
            var params = $.extend({}, {
                appId: $scope.application.pushApplicationID,
                variantType: variantType,
                variantId: result.variant.variantID
            });
            var variantUpdate = {
                name: variant.name,
                description: variant.description,
                projectNumber: variant.projectNumber,
                googleKey: variant.googleKey
            };

            variants.update(params, variantUpdate, function(variant) {
                Notifications.success('Successfully modified variant');
            }, function() {
                Notifications.danger('Something went wrong...');
            });

        });
    };

    $scope.removeVariant = function(variant, variantType) {
        var modalInstance = show(variant, variantType, 'remove-variant.html');
        modalInstance.result.then(function (result) {
            var params = $.extend({}, {
                appId: $scope.application.pushApplicationID,
                variantType: variantType,
                variantId: result.variant.variantID
            });
            variants.remove(params, function() {
                var osVariants = $scope.application[variantType + 'Variants'];
                osVariants.splice(osVariants.indexOf(variant), 1);
                Notifications.success('Successfully removed variant');
            }, function() {
                Notifications.danger('Something went wrong...');
            });
        });
    };

    /*
     * PRIVATE FUNCTIONS
     */

    function modalController($scope, $modalInstance, variant, variantType) {
        $scope.variant = variant;
        $scope.variantType = variantType;
        $scope.ok = function (variant, variantType) {
            $modalInstance.close({
                variant: variant,
                variantType: variantType
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }

    function show(variant, variantType, template) {
        return $modal.open({
            templateUrl: 'views/dialogs/' + template,
            controller: modalController,
            resolve: {
                variant: function () {
                    return variant;
                },
                variantType: function () {
                    return variantType;
                }
            }
        });
    }

//    function createAlert(msg, type) {
//        $scope.alerts.push({type: type || 'success', msg: msg});
//        setTimeout(function () {
//            $scope.alerts.splice(0, 1);
//            $scope.$apply();
//        }, 4000)
//    }

}
