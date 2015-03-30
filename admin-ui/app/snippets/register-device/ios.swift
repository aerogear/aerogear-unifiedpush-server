func application(application: UIApplication!, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData!) {
    println("APNS Success")

    let registration = AGDeviceRegistration(serverURL: NSURL(string: "{{ exampleCtrl.currentLocation }}"))

    registration.registerWithClientInfo({ (clientInfo: AGClientDeviceInformation!)  in

        // apply the token, to identify this device
        clientInfo.deviceToken = deviceToken

        clientInfo.variantID = "{{ exampleCtrl.variant.variantID }}"
        clientInfo.variantSecret = "{{ exampleCtrl.variant.secret }}"

        // --optional config--
        // set some 'useful' hardware information params
        let currentDevice = UIDevice()
        clientInfo.operatingSystem = currentDevice.systemName
        clientInfo.osVersion = currentDevice.systemVersion
        clientInfo.deviceType = currentDevice.model

    }, success: {
        println("UPS registration worked");

    }, failure: { (error:NSError!) -> () in
        println("UPS registration Error: \(error.localizedDescription)")
    })
}
