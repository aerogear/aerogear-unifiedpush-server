/* JBoss, Home of Professional Open Source
* Copyright Red Hat, Inc., and individual contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

Ember.ValidationError.addMessage('match', "Fields must be the same");

Ember.Validators.MatchValidator = Ember.Validator.extend({
  /**
     @param {Object} object
      The object which contains the attribute that has to be validated
     @param {String} attribute
      The attribute path on which the validation should be done
     @param {Object} value
      The value of the attribute
  */
    shouldSkipValidations: function() {
        return false;
    },

    _validate: function( obj, attr ) {
      // TODO: this should be more generic
        if( obj.get( "password" ) !== obj.get( "confirmPassword" ) ) {
            obj.get('validationErrors').add(attr, "match");
        }
    }
});
