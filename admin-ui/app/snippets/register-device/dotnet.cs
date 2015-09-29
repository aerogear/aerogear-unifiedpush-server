using AeroGear.Push;

protected async override void OnLaunched(LaunchActivatedEventArgs e)
{
  var pushConfig = new PushConfig()
  {
    UnifiedPushUri = new Uri("{{ contextPath}}"),
    VariantId = "{{ variant.variantID }}",
    VariantSecret = "{{ variant.secret }}"
  };
  Registration registration = new Registration();
  registration.PushReceivedEvent += HandleNotification;
  await registration.Register(pushConfig);

  ...
}

void HandleNotification(object sender, PushReceivedEvent e)
{
  Debug.WriteLine("notification received {0}", e.Args.Message);
}
