var agSender = require( "unifiedpush-node-sender" ),
  settings = {
    url: "{{ sender.contextPath }}",
    applicationId: "{{ appDetail.app.pushApplicationID }}",
    masterSecret: "{{ appDetail.app.masterSecret }}"
  };

agSender.Sender( settings ).send( message, options ).on( "success", function( response ) {
  console.log( "success called", response );
});
