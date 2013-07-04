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
package org.jboss.aerogear.connectivity.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.jboss.aerogear.connectivity.jpa.dao.AndroidVariantDao;
import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AndroidVariantServiceImplTest {

    @Mock
    private AndroidVariantDao androidApplicationDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddAndroidVariant() {
        when(androidApplicationDao.create(any(AndroidVariant.class))).thenReturn(mock(AndroidVariant.class));
        assertNotNull(androidApplicationDao.create(mock(AndroidVariant.class)));
        verify(androidApplicationDao).create(any(AndroidVariant.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllAndroidVariants() {
        when(androidApplicationDao.findAll()).thenReturn((List<AndroidVariant>) mock(List.class));
        assertNotNull(androidApplicationDao.findAll());
        verify(androidApplicationDao).findAll();
    }

    @Test
    public void testFindByVariantIDForDeveloper() {
        when(androidApplicationDao.findByVariantIDForDeveloper(anyString(), anyString()))
                .thenReturn(mock(AndroidVariant.class));
        assertNotNull(androidApplicationDao.findByVariantIDForDeveloper(anyString(), anyString()));
        verify(androidApplicationDao).findByVariantIDForDeveloper(anyString(), anyString());
    }

    @Test
    public void testUpdateAndroidVariant() {
        when(androidApplicationDao.update(any(AndroidVariant.class))).thenReturn(mock(AndroidVariant.class));
        assertNotNull(androidApplicationDao.update(any(AndroidVariant.class)));
        verify(androidApplicationDao).update(any(AndroidVariant.class));
    }

    @Test
    public void testRemoveAndroidVariant() {
        androidApplicationDao.delete(any(AndroidVariant.class));
        verify(androidApplicationDao).delete(any(AndroidVariant.class));
    }

}
