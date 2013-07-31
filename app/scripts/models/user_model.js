App.User = Ember.Object.extend( Ember.Validations, {
    validations: {
        loginName: {
            presence: true
        },
        password: {
            presence: true
        }
    }
});
