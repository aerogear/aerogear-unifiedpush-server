App.SnippetsView = Ember.View.extend({
    didInsertElement: function() {
        $('pre code').each(function(i, e) {hljs.highlightBlock(e);});
    }
});