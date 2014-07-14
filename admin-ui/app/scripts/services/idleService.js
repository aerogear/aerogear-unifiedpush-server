'use strict';

$.idleTimeout('#idletimeout', '#idletimeout a', {
    idleAfter: 5,
    pollingInterval: 2,
    //        keepAliveURL: authUrl + '/admin/keepalive', would need to change this path
    serverResponseEquals: '',
    failedRequests: 1,
    onTimeout: function(){
        $(this).slideUp();
        console.log('Logged out');
        UPS.logout();
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


