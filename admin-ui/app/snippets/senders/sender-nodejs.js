var agSender = require( "unifiedpush-node-sender" ),
  settings = {
    url: "{{ contextPath }}",
    applicationId: "{{ app.pushApplicationID }}",
    masterSecret: "{{ app.masterSecret }}"
  };

agSender.Sender( settings ).send( message, options ).on( "success", function( response ) {
  console.log( "success called", response );
});
