/*
An Object Representing a list of Mobile Apps
*/


App.MobileApplications = Ember.Object.extend({});

App.MobileApplications.reopenClass({
    find: function() {
        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            result;

        result = Ember.Object.create({
            isLoaded: false
        });

        applicationPipe.read({
            success: function( response ) {
                var apps = [];
                console.log( "application reponse", response );
                response.forEach( function( app ) {
                    apps.push( App.MobileApplication.create( app ));
                });
                result.setProperties( { "apps": apps, isLoaded: true  });
            },
            error: function( error ) { // TODO: Maybe Make this a class method?
                console.log( "error with application endpoint", error );
                switch( error.status ) {
                case 401:
                    App.Router.router.transitionTo("login");
                    break;
                default:
                    //that.transitionTo( "login" );
                    result.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });

        return result;
    }
});

App.MobileApplication = Ember.Object.extend({});

App.MobileApplication.reopenClass({
    find: function( applicationPushId ) {

        var applicationPipe = App.AeroGear.pipelines.pipes.applications,
            result;

        result = Ember.Object.create({
            isLoaded: false
        });

        applicationPipe.read({
            id: applicationPushId,
            success: function( response ) {
                console.log( "application reponse", response );
                result.setProperties( { "apps": response, isLoaded: true  });
                //result.set( "isLoaded", true );
            },
            error: function( error ) { // TODO: Maybe Make this a class method?
                console.log( "error with application endpoint", error );
                switch( error.status ) {
                case 401:
                    App.Router.router.transitionTo("login");
                    break;
                default:
                    //that.transitionTo( "login" );
                    result.setProperties( { isLoaded: true, error: error } );
                    break;
                }
            }
        });

        return result;
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
