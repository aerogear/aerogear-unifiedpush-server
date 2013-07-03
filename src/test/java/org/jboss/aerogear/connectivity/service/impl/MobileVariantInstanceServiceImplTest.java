/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.List;

import org.jboss.aerogear.connectivity.jpa.dao.MobileVariantInstanceDao;
import org.jboss.aerogear.connectivity.model.MobileVariantInstanceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MobileVariantInstanceServiceImplTest {

    @Mock
    private MobileVariantInstanceDao mobileVariantInstanceDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddMobileVariantInstance() {
        when(mobileVariantInstanceDao.create(any(MobileVariantInstanceImpl.class))).thenReturn(
                mock(MobileVariantInstanceImpl.class));
        assertNotNull(mobileVariantInstanceDao.create(mock(MobileVariantInstanceImpl.class)));
        verify(mobileVariantInstanceDao).create(any(MobileVariantInstanceImpl.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveMobileVariantInstances() {
        List<MobileVariantInstanceImpl> instances = (List<MobileVariantInstanceImpl>) mock(List.class);
        when(instances.iterator()).thenReturn((Iterator<MobileVariantInstanceImpl>) mock(Iterator.class));
        when(instances.iterator().hasNext()).thenReturn(true, true, true, false);
        when(instances.iterator().next()).thenReturn(mock(MobileVariantInstanceImpl.class));

        for (MobileVariantInstanceImpl mobileApplicationInstance : instances) {
            mobileVariantInstanceDao.delete(mobileApplicationInstance);
        }

        verify(mobileVariantInstanceDao, times(3)).delete(any(MobileVariantInstanceImpl.class));
    }

    @Test
    public void testUpdateMobileVariantInstance() {
        when(mobileVariantInstanceDao.update(any(MobileVariantInstanceImpl.class))).thenReturn(
                mock(MobileVariantInstanceImpl.class));
        assertNotNull(mobileVariantInstanceDao.update(any(MobileVariantInstanceImpl.class)));
        verify(mobileVariantInstanceDao).update(any(MobileVariantInstanceImpl.class));
    }

}
