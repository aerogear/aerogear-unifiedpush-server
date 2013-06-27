/*
 A Mobile App
*/
App.MobileApplication = Ember.Object.extend();

App.MobileApplication.reopenClass({
    find: function( applicationPushId ) {

        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            mobileApplication;

        if( applicationPushId ) {
            // Looking for 1
            mobileApplication = Ember.Object.create();
        } else {
            //Looking for all
            mobileApplication = Ember.ArrayProxy.create({ content: [] });
        }

        applicationPipe.read({
            id: applicationPushId,
            success: function( response ) {
                if( AeroGear.isArray( response ) ) {
                    response.forEach( function( data ) {
                        data.isLoaded = true;
                        mobileApplication.pushObject( App.MobileApplication.create( data ) );
                    });
                } else {
                    response.isLoaded = true;
                    mobileApplication.setProperties( response );
                }
            },
            error: function( error ) { // TODO: Maybe Make this a class method?
                console.log( "error with application endpoint", error );
                switch( error.status ) {
                case 401:
                    App.Router.router.transitionTo("login");
                    break;
                default:
                    //that.transitionTo( "login" );
                    //result.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });

        return mobileApplication;
    },
    remove: function( applicationPushId ) {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications;//,
            //that = this;

        applicationPipe.remove({
            id: applicationPushId,
            success: function( response ) {
                console.log( "remove response", response );
            },
            error: function( error ) { // TODO: Maybe Make this a class method?
                console.log( "error with application endpoint", error );
                switch( error.status ) {
                case 401:
                    App.Router.router.transitionTo("login");
                    break;
                default:
                    //that.transitionTo( "login" );
                    //result.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });
    }
});
