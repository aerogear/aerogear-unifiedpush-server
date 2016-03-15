angular.module('upsConsole')
  .controller('BootstrapController', function( $rootScope, applicationsEndpoint, Notifications, $router, bootstrapedAppName, allowCreateVariant ) {

    var self = this;

    this.types = {};

    this.config = {
      android: {},
      ios: {},
      simplePush : {},
      windows_wns: {},
      windows_mpns: {},
      adm: {}
    };

    this.allowCreate = function( variantType ) {
      return allowCreateVariant( null, variantType );
    };

    this.iosCertificates = [];

    this.validateForm = function () {
      if (Object.keys(self.types).length == 0) {
        return false;
      }
      return Object.keys(self.types)
        .map(function(key) {return self.types[key]})
        .some(function(value) { return value === true });
    };

    this.validateFileInputs = function () {
      return (self.types.ios) ? self.iosCertificates.length > 0 : true;
    };

    this.bootstrapApplication = function() {
      var formData = new FormData(),
          variantConfig = null;

      formData.append('pushApplicationName', bootstrapedAppName());
      angular.forEach(self.types, function(enabled, variantType) {
        if (enabled) {
          variantConfig = angular.copy(self.config[variantType]);
          variantConfig.variantName = variantType; // placeholder name
          if (variantType === 'ios') {
            if (self.iosCertificates && self.iosCertificates.length) {
              variantConfig.certificate = self.iosCertificates[0];
            }
          }
          if (/^windows_/.test(variantType)) {
            var matcher = /^windows_(.*)$/.exec(variantType);
            variantType = 'windows';
            variantConfig.type = matcher[1];
          }
          angular.forEach(variantConfig, function(propertyValue, propertyName) {
            var formPropertyName = variantType + capitalize(propertyName);
            formData.append( formPropertyName, propertyValue );
          });
        }
      });
      applicationsEndpoint.bootstrap({}, formData)
        .then(function( app ) {
          Notifications.success('Push notification support enabled for ' + app.name);
          $rootScope.$broadcast('upsUpdateStats');
          $rootScope.$broadcast('upsNavigate', '/app/' + app.pushApplicationID + '/variants');
        })
        .catch(function() {
          Notifications.error('Failed to enable Push notification support for ' + self.application.name);
        });
    };

    function capitalize(s) {
      return s[0].toUpperCase() + s.slice(1);
    }

  });
