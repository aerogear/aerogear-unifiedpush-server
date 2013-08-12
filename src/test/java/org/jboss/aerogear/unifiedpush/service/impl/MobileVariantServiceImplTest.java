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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.jpa.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MobileVariantServiceImplTest {

    @Mock
    private VariantDao mobileApplicationDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindByVariantID() {
        when(mobileApplicationDao.findByVariantID(anyString())).thenReturn(mock(Variant.class));
        assertNotNull(mobileApplicationDao.findByVariantID(anyString()));
        verify(mobileApplicationDao).findByVariantID(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddInstance() {
        Variant mobileVariant = mock(Variant.class);
        InstallationImpl instance = mock(InstallationImpl.class);
        when(mobileVariant.getInstallations()).thenReturn((Set<InstallationImpl>) mock(Set.class));
        when(mobileVariant.getInstallations().add(any(InstallationImpl.class))).thenReturn(true);
        assertTrue(mobileVariant.getInstallations().add(instance));
        verify(mobileVariant, times(2)).getInstallations();
        verify(mobileVariant.getInstallations()).add(instance);
        when(mobileApplicationDao.update(any(Variant.class))).thenReturn(mock(Variant.class));
        assertNotNull(mobileApplicationDao.update(any(Variant.class)));
        verify(mobileApplicationDao).update(any(Variant.class));
    }
}
