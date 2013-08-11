module( "Mobile Variant Instance Model Tests" );

test( "MobileVariantInstnce Model", function() {
    var mobileAppVariantInstance = App.MobileVariantInstance.create({});

    equal( mobileAppVariantInstance.get( "status" ), "Disabled", "should be Disabled, instead " +  mobileAppVariantInstance.get( "status" ) );
    equal( mobileAppVariantInstance.get( "vType" ), "N/A", "should be N/A, instead " + mobileAppVariantInstance.get( "vType" ) );
});
