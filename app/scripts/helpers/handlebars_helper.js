Ember.Handlebars.helper( 'headAppsTitle', function(numberOfApps) {
    if(numberOfApps > 0){
        Ember.$('head').find('title').text("Applications ("+ numberOfApps +") - AeroGear UnifiedPush Server");
    }
    else
    {
        Ember.$('head').find('title').text("Welcome! - AeroGear UnifiedPush Server");
    }
}, 'title');

//these could be probably DRYed out
var static_title = " - AeroGear UnifiedPush Server";
Ember.Handlebars.helper('headAppTitle', function( appName ) {
    Ember.$('head').find('title').text("Applications "+ appName + static_title);
}, 'title');

Ember.Handlebars.helper('headEditAppTitle', function( appName, action ) {
    Ember.$('head').find('title').text(action + " " + appName + static_title);
}, 'title');

Ember.Handlebars.helper('headVariantTitle', function( variantName ) {
    Ember.$('head').find('title').text("Variant " + variantName + static_title);
}, 'title');

Ember.Handlebars.helper('headEditVariantTitle', function( variantName, action ) {
    Ember.$('head').find('title').text(action + " " + variantName + static_title);
}, 'title');


Ember.Handlebars.helper('headInstallationTitle', function(deviceToken) {
    Ember.$('head').find('title').text("Device " + deviceToken + static_title);
}, 'title');



