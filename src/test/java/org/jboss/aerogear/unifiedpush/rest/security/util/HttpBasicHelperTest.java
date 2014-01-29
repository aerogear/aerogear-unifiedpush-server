/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.rest.security.util;

import net.iharder.Base64;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class HttpBasicHelperTest {

    @Test
    public void extractUsernameAndPasswordFromBasicHeader() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final String encodedUserPassword = Base64.encodeBytes("user:password".getBytes());
        when(request.getHeader("Authorization")).thenReturn("Basic " + encodedUserPassword);

        final String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        assertNotNull(credentials);
        assertEquals("user", credentials[0]);
        assertEquals("password", credentials[1]);
        assertNotEquals(" user", credentials[0]);
        assertNotEquals(" password ", credentials[1]);
    }

    @Test
    public void tryToExtractUsernameAndPasswordFromEmptyHeader() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("");

        final String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        assertNotNull(credentials);
        assertEquals("", credentials[0]);
        assertEquals("", credentials[1]);
    }

    @Test
    public void tryToExtractUsernameAndPasswordFromNullHeader() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        final String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        assertNotNull(credentials);
        assertEquals("", credentials[0]);
        assertEquals("", credentials[1]);
    }
}
