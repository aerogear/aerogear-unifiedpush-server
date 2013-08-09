module( "Mobile App Model Tests" );

test( "MobileAppliction Model - Name/description", function() {
    var mobileApp = App.MobileApplication.create({
        name: "Push Application",
        description: "Push Application Description"
    });

    equal( mobileApp.get( "name" ), "Push Application", "was instead " + mobileApp.get( "name" ) );
    equal( mobileApp.get( "description" ), "Push Application Description", "was instead" + mobileApp.get( "description" ) );
});

test( "MobileAppliction Model - Empty Contents - total Variants", function() {
    var mobileApp = App.MobileApplication.create({
        contents: []
    });

    equal( mobileApp.get( "totalAndroidVariants" ), 0, "totalAndroidVariants should be 0, but returned" + mobileApp.get( "totalAndroidVariants" ) );
    equal( mobileApp.get( "totaliOSVariants" ), 0, "totaliOSVariants should be 0, but returned" + mobileApp.get( "totaliOSVariants" ) );
    equal( mobileApp.get( "totalSimplePushVariants" ), 0, "totalSimplePushVariants should be 0, but returned" + mobileApp.get( "totalSimplePushVariants" ) );
    equal( mobileApp.get( "totalVariants" ), 0, "should be 0, but returned" + mobileApp.get( "totalVariants" ) );
    equal( mobileApp.get( "variantList" ).length, 0, "variantList should be 0, but returned" + mobileApp.get( "variantList" ) );
});

//TODO: Need to test computed properties with values

test( "Valid MobileApplication Model validation", function() {
    var mobileApp = App.MobileApplication.create({
        name: "Push Application",
        description: "Push Application Description"
    });

    mobileApp.validate();
    equal( mobileApp.get( "isValid" ), true, "should be no errors but isValid is instead " + mobileApp.get( "isValid" ) );
});


test( "inValid MobileApplication Model validation - No Name", function() {
    var mobileApp = App.MobileApplication.create({});

    mobileApp.validate();
    equal( mobileApp.get( "isValid" ), false, "should be have an error but isValid is instead " + mobileApp.get( "isValid" ) );
});

test( "inValid MobileApplication Model validation - Long Description", function() {
    var mobileApp = App.MobileApplication.create({}),
        str;

    for (var i = 0; i < 256; i++) {
        str += ">";
    }

    mobileApp.set( "description", str );

    mobileApp.validateProperty( "description" );
    equal( mobileApp.get( "validationErrors.allMessages" ).length, 1, "should only have 1 error" );
    equal( mobileApp.get( "isValid" ), false, "should be have an error but isValid is instead " + mobileApp.get( "isValid" ) );
});



