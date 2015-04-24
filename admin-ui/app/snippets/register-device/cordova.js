var app = {
  // Application Constructor
  initialize: function() {
    this.bindEvents();
  },
  // Bind Event Listeners
  //
  // Bind any events that are required on startup. Common events are:
  // 'load', 'deviceready', 'offline', and 'online'.
  bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
  },
  // deviceready Event Handler
  //
  // The scope of 'this' is the event. In order to call the 'receivedEvent'
  // function, we must explicitly call 'app.receivedEvent(...);'
  onDeviceReady: function() {
    app.receivedEvent('deviceready');
    var pushConfig = {
      pushServerURL: "{{ contextPath }}",
      {{ cordovaVariantType }}: { {{ senderID ? '\n        senderID: "' + senderID + '",' : '' }}
        variantID: "{{ variant.variantID }}",
        variantSecret: "{{ variant.secret }}"
      }
    };
push.register(app.onNotification, successHandler, errorHandler, pushConfig);

function successHandler() {
  console.log('success')
}

function errorHandler(message) {
  console.log('error ' + message);
}
},
onNotification: function(event) {
  alert(event.alert);
},
// Update DOM on a Received Event
receivedEvent: function(id) {
  var parentElement = document.getElementById(id);
  var listeningElement = parentElement.querySelector('.listening');
  var receivedElement = parentElement.querySelector('.received');

  listeningElement.setAttribute('style', 'display:none;');
  receivedElement.setAttribute('style', 'display:block;');

  console.log('Received Event: ' + id);
}
};

app.initialize();
