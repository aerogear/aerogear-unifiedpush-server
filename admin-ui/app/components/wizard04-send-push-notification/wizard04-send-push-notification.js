angular.module('upsConsole')
  .controller('Wizard04SendPushNotificationController', function( $router, $interval, $timeout, createAppWizard, Notifications, $rootScope, messageSenderEndpoint, appModal, applicationsEndpoint) {

    var self = this;

    var intervalForUpdateDeviceCount;

    this.canActivate = function() {
      if ( !createAppWizard.app ) {
        $router.root.navigate('/wizard/create-app');
        return false;
      }
      if ( !createAppWizard.variant ) {
        $router.root.navigate('/wizard/add-variant');
        return false;
      }
      return true;
    };

    this.app = createAppWizard.app;
    this.variant = createAppWizard.variant;
    this.deviceCount = 0;

    this.pushData = {
      'message': {
        'sound': 'default',
        'alert': 'Hello! This is my first notification to ' + (self.variant ? self.variant.name : null),
        'simple-push': 'version=' + new Date().getTime()
      },
      'criteria' : {}
    };



    this.sendNotification = function() {
      messageSenderEndpoint( self.app.pushApplicationID, self.app.masterSecret ).send({}, self.pushData)
        .then(function() {
          $rootScope.$broadcast('upsNotificationSent', self.pushData, self.app);
          Notifications.success('Notification was successfully sent');
          $router.root.navigate('/wizard/setup-sender');
        })
        .catch(function() {
          Notifications.error('Failed to sent notification');
        });
    };

    this.editAppName = function() {
      var appClone = angular.extend( {}, self.app );
      appModal.editName( appClone )
        .then(function( updatedApp ) {
          angular.extend( self.app, updatedApp );
        });
    };

    function updateDeviceCount() {
      applicationsEndpoint.getWithMetrics({appId: createAppWizard.app.pushApplicationID})
        .then(function(data) {
          self.deviceCount = data.$deviceCount;
        });
    }

    this.activate = function() {
      updateDeviceCount();

      $timeout(function() { // timeout is a workaround for bug in the router - canDeactivate is called right after activate
        intervalForUpdateDeviceCount = $interval(function () {
          updateDeviceCount();
          if (self.deviceCount > 0) {
            $interval.cancel(intervalForUpdateDeviceCount);
          }
        }, 1500);
      }, 1500);
    };

    this.canDeactivate = function() {
      if (intervalForUpdateDeviceCount) {
        $interval.cancel(intervalForUpdateDeviceCount);
      }
      return true;
    };

  });


