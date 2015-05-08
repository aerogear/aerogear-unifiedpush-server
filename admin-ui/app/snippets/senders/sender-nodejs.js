var agSender = require( "unifiedpush-node-sender" ),
  settings = {
    url: "{{ contextPath }}",
    applicationId: "{{ app.pushApplicationID }}",
    masterSecret: "{{ app.masterSecret }}"
  },
  message = {
    alert: "Hello from the Node.js Sender API!"
  },
  options = {
    config: {
        ttl: 3600,
    }
  };

agSender.Sender( settings ).send( message, options ).on( "success", function( response ) {
  console.log( "success called", response );
});
