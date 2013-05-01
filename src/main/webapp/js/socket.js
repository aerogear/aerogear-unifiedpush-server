(function() {
    var mailEndpoint, mailRequest, fooEndpoint, fooRequest, broadcastRequest, broadcastEndpoint, testFrame;

    getTextAreaElement().value = "Web Socket opened!";

    broadcastRequest = navigator.push.register();
    broadcastRequest.onsuccess = function( event ) {
        broadcastEndpoint = event.target.result;
        broadcastRequest.registerWithPushServer( "broadcast", broadcastEndpoint );
        appendTextArea("Subscribed to Broadcast messages");
    };

    mailRequest = navigator.push.register();
    mailRequest.onsuccess = function( event ) {
        mailEndpoint = event.target.result;
        mailRequest.registerWithPushServer( "mail", mailEndpoint );
        $("#mailVersion").attr("name", mailEndpoint.channelID);
        appendTextArea("Subscribed to Mail messages on " + mailEndpoint.channelID);
    };

    fooRequest = navigator.push.register();
    fooRequest.onsuccess = function( event ) {
        fooEndpoint = event.target.result;
        fooRequest.registerWithPushServer( "foo", fooEndpoint );
        $("#fooVersion").attr("name", fooEndpoint.channelID);
        appendTextArea("Subscribed to Foo messages on " + fooEndpoint.channelID);
    };

    navigator.setMessageHandler( "push", function( message ) {
        if ( message.channelID === mailEndpoint.channelID )
            appendTextArea("Mail Notification - " + message.version);
        else if ( message.channelID === fooEndpoint.channelID )
            appendTextArea("Foo Notification - " + message.version);
        // Broadcast messages are subscribed by default and can be acted on as well
        else if ( message.channelID === broadcastEndpoint.channelID )
            appendTextArea("Broadcast Notification - " + message.version);
    });

    function appendTextArea(newData) {
        var el = getTextAreaElement();
        el.value = el.value + '\n' + newData;
    }

    function getTextAreaElement() {
        return document.getElementById('responseText');
    }

    $("button").on("click", function( event ) {
        var urlSwitch, data,
            $this = $(this),
            type = this.id,
            input = $("#" + type + "Version"),
            idArray = [ input.attr("name") ],
            val = input.val();

        $this.prop("disabled", true);

        if ( type === "broadcast" ) {
            urlSwitch = "broadcast";
            data = {
                version: val
            };
        } else {
            urlSwitch = "selected",
            data = {
                channelIDs: idArray,
                version: val
            };
        }

        $.ajax({
            url: "http://" + window.location.hostname + ":8080/ag-push/rest/sender/simplePush/" + urlSwitch + "/" + AeroGear.SimplePush.variantID,
            contentType: "application/json",
            dataType: "json",
            type: "POST",
            data: JSON.stringify( data ),
            complete: function() {
                input.val( parseInt(val, 10) + 1 );
                $this.prop("disabled", false);
            }
        });
    });

    if ( window.location === window.parent.location ) {
        testFrame = $("<iframe></iframe>");
        testFrame.attr("src", "http://" + window.location.hostname + ":8080/ag-push/websocket.html");
        testFrame.css({
            width: "100%",
            height: "450px",
            "margin-top": "-50px"
        });
        $("#framer").append( testFrame );
    } else {
        $(".leftDiv").css("width", "100%");
    }
})();
