'use strict';

angular.module('upsConsole')
  .controller('RouteController', function($router, $scope, $timeout, $log) {
    $router.config([
      {path: '/',                     component: 'home'},
      {path: '/welcome',              component: 'welcome'},
      {path: '/bootstrap',              component: 'bootstrap'},
      {path: '/wizard/create-app',    component: 'wizard01CreateApp'},
      {path: '/wizard/add-variant',   component: 'wizard02AddVariant'},
      {path: '/wizard/register-device',  component: 'wizard03RegisterDevice'},
      {path: '/wizard/send-push-notification',  component: 'wizard04SendPushNotification'},
      {path: '/wizard/setup-sender',  component: 'wizard05SetupSender'},
      {path: '/wizard/done',          component: 'wizard06Done'},
      {path: '/app/:app/:tab',        component: 'appDetail'},
      {path: '/links-check',          component: 'linksCheck'},
    ]);

    /**
     * Listens for 'upsNavigate' event and switches the view given by provided path (defined in $router.config above)
     *
     * @param event - the event passed automatically from $broadcast/$emit
     * @param {string} path - the path of the path that this view should navigate to
     */
    $scope.$on('upsNavigate', function(event, path) {
      $log.debug( 'Navigating to: ' + path );
      $timeout(function() {
        $router.navigate(path).then(
          function(){
            $log.debug( 'Navigation success: ' + path );
          },
          function(){
            $log.warn( 'Navigation failure: ' + path );
          }
        );
      }, 100);
    });
  });
