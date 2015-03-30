using AeroGear.Push;

protected async override void OnLaunched(LaunchActivatedEventArgs e)
{
  PushConfig pushConfig = new PushConfig() { UnifiedPushUri = new Uri("{{ exampleCtrl.currentLocation }}"), VariantId = "{{ exampleCtrl.variant.variantID }}", VariantSecret = "{{ exampleCtrl.variant.secret }}" };
  Registration registration = new MpnsRegistration();
  registration.PushReceivedEvent += HandleNotification;
  await registration.Register(pushConfig);

  ...
}

void HandleNotification(object sender, PushReceivedEvent e)
{
  Debug.WriteLine("notification received {0}", e.Args.message);
}
