App.HeaderClicker = Ember.View.extend({
    tagName: "h3",
    classNames: [ "topcoat-list__header" ],
    click: function( event ) {
        console.log( event );
    }
});

App.MobileAppsView = Ember.View.extend({
    lastName: "H"
});
