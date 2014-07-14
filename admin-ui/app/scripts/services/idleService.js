'use strict';

$.idleTimeout('#idletimeout', '#idletimeout a', {
    idleAfter: 300,
    pollingInterval: 60,
    serverResponseEquals: '',
    failedRequests: 1,
    onTimeout: function(){
        UPS.logout();
        $(this).slideUp();
      },
    onIdle: function(){
        $(this).slideDown(); // show the warning bar
      },
    onCountdown: function( counter ){
        $(this).find('span').html( counter ); // update the counter
      },
    onResume: function(){
        $(this).slideUp(); // hide the warning bar
      }
  });


