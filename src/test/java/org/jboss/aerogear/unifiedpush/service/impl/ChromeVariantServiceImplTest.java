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

import org.jboss.aerogear.unifiedpush.jpa.dao.ChromePackagedAppVariantDao;
import org.jboss.aerogear.unifiedpush.model.ChromePackagedAppVariant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ChromeVariantServiceImplTest {

    @Mock
    private ChromePackagedAppVariantDao chromePackagedAppVariantDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddChromePackagedAppVariant() {
        when(chromePackagedAppVariantDao.create(any(ChromePackagedAppVariant.class))).thenReturn(mock(ChromePackagedAppVariant.class));
        assertNotNull(chromePackagedAppVariantDao.create(mock(ChromePackagedAppVariant.class)));
        verify(chromePackagedAppVariantDao).create(any(ChromePackagedAppVariant.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllAndroidVariants() {
        when(chromePackagedAppVariantDao.findAll()).thenReturn((List<ChromePackagedAppVariant>) mock(List.class));
        assertNotNull(chromePackagedAppVariantDao.findAll());
        verify(chromePackagedAppVariantDao).findAll();
    }

    @Test
    public void testFindByVariantIDForDeveloper() {
        when(chromePackagedAppVariantDao.findByVariantIDForDeveloper(anyString(), anyString()))
                .thenReturn(mock(ChromePackagedAppVariant.class));
        assertNotNull(chromePackagedAppVariantDao.findByVariantIDForDeveloper(anyString(), anyString()));
        verify(chromePackagedAppVariantDao).findByVariantIDForDeveloper(anyString(), anyString());
    }

    @Test
    public void testUpdateAndroidVariant() {
        when(chromePackagedAppVariantDao.update(any(ChromePackagedAppVariant.class))).thenReturn(mock(ChromePackagedAppVariant.class));
        assertNotNull(chromePackagedAppVariantDao.update(any(ChromePackagedAppVariant.class)));
        verify(chromePackagedAppVariantDao).update(any(ChromePackagedAppVariant.class));
    }

    @Test
    public void testRemoveAndroidVariant() {
        chromePackagedAppVariantDao.delete(any(ChromePackagedAppVariant.class));
        verify(chromePackagedAppVariantDao).delete(any(ChromePackagedAppVariant.class));
    }
}
