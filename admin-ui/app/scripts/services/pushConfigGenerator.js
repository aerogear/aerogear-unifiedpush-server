var module = angular.module('upsConsole.services');

module.factory('pushConfigGenerator', function ($resource, $q, ContextProvider) {

	function cordovaVariantType(variant) {
	    switch (variant.type) {
		    case 'windows_mpns':
		      return 'windows';
		    default:
		      return variant.type;
	    }
	}

	function variantSpecificConfiguration(variant, config) {
      switch (variant.type) {
      	case 'android': config.senderID = variant.projectNumber 
      }
	}

	return {
		generate: function(variants) {
			var pushConfig = {
		      "pushServerURL": ContextProvider.contextPath()
		    };
		    variants.forEach(function(variant) {
		      var type = cordovaVariantType(variant);
		      var config = pushConfig[type] = {};
		      variantSpecificConfiguration(variant, config);
		      config.variantID = variant.variantID;
		      config.variantSecret = variant.secret;
		    });
		    return JSON.stringify(pushConfig, null, 2);
		}
	};

});