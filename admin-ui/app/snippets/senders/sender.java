JavaSender defaultJavaSender = new SenderClient.Builder("{{ contextPath }}").build();
UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
        .pushApplicationId("{{ app.pushApplicationID }}")
        .masterSecret("{{ app.masterSecret }}")
        .alert("Hello from Java Sender API!")
        .build();
defaultJavaSender.send(unifiedMessage, new MessageResponseCallback() {

    @Override
    public void onComplete(int statusCode) {
        //do cool stuff
    }

    @Override
    public void onError(Throwable throwable) {
        //bring out the bad news
    }
});
