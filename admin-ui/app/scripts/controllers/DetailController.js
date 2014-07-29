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

angular.module('upsConsole').controller('DetailController',
  function($rootScope, $scope, $routeParams, $location, $modal, pushApplication, variants, Notifications, breadcrumbs, application, counts, ContextProvider, metrics) {

  /*
   * INITIALIZATION
   */
  $rootScope.application = application;
  $scope.counts = counts;
  breadcrumbs.generateBreadcrumbs();
  $scope.currentLocation = ContextProvider.contextPath();

    metrics.application({id: $routeParams.applicationId}, function(data) {
    angular.forEach(data, function (warning) {
      angular.forEach(application.variants, function (variant) {
        if (warning.variantInformations.length
            && variant.variantID === warning.variantInformations[0].variantID
            && !warning.variantInformations[0].deliveryStatus) {
          variant.hasError = true;
          variant.error = warning.variantInformations[0].reason;
        }
      });
    });
  });

  /*
   * PUBLIC METHODS
   */

  $scope.addVariant = function (variant) {
    var modalInstance = show(variant, 'create-variant.html');
    modalInstance.result.then(function (result) {
      var variantData = variantProperties(result.variant);
      var params = angular.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: result.variant.type
      });

      var createFunction = (variantData instanceof FormData) ? variants.createWithFormData : variants.create;

      createFunction(params, variantData, function (newVariant) {
        var length = application.variants.length;
        for (var i = 0; i < length; i++) {
          if (newVariant.type === application.variants[i].type) {
            break;
          }
        }
        $scope.application.variants.splice(i, 0, newVariant);
        Notifications.success('Successfully created variant');
      }, function () {
        Notifications.error('Something went wrong...');
      });
    });
  };

  $scope.editVariant = function (variant) {
    var modalInstance = show(variant, 'create-variant.html');
    modalInstance.result.then(function (result) {
      var variantDataUpdate = variantProperties(variant);
      var params = angular.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: result.variant.type,
        variantId: result.variant.variantID
      });

      var successCallback = function () {
        Notifications.success('Successfully modified variant');
      };
      var failureCallback = function () {
        Notifications.error('Something went wrong...');
      };

      if (variant.type !== 'ios') {
        variants.update(params, variantDataUpdate, successCallback, failureCallback);
      } else {
        if (variantDataUpdate.certificate) {
          variants.updateWithFormData(params, variantDataUpdate, successCallback, failureCallback);
        } else {
          variants.patch(params, { name: variant.name, description: variant.description}, successCallback, failureCallback);
        }
      }
    });
  };

  $scope.removeVariant = function (variant) {
    var modalInstance = show(variant, 'remove-variant.html');
    modalInstance.result.then(function (result) {
      var params = angular.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: result.variant.type,
        variantId: result.variant.variantID
      });
      variants.remove(params, function () {
        var osVariants = $scope.application.variants;
        osVariants.splice(osVariants.indexOf(variant), 1);
        Notifications.success('Successfully removed variant');
      }, function () {
        Notifications.error('Something went wrong...');
      });
    });
  };
    
  $scope.renewMasterSecret = function () {
    var modalInstance = show(null, 'renew-master-secret.html');
    modalInstance.result.then(function () {
      var app = $scope.application;
      pushApplication.reset({appId: app.pushApplicationID}, function (application) {
        $scope.application.masterSecret = application.masterSecret;
        Notifications.success('Successfully renewed master secret for "' + app.name + '"');
      });
    });
  };

  $scope.renewVariantSecret = function (variant) {
    var modalInstance = show(null, 'renew-variant-secret.html');
    modalInstance.result.then(function () {
      var app = $scope.application;
      var params = {
        appId: app.pushApplicationID,
        variantType: variant.type,
        variantId: variant.variantID
      };
      variants.reset(params, function (updatedVariant) {
        variant.secret = updatedVariant.secret;
        Notifications.success('Successfully renewed secret for variant "' + updatedVariant.name + '"');
      });
    });
  };


  /*
   * PRIVATE FUNCTIONS
   */

  function modalController($scope, $modalInstance, variant) {
    $scope.variant = variant;

    if (!$scope.variant) {
      $scope.variant = {};
    }
    $scope.variant.certificates = [];

    $scope.ok = function (variant) {
      $modalInstance.close({
        variant: variant
      });
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }

  function show(variant, template) {
    return $modal.open({
      templateUrl: 'views/dialogs/' + template,
      controller: modalController,
      resolve: {
        variant: function () {
          return variant;
        }
      }
    });
  }

  function variantProperties(variant) {
    var properties = ['name', 'description'], result = {};
    switch (variant.type) {
    case 'android':
      properties = properties.concat(['projectNumber', 'googleKey']);
      break;
    case 'simplePush':
      properties = properties.concat([]);
      break;
    case 'chrome':
      properties = properties.concat(['clientId', 'clientSecret', 'refreshToken']);
      break;
    case 'ios':
      if (variant.certificates && variant.certificates.length) {
        variant.certificate = variant.certificates[0];
      }
      properties = properties.concat(['production', 'passphrase', 'certificate']);
      var formData = new FormData();
      properties.forEach(function (property) {
        formData.append(property, variant[property] || '');
      });
      return formData;
    case 'windows':
      properties = properties.concat(['sid', 'clientSecret']);
      break;
    default:
      Notifications.error('Unknown variant type ' + variant.type);
    }

    properties.forEach(function (property) {
      result[property] = variant[property];
    });
    return result;
  }

});
