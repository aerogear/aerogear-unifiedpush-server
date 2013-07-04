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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jboss.aerogear.connectivity.jpa.dao.iOSVariantDao;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class iOSVariantServiceImplTest {

    @Mock
    private iOSVariantDao iOSApplicationDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddiOSVariant() {
        when(iOSApplicationDao.create(any(iOSVariant.class))).thenReturn(mock(iOSVariant.class));
        assertNotNull(iOSApplicationDao.create(mock(iOSVariant.class)));
        verify(iOSApplicationDao).create(any(iOSVariant.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAlliOSVariants() {
        when(iOSApplicationDao.findAll()).thenReturn((List<iOSVariant>) mock(List.class));
        assertNotNull(iOSApplicationDao.findAll());
        verify(iOSApplicationDao).findAll();
    }

    @Test
    public void testFindByVariantIDForDeveloper() {
        when(iOSApplicationDao.findByVariantIDForDeveloper(anyString(), anyString())).thenReturn(mock(iOSVariant.class));
        assertNotNull(iOSApplicationDao.findByVariantIDForDeveloper(anyString(), anyString()));
        verify(iOSApplicationDao).findByVariantIDForDeveloper(anyString(), anyString());
    }

    @Test
    public void testUpdateiOSVariant() {
        when(iOSApplicationDao.update(any(iOSVariant.class))).thenReturn(mock(iOSVariant.class));
        assertNotNull(iOSApplicationDao.update(any(iOSVariant.class)));
        verify(iOSApplicationDao).update(any(iOSVariant.class));
    }

    @Test
    public void testRemoveiOSVariant() {
        iOSApplicationDao.delete(any(iOSVariant.class));
        verify(iOSApplicationDao).delete(any(iOSVariant.class));
    }

}
