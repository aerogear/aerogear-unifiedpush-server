module( "Mobile Variant Model Tests" );

test( "MobileVariant Model", function() {
    var mobileAppVariant = App.MobileVariant.create({
        name: "Push Application Variant",
        description: "Push Application Variant Description",
        //These would never be togehter in real life
        googleKey: "12345",
        passphrase: "secret",
        certificate: "cert_value",
        pushNetworkURL: "http://localhost"
    });

    equal( mobileAppVariant.get( "name" ), "Push Application Variant", "was instead " + mobileAppVariant.get( "name" ) );
    equal( mobileAppVariant.get( "description" ), "Push Application Variant Description", "was instead" + mobileAppVariant.get( "description" ) );
    equal( mobileAppVariant.get( "googleKey" ), "12345", "was instead " + mobileAppVariant.get( "googleKey" ) );
    equal( mobileAppVariant.get( "passphrase" ), "secret", "was instead " + mobileAppVariant.get( "passphrase" ) );
    equal( mobileAppVariant.get( "certificate" ), "cert_value", "was instead " + mobileAppVariant.get( "certificate" ) );
    equal( mobileAppVariant.get( "pushNetworkURL" ), "http://localhost", "was instead " + mobileAppVariant.get( "pushNetworkURL" ) );
});




