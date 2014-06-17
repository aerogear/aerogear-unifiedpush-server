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
  function($rootScope, $scope, $routeParams, $location, $modal, pushApplication, variants, Notifications, breadcrumbs, variantService) {

  /*
   * INITIALIZATION
   */
  pushApplication.get({appId: $routeParams.applicationId}, function (application) {
    $rootScope.application = application;
    breadcrumbs.generateBreadcrumbs();
    var href = $location.absUrl();
    $scope.currentLocation = href.substring(0, href.indexOf('#'));
  });
  pushApplication.count({appId: $routeParams.applicationId}, function (counts) {
    $scope.counts = counts;
  });


  /*
   * PUBLIC METHODS
   */
  $scope.editVariant = function (variant) {
    $rootScope.variant = variant;
    breadcrumbs.generateBreadcrumbs();
    $location.path('/variant/' + $rootScope.application.pushApplicationID + '/' + variantService.variantType(variant.type) +
      '/' + variant.variantID);
  };

  $scope.removeVariant = function (variant, variantType) {
    var modalInstance = show(variant, variantType, 'remove-variant.html');
    modalInstance.result.then(function (result) {
      var params = $.extend({}, {
        appId: $rootScope.application.pushApplicationID,
        variantType: variantService.variantEndpoint(variantType),
        variantId: result.variant.variantID
      });
      variants.remove(params, function () {
        var osVariants = variantService.getOsVariants($rootScope.application, variantType);
        osVariants.splice(osVariants.indexOf(variant), 1);
        Notifications.success('Successfully removed variant');
      }, function () {
        Notifications.error('Something went wrong...');
      });
    });
  };

  $scope.renewMasterSecret = function () {
    var modalInstance = show(null, null, 'renew-secret.html');
    modalInstance.result.then(function () {
      var app = $scope.application;
      pushApplication.reset({appId: app.pushApplicationID}, function (application) {
        $scope.application.masterSecret = application.masterSecret;
        Notifications.success('Successfully renewed master secret for "' + app.name + '"');
      });
    });
  };


  /*
   * PRIVATE FUNCTIONS
   */

  function modalController($scope, $modalInstance, variant, variantType) {
    $scope.variant = variant;
    $scope.variantType = variantType;

    if (!$scope.variant) {
      $scope.variant = {};
    }
    $scope.variant.certificates = [];

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
});


angular.module('upsConsole').controller('VariantController', function ($rootScope, $scope, $routeParams, $location,
                                                                       pushApplication, variants, Notifications,
                                                                       breadcrumbs, variantService) {

  $scope.variantType = $routeParams.variantType;

  if (typeof $rootScope.variant === 'undefined') {
    if (typeof $routeParams.variantId !== 'undefined') {
      var params = {
        appId: $routeParams.applicationId,
        variantType: $routeParams.variantType,
        variantId: $routeParams.variantId
      };
      variants.get(params, function (variant) {
        $rootScope.variant = variant;
        breadcrumbs.generateBreadcrumbs();
      });
    } else {
      $rootScope.variant = {};
    }
  }

  $scope.saveVariant = function (variant, variantType) {
    var variantData = variantProperties(variant, variantType);
    var params = {
      appId: $routeParams.applicationId,
      variantType: variantService.variantEndpoint(variantType),
      variantId: variant.variantID
    };

    var successCallback = function (newVariant) {
      Notifications.success('Successfully ' + (variant.id ? 'modified' : 'created')  + ' variant');
      if (!variant.id) {
        variantService.getOsVariants($rootScope.application, variantType).push(newVariant);
      }
      $location.path('/detail/' + $routeParams.applicationId);
      $rootScope.variant = null;
    };
    var failureCallback = function () {
      Notifications.error('Something went wrong...');
    };

    var save =  variant.id ? variants.update : ((variantData instanceof FormData) ? variants.createWithFormData : variants.create);

    if (variantType !== 'iOS') {
      save(params, variantData, successCallback, failureCallback);
    } else {
      if (variantData.certificate) {
        variants.updateWithFormData(params, variantData, successCallback, failureCallback);
      } else {
        variants.patch(params, { name: variant.name, description: variant.description}, successCallback, failureCallback);
      }
    }
  };

  function variantProperties(variant, variantType) {
    var properties = ['name', 'description'], result = {};
    switch (variantType) {
    case 'android':
      properties = properties.concat(['projectNumber', 'googleKey']);
      break;
    case 'simplePush':
      properties = properties.concat([]);
      break;
    case 'chrome':
      properties = properties.concat(['clientId', 'clientSecret', 'refreshToken']);
      break;
    case 'iOS':
      if (variant.certificates && variant.certificates.length) {
        variant.certificate = variant.certificates[0];
      }
      properties = properties.concat(['production', 'passphrase', 'certificate']);
      var formData = new FormData();
      properties.forEach(function (property) {
        formData.append(property, variant[property] || '');
      });
      return formData;
    default:
      Notifications.error('Unknown variant type ' + variantType);
    }

    properties.forEach(function (property) {
      result[property] = variant[property];
    });
    return result;
  }

});

angular.module('upsConsole').factory('variantService', function(Notifications) {
  function variantKey(variantType) {
    switch (variantType) {
    case 'android':
    case 'simplePush':
      return variantType + 'Variants';
    case 'iOS':
      return 'iosvariants';
    case 'chrome':
      return 'chromePackagedAppVariants';
    default:
      Notifications.error('Unknown variant type ' + variantType);
      return '';
    }
  }

  return {
    variantEndpoint: function(variantType) {
      switch (variantType) {
      case 'android':
      case 'simplePush':
      case 'chrome':
      case 'iOS':
        return variantType;
      default:
        Notifications.error('Unknown variant type ' + variantType);
        return '';
      }
    },

    getOsVariants: function(application, variantType) {
      if (application) {
        return application[variantKey(variantType)];
      }
    },

    variantType: function(type) {
      switch (type) {
      case 'ANDROID':
        return 'android';
      case 'IOS':
        return 'iOS';
      case 'CHROME_PACKAGED_APP':
        return 'chrome';
      case 'SIMPLE_PUSH':
        return 'simplePush';
      }
    }
  };
});