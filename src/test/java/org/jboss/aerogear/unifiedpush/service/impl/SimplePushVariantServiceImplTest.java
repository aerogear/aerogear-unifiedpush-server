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
package org.jboss.aerogear.unifiedpush.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jboss.aerogear.unifiedpush.jpa.dao.SimplePushVariantDao;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SimplePushVariantServiceImplTest {

    @Mock
    private SimplePushVariantDao simplePushApplicationDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddSimplePushVariant() {
        when(simplePushApplicationDao.create(any(SimplePushVariant.class))).thenReturn(mock(SimplePushVariant.class));
        assertNotNull(simplePushApplicationDao.create(mock(SimplePushVariant.class)));
        verify(simplePushApplicationDao).create(any(SimplePushVariant.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllSimplePushVariants() {
        when(simplePushApplicationDao.findAll()).thenReturn((List<SimplePushVariant>) mock(List.class));
        assertNotNull(simplePushApplicationDao.findAll());
        verify(simplePushApplicationDao).findAll();
    }

    @Test
    public void testFindByVariantIDForDeveloper() {
        when(simplePushApplicationDao.findByVariantIDForDeveloper(anyString(), anyString())).thenReturn(
                mock(SimplePushVariant.class));
        assertNotNull(simplePushApplicationDao.findByVariantIDForDeveloper(anyString(), anyString()));
        verify(simplePushApplicationDao).findByVariantIDForDeveloper(anyString(), anyString());
    }

    @Test
    public void testUpdateSimplePushVariant() {
        when(simplePushApplicationDao.update(any(SimplePushVariant.class))).thenReturn(mock(SimplePushVariant.class));
        assertNotNull(simplePushApplicationDao.update(any(SimplePushVariant.class)));
        verify(simplePushApplicationDao).update(any(SimplePushVariant.class));
    }

    @Test
    public void testRemoveSimplePushVariant() {
        simplePushApplicationDao.delete(any(SimplePushVariant.class));
        verify(simplePushApplicationDao).delete(any(SimplePushVariant.class));
    }

}
