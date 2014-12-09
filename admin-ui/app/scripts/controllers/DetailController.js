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
  function($rootScope, $routeParams, $location, $modal, $http, applicationsEndpoint, variantsEndpoint,
    importerEndpoint, exporterEndpoint, Notifications, breadcrumbs, application, counts, ContextProvider,
    metricsEndpoint, pushConfigGenerator) {

  var $scope = this;

  /*
   * INITIALIZATION
   */
  $scope.application = application;
  $rootScope.application = application;
  $scope.counts = counts;
  breadcrumbs.generateBreadcrumbs();
  $scope.currentLocation = ContextProvider.contextPath();

  metricsEndpoint.application({id: $routeParams.applicationId}, function(data) {
    angular.forEach(data, function (warning) {
      angular.forEach(application.variants, function (variant) {
        if (warning.variantInformations.length &&
            variant.variantID === warning.variantInformations[0].variantID &&
            !warning.variantInformations[0].deliveryStatus) {
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
    var modalInstance = show('create-variant.html', {
      variant: function () { return variant; }
    });
    modalInstance.result.then(function (result) {
      var variantData = variantProperties(result.variant);
      var params = angular.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: result.variant.type
      });

      var createFunction = (variantData instanceof FormData) ? variantsEndpoint.createWithFormData : variantsEndpoint.create;

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
        Notifications.error('Unable to add the variant...');
      });
    });
  };

  $scope.editVariant = function (variant) {
    variant.protocolType = variant.type.split('windows_')[1];
    var modalInstance = show('create-variant.html', {
      variant: function () { return variant; }
    });
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
        Notifications.error('Unable to save the variant...');
      };

      if (variant.type !== 'ios') {
        variantsEndpoint.update(params, variantDataUpdate, successCallback, failureCallback);
      } else {
        if (variant.certificate) {
          variantsEndpoint.updateWithFormData(params, variantDataUpdate, successCallback, failureCallback);
        } else {
          variantsEndpoint.patch(params, { name: variant.name, description: variant.description}, successCallback, failureCallback);
        }
      }
    });
  };

  $scope.removeVariant = function (variant) {
    var modalInstance = show('remove-variant.html', {
      variant: function () { return variant; }
    });
    modalInstance.result.then(function (result) {
      var params = angular.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: result.variant.type,
        variantId: result.variant.variantID
      });
      variantsEndpoint.remove(params, function () {
        var osVariantsEndpoint = $scope.application.variants;
        osVariantsEndpoint.splice(osVariantsEndpoint.indexOf(variant), 1);
        updateCounts();
        Notifications.success('Successfully removed variant');
      }, function () {
        Notifications.error('Unable to remove the variant...');
      });
    });
  };

  $scope.renewMasterSecret = function () {
    var modalInstance = show('renew-master-secret.html', {});
    modalInstance.result.then(function () {
      var app = $scope.application;
      applicationsEndpoint.reset({appId: app.pushApplicationID}, function (application) {
        $scope.application.masterSecret = application.masterSecret;
        Notifications.success('Successfully renewed master secret for "' + app.name + '"');
      });
    });
  };

  $scope.renewVariantSecretEndpoint = function (variant) {
    var modalInstance = show('renew-variant-secret.html', {});
    modalInstance.result.then(function () {
      var app = $scope.application;
      var params = {
        appId: app.pushApplicationID,
        variantType: variant.type,
        variantId: variant.variantID
      };
      variantsEndpoint.reset(params, function (updatedVariant) {
        variant.secret = updatedVariant.secret;
        Notifications.success('Successfully renewed secret for variant "' + updatedVariant.name + '"');
      });
    });
  };

  $scope.exportInstallations = function (variant) {
    variant.total = counts[variant.type];
    var modalInstance = show('export-installations.html', {
      variant: function () { return variant; }
    });
    modalInstance.result.then(function () {
      var params = {
        variantId: variant.variantID
      };
      exporterEndpoint.export(params, function (content) {
        var hiddenElement = document.createElement('a');

        hiddenElement.href = 'data:attachment/json,' + encodeURI(JSON.stringify(content));
        hiddenElement.target = '_blank';
        hiddenElement.download = variant.variantID + '.json';
        hiddenElement.click();

        Notifications.success('Successfully exported installations');
      });
    });
  };

  $scope.importInstallations = function (variant) {
    variant.installations = [];
    var modalInstance = show('import-installations.html', {
      variant: function () { return variant; }
    });
    modalInstance.result.then(function (result) {
      $rootScope.isViewLoading = true;
      var fd = new FormData();
      fd.append('file', result.variant.installations[0]);
      $http.defaults.headers.common.Authorization = 'Basic ' + btoa(variant.variantID+
      ':' + variant.secret);
      importerEndpoint.import(null,fd,function(){
        $rootScope.isViewLoading = false;
        Notifications.success('Import process has started"');
        updateCounts();
      });
    });
  };

  $scope.generatePushConfigDialog = function() {
    var application = $scope.application;
    show('generate-push-config.html', {
      application: function () { return application; }
    });
  };

  /*
   * PRIVATE FUNCTIONS
   */

  function updateCounts() {
    $scope.counts =applicationsEndpoint.count({appId: $scope.application.pushApplicationID});
  }

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

    //preview function for the import
    $scope.previewImport = function() {
      if (window.File && window.FileList && window.FileReader) {
        var importFiles = variant.installations[0];
        var fileReader = new FileReader();
        fileReader.readAsText(importFiles);
        fileReader.onload = function(e) {
          try {
            $scope.importPreview = JSON.parse(e.target.result).length;
            $scope.incorrectFormat = false;
          }
          catch(e) {
            $scope.importPreview = null;
            $scope.incorrectFormat = true;
          }

        };
      }
    };

    // generatePushConfig dialog
    $scope.variantSelection = {};
    $scope.toggleSelection = function toggleSelection( variant ) {
      $scope.variantSelection[variant.type] = variant;
    };
    $scope.generatePushConfig = function() {
      var selectedVariants = Object.keys($scope.variantSelection).map(function (type) {
        return $scope.variantSelection[type];
      });
      return pushConfigGenerator.generate(selectedVariants);
    };
    $scope.downloadPushConfig = function() {
      var pushConfig = $scope.generatePushConfig();
      var pom = document.createElement('a');
      pom.setAttribute('href', 'data:application/json;charset=utf-8,' + encodeURIComponent(pushConfig));
      pom.setAttribute('download', 'push-config.json');
      pom.click();
      $scope.cancel();
    };
  }

  function show(template, resolve) {
    return $modal.open({
      templateUrl: 'views/dialogs/' + template,
      controller: modalController,
      resolve: angular.extend({ // mix-in some defaults
        variant: function() { return null; }
      }, resolve)
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
      variant.type = variant.type + '_' + variant.protocolType;
      properties = properties.concat(['sid', 'clientSecret', 'protocolType']);
      break;
    case 'windows_wns':
      result.protocolType = 'wns';
      properties = properties.concat(['sid', 'clientSecret']);
      break;
    case 'windows_mpns':
      result.protocolType = 'mpns';
      properties = properties.concat([]);
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
