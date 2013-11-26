module('App.LoginController', {
    setup: function() {
        App.reset();
        var controller = App.__container__.lookup("controller:login");
        this.controller = controller;
    },
    teardown: function() {
        //$.mockjaxClear();
    }
});

test( "visit login page", function() {
    visit( "login" ).then( function() {
        equal( exists( ".login" ), true, "Login form should visible on first load, but isn't" );
    });
});

test( "submit empty login page", function() {
    var that = this;
    visit( "login" )
    .click( ".submit" )
    .then( function() {
        that.model = that.controller.get( "model" );
        equal( that.model.get( "isValid" ), false, "shouldn't be valid, but is" );
        equal( exists( ".errors" ), true, "should be errors, but not" );
    });
});

test( "Valid first time login flow", function() {
    var that = this;
    visit( "login" )
    .fillIn( ".loginName", "admin" )
    .fillIn( ".password", "123" )
    .click( ".submit" )
    .then( function() {
        that.model = that.controller.get( "model" );
        equal( that.controller.get( "loginIn" ), false, "should be false" + that.controller.get( "loginIn" ) );
        equal( exists( ".confirm" ), true, "Confirm form should be here but it isn't" );
    }).then( function() {
        fillIn( ".password", "1234" )
        .fillIn( ".confirmPassword", "1234" )
        .click( ".submit" )
        .then( function() {
            equal( that.model.get( "isValid" ), true, "should be valid, but isn't" );
            equal( that.controller.get( "relog" ), true, "relog var should be true, but it's " + that.controller.get( "relog" ) );
            equal( exists( ".login" ), true, "Login form should be here but it isn't" );
            //get login again text?
            fillIn( ".loginName", "admin" )
            .fillIn( ".password", "1234" )
            .click( ".submit" )
            .then( function() {
                equal( exists( ".login" ), false, "Login form should be here but it isn't" );
                equal( that.controller.get( "relog" ), false, "relog var should be false, but it's " + that.controller.get( "relog" ) );
                equal( exists( ".message" ), true, "message should be here but it isn't" );
            })
        })
      })
});
