Ember.Handlebars.helper( 'headAppsTitle', function(numberOfApps) {
    if(numberOfApps > 0){
        Ember.$('head').find('title').text("Applications ("+ numberOfApps +") - AeroGear Unified Push Server");
    }
}, 'title');

//these could be probably DRYed out

Ember.Handlebars.helper('headAppTitle', function( appName ) {
    Ember.$('head').find('title').text("Applications "+ appName +" - AeroGear Unified Push Server");
}, 'title');

Ember.Handlebars.helper('headEditAppTitle', function( appName, action ) {
    Ember.$('head').find('title').text(action + " " + appName +" - AeroGear Unified Push Server");
}, 'title');

Ember.Handlebars.helper('headVariantTitle', function( variantName ) {
    Ember.$('head').find('title').text("Variant " + variantName+ " - AeroGear Unified Push Server");
}, 'title');

Ember.Handlebars.helper('headEditVariantTitle', function( variantName, action ) {
    Ember.$('head').find('title').text(action + " " + variantName+ " - AeroGear Unified Push Server");
}, 'title');


Ember.Handlebars.helper('headInstallationTitle', function(deviceToken) {
    Ember.$('head').find('title').text("Device " + deviceToken+ " - AeroGear Unified Push Server");
}, 'title');

