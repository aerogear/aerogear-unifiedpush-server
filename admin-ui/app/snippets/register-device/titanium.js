// Appcelerator Titanium AeroGear Push
var AeroGearPush = require('AeroGearPush').init({
    pushServerURL: "{{ contextPath }}",
    {{ cordovaVariantType }}: { {{ senderID ? '\n        senderID: "' + senderID + '",' : '' }}
      variantID: "{{ variant.variantID }}",
      variantSecret: "{{ variant.secret }}"
    }
});

// register this device
AeroGearPush.registerDevice({
    onReceive: function(event) {
        // Track Push Open
        var pushId = event.data["aerogear-push-id"];
        AeroGearPush.trackPushOpenEvent(pushId);

        var dialog = Ti.UI.createAlertDialog({
            title: L('New Notification'),
            message: JSON.stringify(event.data),
            buttonNames: [L('View'),L('Cancel')],
            cancel: 1
        });
        dialog.addEventListener("click", function(event) {
            dialog.hide();
            if (event.index == 0) {
                // Do stuff to view the notification
            }
        });
        dialog.show();
	}
});
