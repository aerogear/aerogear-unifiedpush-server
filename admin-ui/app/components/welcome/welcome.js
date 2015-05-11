angular.module('upsConsole')
  .controller('WelcomeController', function( $rootScope ) {

    this.activate = function() {
      $rootScope.$broadcast('upsUpdateStats');
    };

  });
