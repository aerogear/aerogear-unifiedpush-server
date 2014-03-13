App.Route = Ember.Route.extend({
    activate: function(){
        this.send( "clearErrors" );
    }
});
