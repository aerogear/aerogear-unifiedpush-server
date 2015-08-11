'use strict';

angular.module('upsConsole').factory('variantModal', function ($modal, $q, variantsEndpoint, allowCreateVariant) {
  var service = {

    editName: function(app, variant) {
      return $modal.open({
        templateUrl: 'dialogs/edit-variant-name.html',
        controller: function($scope, $modalInstance) {
          $scope.variant = variant;

          $scope.confirm = function() {
            updateVariant( app, $scope.variant )
              .then(function ( updatedVariant ) {
                $modalInstance.close( updatedVariant );
              })
              .catch(function(err) {
                $modalInstance.dismiss(err);
              });
          };

          $scope.dismiss = function() {
            $modalInstance.dismiss('cancel');
          };
        }
      }).result;
    },

    add: function (app) {
      return $modal.open({
        templateUrl: 'dialogs/create-variant.html',
        controller: function ($scope, $modalInstance) {

          $scope.isNew = true;
          $scope.variant = {}; // start with empty variant
          $scope.variant.certificates = []; // initialize file list for upload

          $scope.confirm = function () {
            var variantData = extractValidVariantData($scope.variant);

            var createFunction = (variantData instanceof FormData) ? variantsEndpoint.createWithFormData : variantsEndpoint.create;

            createFunction({
              appId: app.pushApplicationID,
              variantType: extractVariantType($scope.variant)
            }, variantData)
              .then(function (variant) {
                $modalInstance.close(variant);
              })
              .catch(function (err) {
                $modalInstance.dismiss(err);
              });
          };

          $scope.dismiss = function () {
            $modalInstance.dismiss('cancel');
          };

          $scope.validateFileInputs = function () {
            switch ($scope.variant.type) {
            case 'ios':
              return $scope.variant.certificates.length > 0;
            }
            return true;
          };

          $scope.allowCreate = function( variantType ) {
            return allowCreateVariant( app, variantType );
          };
        }
      }).result;
    },

    edit: function (app, variant) {
      return $modal.open({
        templateUrl: 'dialogs/create-variant.html',
        controller: function ($scope, $modalInstance) {

          $scope.isNew = false;
          $scope.variant = variant;
          $scope.variant.certificates = []; // initialize file list for upload

          $scope.confirm = function () {
            updateVariant( app, $scope.variant )
              .then(function ( updatedVariant ) {
                $modalInstance.close( updatedVariant );
              });
          };

          $scope.dismiss = function () {
            $modalInstance.dismiss('cancel');
          };

          $scope.validateFileInputs = function () {
            if ($scope.variant.type === 'ios') {
              if (!$scope.variant.id) {
                // for new variant
                return $scope.variant.certificates.length > 0 && !!variant.passphrase; // we must enter certificate and passphrase
              } else {
                // for existing variant...
                if ($scope.variant.certificates.length > 0) { // if there is a certificate selected
                  return !!variant.passphrase; // we must provide a passphase
                }
              }
            }
            return true;
          };
        }
      }).result;
    }
  };

  function updateVariant ( app, variant ) {
    var endpointParams = {
      appId: app.pushApplicationID,
      variantType: extractVariantType(variant),
      variantId: variant.variantID
    };
    var variantData = extractValidVariantData(variant);
    if (variant.type !== 'ios') {
      return variantsEndpoint.update(endpointParams, variantData);
    } else {
      if (variant.certificates.length > 0) {
        return variantsEndpoint.updateWithFormData(endpointParams, variantData);
      } else {
        return variantsEndpoint.patch(endpointParams, {
          name: variant.name,
          description: variant.description,
          production: variant.production
        });
      }
    }
  }

  function extractValidVariantData(variant) {
    var properties = ['name'], result = {};
    switch (variant.type) {
    case 'android':
      properties = properties.concat(['projectNumber', 'googleKey']);
      break;
    case 'simplePush':
      properties = properties.concat([]);
      break;
    case 'ios':
      if (variant.certificates && variant.certificates.length) {
        variant.certificate = variant.certificates[0];
      }
      properties = properties.concat(['production', 'passphrase', 'certificate']);
      var formData = new FormData();
      properties.forEach(function (property) {
        formData.append(property, variant[property] === undefined ? '' : variant[property]);
      });
      return formData;
    case 'windows_wns':
      result.protocolType = 'wns';
      properties = properties.concat(['sid', 'clientSecret']);
      break;
    case 'windows_mpns':
      result.protocolType = 'mpns';
      properties = properties.concat([]);
      break;
    case 'adm':
      properties = properties.concat(['clientId', 'clientSecret']);
      break;
    default:
      throw 'Unknown variant type ' + variant.type;
    }
    properties.forEach(function (property) {
      result[property] = variant[property];
    });
    return result;
  }

  function extractVariantType( variant ) {
    switch(variant.type) {
    default:
      return variant.type;
    }
  }

  return service;

});
