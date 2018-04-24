/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.token;

import com.google.android.gcm.server.Constants;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

public class TokenLoaderUtilsTest {

    @Test
    public void testEmptyCriteria() {
        final Criteria criteria = new Criteria();
        assertThat(TokenLoaderUtils.isEmptyCriteria(criteria)).isTrue();
    }

    @Test
    public void testNonEmptyCriteria() {
        final Criteria criteria = new Criteria();
        criteria.setAliases(Arrays.asList("foo", "bar"));
        assertThat(TokenLoaderUtils.isEmptyCriteria(criteria)).isFalse();
    }

    @Test
    public void testNotCategoryOnly() {
        final Criteria criteria = new Criteria();
        criteria.setAliases(Arrays.asList("foo", "bar"));
        assertThat(TokenLoaderUtils.isCategoryOnlyCriteria(criteria)).isFalse();
    }

    @Test
    public void testCategoryOnly() {
        final Criteria criteria = new Criteria();
        criteria.setCategories(Arrays.asList("football"));
        assertThat(TokenLoaderUtils.isCategoryOnlyCriteria(criteria)).isTrue();
    }

    @Test
    public void testNotCategoryOnlyForEmpyt() {
        final Criteria criteria = new Criteria();
        assertThat(TokenLoaderUtils.isCategoryOnlyCriteria(criteria)).isFalse();
    }

    @Test
    public void testGcmTopicExtractionForEmptyCriteria() {
        final Criteria criteria = new Criteria();

        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).isNotEmpty();
        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).containsOnly(
                Constants.TOPIC_PREFIX+"123"
        );
    }

    @Test
    public void testGcmTopicExtractionForCriteriaWithCategories() {
        final Criteria criteria = new Criteria();
        criteria.setCategories(Arrays.asList("football"));

        assertThat(TokenLoaderUtils.isCategoryOnlyCriteria(criteria)).isTrue();

        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).isNotEmpty();
        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).containsOnly(
                Constants.TOPIC_PREFIX+"football"
        );
    }

    @Test
    public void testGcmTopicExtractionForCriteriaWithAlias() {
        final Criteria criteria = new Criteria();
        criteria.setAliases(Arrays.asList("foo@bar.org"));

        assertThat(TokenLoaderUtils.isCategoryOnlyCriteria(criteria)).isFalse();

        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).isEmpty();
    }

    @Test
    public void testGCMTopic() {
        final Criteria criteria = new Criteria();
        assertThat(TokenLoaderUtils.isFCMTopicRequest(criteria)).isTrue();
        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).containsOnly(
                Constants.TOPIC_PREFIX+"123"
        );
    }

    @Test
    public void testGCMTopicForCategory() {
        final Criteria criteria = new Criteria();
        criteria.setCategories(Arrays.asList("football"));
        assertThat(TokenLoaderUtils.isFCMTopicRequest(criteria)).isTrue();
        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).containsOnly(
                Constants.TOPIC_PREFIX+"football"
        );
    }

    @Test
    public void testGCMTopicForAlias() {
        final Criteria criteria = new Criteria();
        criteria.setAliases(Arrays.asList("foo@bar.org"));
        assertThat(TokenLoaderUtils.isFCMTopicRequest(criteria)).isFalse();
    }

    @Test
    public void testGCMTopicForVariant() {
        final Criteria criteria = new Criteria();
        criteria.setVariants(Arrays.asList("variant1", "variant2"));
        assertThat(TokenLoaderUtils.isFCMTopicRequest(criteria)).isTrue();
    }

    @Test
    public void testGCMTopicForVariantAndCategory() {
        final Criteria criteria = new Criteria();
        criteria.setVariants(Arrays.asList("variant1", "variant2"));
        criteria.setCategories(Arrays.asList("football"));

        assertThat(TokenLoaderUtils.isFCMTopicRequest(criteria)).isTrue();
        assertThat(TokenLoaderUtils.extractFCMTopics(criteria, "123")).containsOnly(
                Constants.TOPIC_PREFIX+"football"
        );

    }

    @Test
    public void testGCMTopicForVariantAndAlias() {
        final Criteria criteria = new Criteria();
        criteria.setVariants(Arrays.asList("variant1", "variant2"));
        criteria.setAliases(Arrays.asList("foo@bar.org"));

        assertThat(TokenLoaderUtils.isFCMTopicRequest(criteria)).isFalse();
    }
}
