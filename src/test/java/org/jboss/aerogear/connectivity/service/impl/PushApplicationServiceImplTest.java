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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.jboss.aerogear.connectivity.jpa.dao.PushApplicationDao;
import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.model.SimplePushVariant;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PushApplicationServiceImplTest {

    @Mock
    private PushApplicationDao pushApplicationDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddPushApplication() {
        when(pushApplicationDao.create(any(PushApplication.class))).thenReturn(mock(PushApplication.class));
        assertNotNull(pushApplicationDao.create(mock(PushApplication.class)));
        verify(pushApplicationDao).create(any(PushApplication.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllPushApplications() {
        when(pushApplicationDao.findAll()).thenReturn((List<PushApplication>) mock(List.class));
        assertNotNull(pushApplicationDao.findAll());
        verify(pushApplicationDao).findAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllPushApplicationsForDeveloper() {
        when(pushApplicationDao.findAllForDeveloper(anyString())).thenReturn((List<PushApplication>) mock(List.class));
        assertNotNull(pushApplicationDao.findAllForDeveloper(anyString()));
        verify(pushApplicationDao).findAllForDeveloper(anyString());
    }

    @Test
    public void testFindByPushApplicationIDForDeveloper() {
        when(pushApplicationDao.findByPushApplicationIDForDeveloper(anyString(), anyString())).thenReturn(
                mock(PushApplication.class));
        assertNotNull(pushApplicationDao.findByPushApplicationIDForDeveloper(anyString(), anyString()));
        verify(pushApplicationDao).findByPushApplicationIDForDeveloper(anyString(), anyString());
    }

    @Test
    public void testFindByPushApplicationID() {
        when(pushApplicationDao.findByPushApplicationID(anyString())).thenReturn(mock(PushApplication.class));
        assertNotNull(pushApplicationDao.findByPushApplicationID(anyString()));
        verify(pushApplicationDao).findByPushApplicationID(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddiOSVariant() {
        PushApplication pushApplication = mock(PushApplication.class);
        iOSVariant iOVariant = mock(iOSVariant.class);
        when(pushApplication.getIOSApps()).thenReturn((Set<iOSVariant>) mock(Set.class));
        when(pushApplication.getIOSApps().add(any(iOSVariant.class))).thenReturn(true);
        assertTrue(pushApplication.getIOSApps().add(iOVariant));
        verify(pushApplication, times(2)).getIOSApps();
        verify(pushApplication.getIOSApps()).add(iOVariant);
        when(pushApplicationDao.update(any(PushApplication.class))).thenReturn(mock(PushApplication.class));
        assertNotNull(pushApplicationDao.update(any(PushApplication.class)));
        verify(pushApplicationDao).update(any(PushApplication.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddAndroidVariant() {
        PushApplication pushApplication = mock(PushApplication.class);
        AndroidVariant androidVariant = mock(AndroidVariant.class);
        when(pushApplication.getAndroidApps()).thenReturn((Set<AndroidVariant>) mock(Set.class));
        when(pushApplication.getAndroidApps().add(any(AndroidVariant.class))).thenReturn(true);
        assertTrue(pushApplication.getAndroidApps().add(androidVariant));
        verify(pushApplication, times(2)).getAndroidApps();
        verify(pushApplication.getAndroidApps()).add(androidVariant);
        when(pushApplicationDao.update(any(PushApplication.class))).thenReturn(mock(PushApplication.class));
        assertNotNull(pushApplicationDao.update(any(PushApplication.class)));
        verify(pushApplicationDao).update(any(PushApplication.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddSimplePushVariant() {
        PushApplication pushApplication = mock(PushApplication.class);
        SimplePushVariant simplePushVariant = mock(SimplePushVariant.class);
        when(pushApplication.getSimplePushApps()).thenReturn((Set<SimplePushVariant>) mock(Set.class));
        when(pushApplication.getSimplePushApps().add(any(SimplePushVariant.class))).thenReturn(true);
        assertTrue(pushApplication.getSimplePushApps().add(simplePushVariant));
        verify(pushApplication, times(2)).getSimplePushApps();
        verify(pushApplication.getSimplePushApps()).add(simplePushVariant);
        when(pushApplicationDao.update(any(PushApplication.class))).thenReturn(mock(PushApplication.class));
        assertNotNull(pushApplicationDao.update(any(PushApplication.class)));
        verify(pushApplicationDao).update(any(PushApplication.class));
    }

    @Test
    public void testUpdatePushApplication() {
        when(pushApplicationDao.update(any(PushApplication.class))).thenReturn(mock(PushApplication.class));
        assertNotNull(pushApplicationDao.update(any(PushApplication.class)));
        verify(pushApplicationDao).update(any(PushApplication.class));
    }

    @Test
    public void removePushApplication() {
        pushApplicationDao.delete(any(PushApplication.class));
        verify(pushApplicationDao).delete(any(PushApplication.class));
    }
}
