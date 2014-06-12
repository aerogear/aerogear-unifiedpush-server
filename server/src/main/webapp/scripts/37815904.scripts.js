'use strict';

/* Directives */
angular.module('ups.directives', [])

  .directive('upsNavigation', function () {
    return {
      scope: {
        current: '@'
      },
      restrict: 'E',
      replace: true,
      templateUrl: 'directives/ups-navigation.html'
    };
  })

  .directive('upsAlerts', function () {
    return {
      scope: {
      },
      controller: function ($rootScope, $scope) {
        $scope.alerts = $rootScope.notifications.data;
      },
      restrict: 'E',
      replace: false,
      templateUrl: 'directives/ups-alerts.html'
    };
  })
  .directive('upsBreadcrumb', function () {
    return {
      templateUrl: 'directives/ups-breadcrumb.html',
      controller: function ($scope, $compile, breadcrumbs) {
        $scope.breadcrumbs = breadcrumbs;
      }
    };
  })

  .directive('variants', function () {
    return {
      scope: {
        variants: '=',
        counts: '=',
        type: '@'
      },
      controller: function ($rootScope, $scope, $routeParams) {
        $scope.expand = function (variant) {
          variant.expand = !variant.expand;
        };

        $scope.isCollapsed = function (variant) {
          return !variant.expand;
        };

        $scope.editVariant = function (variant, type) {
          $scope.$parent.editVariant(variant, type);
        };

        $scope.removeVariant = function (variant, type) {
          $scope.$parent.removeVariant(variant, type);
        };

        $scope.applicationId = $routeParams.applicationId;

        $scope.currentVariant = function (variant) {
          $rootScope.variant = variant;
        };

      },
      templateUrl: 'directives/variant-details.html'
    };
  })

  .directive('upsFiles', function () {
    return {
      scope: {
        'files': '=upsFiles'
      },
      restrict: 'A',
      replace: false,
      link: function ($scope, $element) {
        $element.bind('change', function (e) {
          while ($scope.files.length > 0) {
            $scope.files.pop();
          }
          for (var i in e.target.files) {
            if (typeof e.target.files[i] === 'object') {
              $scope.files.push(e.target.files[i]);
            }
          }
        });
      }
    };
  })

  .directive('upsPluralize', function () {
    return {
      scope: {
        'noun': '@',
        'count': '='
      },
      restrict: 'E',
      template:
        '<span ng-show="count > 0"><strong>{{count}}</strong> {{ noun }}<span ng-show="count > 1">s</span></span>' +
        '<span ng-show="count == 0">No {{ noun }}s</span>'
    };
  });

'use strict';

angular.module('newadminApp', [
  'newadminApp.services',
  'ngResource',
  'ngRoute',
  'ui.bootstrap',
  'ups.directives',
  'patternfly.notification',
  'hljs'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainController',
        crumb: {
          id: 'apps',
          label: 'Applications'
        }
      })
      .when('/detail/:applicationId', {
        templateUrl: 'views/detail.html',
        controller: 'DetailController',
        crumb: {
          id: 'app-detail',
          parent: 'apps',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/:applicationId/installations/:variantId', {
        templateUrl: 'views/installation.html',
        controller: 'InstallationController',
        crumb: {
          parent: 'app-detail',
          label: '$ variant.name ? variant.name : "Registering Installations"'
        }
      })
      .when('/example/:applicationId/:variantType/:variantId', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/example/:applicationId/:variantType', {
        templateUrl: 'views/example.html',
        controller: 'ExampleController',
        crumb: {
          parent: 'app-detail',
          label: 'Example'
        }
      })
      .when('/compose/:applicationId', {
        templateUrl: 'views/compose.html',
        controller: 'ComposeController',
        crumb: {
          parent: 'app-detail',
          label: 'Send Push'
        }
      })
      .when('/dashboard', {
        templateUrl: 'views/dashboard.html',
        controller: 'DashboardController',
        crumb: {
          id: 'dash',
          label: 'Dashboard'
        }
      })
      .when('/activity/:applicationId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        crumb: {
          id: 'activity',
          parent: 'dash',
          label: '$ application.name ? application.name : "Current Application"'
        }
      })
      .when('/activity/:applicationId/:variantId', {
        templateUrl: 'views/notification.html',
        controller: 'ActivityController',
        crumb: {
          parent: 'activity',
          label: '$ variant.name ? variant.name : "Current variant"'
        }
      })
      .otherwise({
        redirectTo: '/'
      });

  });


'use strict';

(function($) {

  // close dropdown immediately the menuitem is clicked, otherwise it will collide with popups/modals
  $(document).on('click', '.dropdown.open a[role="menuitem"]', function() {
    $('.dropdown.open').removeClass('open');
  });

  // focus first input in a modal once modal is shown
  angular.module('newadminApp')
    .config(function($provide) {
      $provide.decorator('$modal', function($delegate) {
        return {
          open: function() {
            var modal = $delegate.open.apply(this, arguments);
            modal.opened.then(function() {
              (function delayFocus() {
                var el = $('.modal-dialog input:first:visible');
                if (el.size() > 0) {
                  window.requestAnimationFrame(function() {
                    el.focus();
                  });
                } else {
                  window.setTimeout(delayFocus, 50);
                }
              })();
            });
            return modal;
          }
        };
      });
    });

})($);
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

angular.module('newadminApp').controller('MainController',
  function($rootScope, $scope, $modal, pushApplication, Notifications) {

  /*
   * INITIALIZATION
   */

  $scope.alerts = [];

  //let's show all the applications
  $scope.applications = pushApplication.query();



  /*
   * PUBLIC METHODS
   */

  $scope.open = function (application) {
    var modalInstance = show(application, 'create-app.html');
    modalInstance.result.then(function (application) {
      pushApplication.create(application, function (newApp) {
        $scope.applications.push(newApp);
        Notifications.success('Successfully created application "' + newApp.name + '".');
      }, function () {
        Notifications.error('Something went wrong...', 'danger');
      });
    });
  };

  $scope.edit = function (application) {
    var modalInstance = show(application, 'create-app.html');
    modalInstance.result.then(function (application) {
      pushApplication.update({appId: application.pushApplicationID}, application, function () {
        Notifications.success('Successfully edited application "' + application.name + '".');
      });
    });
  };

  $scope.remove = function (application) {
    var modalInstance = show(application, 'remove-app.html');
    modalInstance.result.then(function () {
      pushApplication.remove({appId: application.pushApplicationID}, function () {
        $scope.applications.splice($scope.applications.indexOf(application), 1);
        Notifications.success('Successfully removed application "' + application.name + '".');
      });
    });
  };


  /*
   * PRIVATE METHODS
   */

  function modalController($scope, $modalInstance, application) {
    $scope.application = application;
    $scope.ok = function (application) {
      $modalInstance.close(application);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }

  function show(application, template) {
    return $modal.open({
      templateUrl: 'views/dialogs/' + template,
      controller: modalController,
      resolve: {
        application: function () {
          return application;
        }
      }
    });
  }
});

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

angular.module('newadminApp').controller('DetailController',
  function($rootScope, $scope, $routeParams, $window, $modal, pushApplication, variants, Notifications, breadcrumbs) {

  /*
   * INITIALIZATION
   */
  pushApplication.get({appId: $routeParams.applicationId}, function (application) {
    $rootScope.application = application;
    breadcrumbs.generateBreadcrumbs();
    var href = $window.location.href;
    $scope.currentLocation = href.substring(0, href.indexOf('#'));
  });
  pushApplication.count({appId: $routeParams.applicationId}, function (counts) {
    $scope.counts = counts;
  });


  /*
   * PUBLIC METHODS
   */

  $scope.addVariant = function (variant) {
    var modalInstance = show(variant, null, 'create-variant.html');
    modalInstance.result.then(function (result) {
      var variantType = result.variantType;
      var variantData = variantProperties(result.variant, variantType);
      var params = $.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: variantEndpoint(result.variantType)
      });

      var createFunction = (variantData instanceof FormData) ? variants.createWithFormData : variants.create;

      createFunction(params, variantData, function (newVariant) {
        var osVariants = getOsVariants(variantType);
        osVariants.push(newVariant);
        Notifications.success('Successfully created variant');
      }, function () {
        Notifications.error('Something went wrong...');
      });
    });
  };

  $scope.editVariant = function (variant, variantType) {
    var modalInstance = show(variant, variantType, 'create-variant.html');
    modalInstance.result.then(function (result) {
      var variantDataUpdate = variantProperties(variant, variantType);
      var params = $.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: variantEndpoint(variantType),
        variantId: result.variant.variantID
      });

      var successCallback = function () {
        Notifications.success('Successfully modified variant');
      };
      var failureCallback = function () {
        Notifications.error('Something went wrong...');
      };

      if (variantType !== 'iOS') {
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

  $scope.removeVariant = function (variant, variantType) {
    var modalInstance = show(variant, variantType, 'remove-variant.html');
    modalInstance.result.then(function (result) {
      var params = $.extend({}, {
        appId: $scope.application.pushApplicationID,
        variantType: variantEndpoint(variantType),
        variantId: result.variant.variantID
      });
      variants.remove(params, function () {
        var osVariants = getOsVariants(variantType);
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

  function getOsVariants(variantType) {
    return $scope.application[variantKey(variantType)];
  }

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

  function variantEndpoint(variantType) {
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
  }

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

angular.module('newadminApp').controller('InstallationController',
  function($rootScope, $scope, $routeParams, installations) {

  $scope.currentPage = 1;

  $scope.expand = function (installation) {
    installation.expand = !installation.expand;
  };

  $scope.isCollapsed = function (installation) {
    return !installation.expand;
  };

  $scope.pageChanged = function () {
    fetchInstallations($scope.currentPage);
  };

  $scope.update = function (installation) {
    var params = {variantId: $routeParams.variantId, installationId: installation.id};
    installation.enabled = !installation.enabled;
    installations.update(params, installation);
  };

  function fetchInstallations(pageNo) {
    installations.get({variantId: $routeParams.variantId, page: pageNo - 1, per_page: 10}, function (data, responseHeaders) {
      $scope.installations = data;
      $scope.totalItems = responseHeaders('total');
    });
  }

  fetchInstallations(1);
});
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

angular.module('newadminApp').controller('ExampleController',
  function($rootScope, $scope, $routeParams, $window, $timeout, variants, pushApplication) {

  /*
   * INITIALIZATION
   */
  var params = {
    appId: $routeParams.applicationId,
    variantType: $routeParams.variantType,
    variantId: $routeParams.variantId
  };
  $scope.variantType = $routeParams.variantType;
  $scope.active = $routeParams.variantType;
  $scope.applicationId = $routeParams.applicationId;

  if (typeof $routeParams.variantId !== 'undefined') {
    variants.get(params, function (variant) {
      $scope.variant = variant;
      var href = $window.location.href;
      $scope.currentLocation = href.substring(0, href.indexOf('#'));
    });
  } else {
    pushApplication.get(params, function (application) {
      $scope.application = application;
    });
  }

  $timeout(function(){
    hljs.highlightBlock(angular.element('#cordova-code')[0]);
  }, 1000);
    
  $scope.isActive = function (tabName) {
    return tabName === $scope.active;
  };

  $scope.setActive = function (tabName) {
    $scope.active = tabName;
  };

  $scope.projectNumber = function() {
    return $scope.variantType === 'android' ? ('senderID: "' + $scope.variant.projectNumber +'",') : '';
  };
});
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

angular.module('newadminApp').controller('ComposeController', function($rootScope, $scope, $routeParams, $window, $modal, pushApplication, Notifications) {

    /*
     * INITIALIZATION
     */
  $scope.variantSelection = [];
  $scope.criteria = [];

  pushApplication.get( {appId: $routeParams.applicationId}, function ( application ) {
    $scope.application = application;
    var href = $window.location.href;
    $scope.currentLocation = href.substring( 0, href.indexOf( '#' ) );
  } );

  $scope.sendMessage = function () {
    var pushData = {'message': {'sound': 'default', 'alert': $scope.testMessage}};

    //let's check if we filter variants
    if($scope.variantSelection.length > 0) {
      pushData.variants = [];
      for(var variant in $scope.variantSelection) {
        pushData.variants.push($scope.variantSelection[variant].variantID);
      }
    }
    //let's check if we filer on aliases
    if($scope.criteria.alias) {
      pushData.alias = $scope.criteria.alias.split(',');
    }

    //let's check if we filter on deviceType
    if($scope.criteria.deviceType) {
      pushData.deviceType = $scope.deviceType.alias.split(',');
    }

    //let's check if we filter on categories
    if($scope.criteria.categories) {
      pushData.categories = $scope.categories.alias.split(',');
    }

    $.ajax
      ({
      contentType: 'application/json',
      type: 'POST',
      url: 'rest/sender',
      username: $scope.application.pushApplicationID,
      password: $scope.application.masterSecret,
      headers: { 'aerogear-sender': 'AeroGear UnifiedPush Console' },
      data: JSON.stringify( pushData ),
      success: function(){
            Notifications.success('Successfully sent notification');
          },
      error: function(){
            Notifications.error('Something went wrong...', 'danger');
          },
      complete: function () {
            $scope.testMessage = '';
            $scope.$apply();
          }
    });
  };

  $scope.changeVariant = function ( application ) {
    show( application, 'filter-variants.html' );
  };

  $scope.changeCriteria = function ( application ) {
    show( application, 'add-criteria.html' );
  };

  function modalController( $scope, $modalInstance, application, variantSelection, criteria ) {

    $scope.variantSelection = variantSelection;
    $scope.criteria = criteria;
    $scope.application = application;
    $scope.ok = function ( application ) {
      $modalInstance.close( application );
    };

    $scope.cancel = function () {
      $modalInstance.dismiss( 'cancel' );
    };

    $scope.toggleSelection = function toggleSelection( variant ) {
      var idx = $scope.variantSelection.indexOf( variant );

      // is currently selected
      if ( idx > -1 ) {
        $scope.variantSelection.splice( idx, 1 );
      }
      // is newly selected
      else {
        $scope.variantSelection.push( variant );
      }

    };
  }


  function show( application, template ) {
    return $modal.open( {
      templateUrl: 'views/dialogs/' + template,
      controller: modalController,
      resolve: {
        application: function () {
          return application;
        },
        variantSelection: function () {
          return $scope.variantSelection;
        },
        criteria: function () {
          return $scope.criteria;
        }
      }
    } );
  }

});

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

angular.module('newadminApp').controller('DashboardController',
  function ($scope, dashboard) {
    dashboard.totals({}, function (data) {
      $scope.dashboardData = data;
    });

    dashboard.warnings({}, function (data) {
      $scope.warnings = data;
    });

    dashboard.topThree({}, function (data) {
      $scope.topThree = data;
    });
  });

angular.module('newadminApp').controller('ActivityController',
  function ($scope, $rootScope, $routeParams, $modal, metrics, pushApplication, breadcrumbs) {

    $scope.applicationId = $routeParams.applicationId;

    function findVariant(variants, closure, variantId) {
      angular.forEach(variants, function (variant) {
        if (variant.variantID === variantId) {
          closure(variant);
        }
      });
    }

    function forAllVariants(application, variantId, closure) {
      findVariant(application.iosvariants, closure, variantId);
      findVariant(application.androidVariants, closure, variantId);
      findVariant(application.simplePushVariants, closure, variantId);
      findVariant(application.chromePackagedAppVariants, closure, variantId);
    }

    pushApplication.get({appId: $routeParams.applicationId}, function (application) {
      $rootScope.application = application;

      if (typeof $routeParams.variantId !== 'undefined') {
        forAllVariants(application, $routeParams.variantId, function (variant) {
          $rootScope.variant = variant;
        });
      }
      breadcrumbs.generateBreadcrumbs();
    });

    if (typeof $routeParams.variantId !== 'undefined') {
      metrics.variant({id: $routeParams.variantId}, function (data) {
        $scope.pushMetrics = data;
        angular.forEach(data, function (metric) {
          metric.totalReceivers = metric.variantInformations[0].receivers;
          metric.deliveryFailed = !metric.variantInformations[0].deliveryStatus;
        });
      });
    } else {
      metrics.application({id: $routeParams.applicationId}, function(data) {
        $scope.pushMetrics = data;

        function totalReceivers(data) {
          angular.forEach(data, function (metric) {
            angular.forEach(metric.variantInformations, function (variant) {
              if (!variant.deliveryStatus) {
                metric.deliveryFailed = true;
              }
              if (!metric.totalReceivers) {
                metric.totalReceivers = 0;
              }
              metric.totalReceivers += variant.receivers;
            });
          });
        }

        totalReceivers(data);
      });
    }

    $scope.variantMetricInformation = function(metrics) {
      angular.forEach(metrics, function(variantInfo) {
        forAllVariants($rootScope.application, variantInfo.variantID, function (variant) {
          variantInfo.name = variant.name;
        });
      });

      return metrics;
    };

    $scope.expand = function (metric) {
      metric.expand = !metric.expand;
    };

    $scope.isCollapsed = function (metric) {
      return !metric.expand;
    };

    $scope.parse = function (metric) {
      return JSON.parse(metric.rawJsonMessage);
    };

    $scope.showFullRequest = function (rawJsonMessage) {
      $modal.open({
        templateUrl: 'views/dialogs/request.html',
        controller: function ($scope, $modalInstance, request) {
          $scope.request = request;

          $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
          };
        },
        resolve: {
          request: function () {
            //nasty way to get formatted json
            return JSON.stringify(JSON.parse(rawJsonMessage), null, 4);
          }
        }
      });
    };

  });

'use strict';

var backendMod = angular.module('newadminApp.services', []).
  value('version', '0.1');

backendMod.factory('pushApplication', function ($resource) {
  return $resource('rest/applications/:appId/:verb', {
    appId: '@appId'
  }, {
    get: {
      method: 'GET'
    },
    query: {method: 'GET', isArray: true},
    create: {
      method: 'POST'
    },
    update: {
      method: 'PUT'
    },
    delete: {
      method: 'DELETE'
    },
    count: {
      method: 'GET',
      params: {verb: 'count'}
    },
    reset: {
      method: 'PUT',
      params: {verb: 'reset'}
    }
  });
});

backendMod.factory('variants', function ($resource) {
  return $resource('rest/applications/:appId/:variantType/:variantId', {
    appId: '@appId',
    variantType: '@variantType'
  }, {
    get: {
      method: 'GET'
    },
    query: {method: 'GET', isArray: true},
    create: {
      method: 'POST'
    },
    update: {
      method: 'PUT'
    },
    delete: {
      method: 'DELETE'
    },
    patch: {
      method: 'PATCH'
    },
    createWithFormData: {
      method: 'POST',
      headers: {'Content-Type': undefined},
      withCredentials: true,
      transformRequest: angular.identity
    },
    updateWithFormData: {
      method: 'PUT',
      headers: {'Content-Type': undefined},
      withCredentials: true,
      transformRequest: angular.identity
    }
  });
});

backendMod.factory('installations', function ($resource) {
  return $resource('rest/applications/:variantId/installations/:installationId', {
    variantId: '@variantId',
    installationId: '@installationId'
  }, {
    get: {
      method: 'GET',
      isArray: true
    },
    update: {
      method: 'PUT'
    }
  });
});

backendMod.factory('dashboard', function ($resource) {
  return $resource('rest/metrics/dashboard/:verb', {}, {
    totals: {
      method: 'GET'
    },
    warnings: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'warnings'
      }
    },
    topThree: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'active'
      }
    }
  });
});

backendMod.factory('metrics', function ($resource) {
  return $resource('rest/metrics/messages/:verb/:id', {
    id: '@id'
  }, {
    application: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'application'
      }
    },
    variant: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'variant'
      }
    }
  });
});


backendMod.factory('breadcrumbs', function ($rootScope, $route) {
  var BreadcrumbService = {
    breadcrumbs: [],
    routes: {},
    get: function() {
      return this.breadcrumbs;
    },
    init: function() {
      var self = this;
      angular.forEach($route.routes, function(route) {
        if (route.crumb) {
          self.routes[route.crumb.id] = route;
        }
      });
    },
    generateBreadcrumbs: function() {
      var parent, self = this;

      var getRoute = function(route) {
        if ($route.current) {
          var param;
          angular.forEach($route.current.params, function (value, key) {
            if (route.indexOf(key) !== -1) {
              param = value;
            }
            if (param) {
              route = route.replace(':' + key, value);
            }
          });
          return route;
        }
      };

      var label = function(route) {
        return route.crumb.label.indexOf('$') !== -1 ? $rootScope.$eval(route.crumb.label.substring(1)) : route.crumb.label;
      };

      this.breadcrumbs = [];
      if ($route.current && $route.current.crumb) {
        self.breadcrumbs.push({ label: label($route.current), path: $route.current.path });
        parent = $route.current.crumb.parent;

        while (parent) {
          var route = self.routes[parent];
          route.path = getRoute(route.originalPath);
          self.breadcrumbs.push({ label: label(route), path: route.path });
          parent = route.crumb.parent;
        }

        self.breadcrumbs.reverse();
      }
    }
  };

  // We want to update breadcrumbs only when a route is actually changed
  // as $location.path() will get updated immediately (even if route change fails!)
  $rootScope.$on('$routeChangeSuccess', function() {
    BreadcrumbService.generateBreadcrumbs();
  });

  BreadcrumbService.init();
  BreadcrumbService.generateBreadcrumbs();

  return BreadcrumbService;
});
