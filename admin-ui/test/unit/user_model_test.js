module( "User Model Tests" );

test( "user model", function(){
    var user = App.User.create({
        loginName: "admin",
        password: "12345",
        confirmPassword: "12345"
    });
    equal( user.get( "loginName" ), "admin", "loginName is instead " + user.get( "loginName" ) );
    equal( user.get( "password" ), "12345", "password is instead " + user.get( "password" ) );
    equal( user.get( "confirmPassword" ), "12345", "confirmPassword is instead " + user.get( "confirmPassword" ) );
});

test( "valid user model validations - presence and match ", function(){
    var user = App.User.create({
        loginName: "admin",
        password: "12345",
        confirmPassword: "12345"
    });

    user.validate();

    equal( user.get( "isValid" ), true, "isValid is instead" + user.get( "isValid" ) );
});

test( "invalid user model validations - presence", function(){
    var user = App.User.create({
        loginName: "",
        password: "",
        confirmPassword: ""
    });

    user.validate();

    equal( user.get( "isValid" ), false, "isValid is instead" + user.get( "isValid" ) );
    equal( user.get( "validationErrors.allMessages" ).length, 3, "validationErrors.allMessages is instead" + user.get( "validationErrors.allMessages" ).length );
});

test( "invalid user model validations - match", function(){
    var user = App.User.create({
        loginName: "admin",
        password: "123",
        confirmPassword: "321"
    });

    user.validate();

    equal( user.get( "isValid" ), false, "isValid is instead" + user.get( "isValid" ) );
    equal( user.get( "validationErrors.allMessages" ).length, 1, "validationErrors.allMessages is instead" + user.get( "validationErrors.allMessages" ).length );
});
