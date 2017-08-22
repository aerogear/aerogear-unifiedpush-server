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
package org.jboss.aerogear.unifiedpush.rest.util;

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpRequestHelperTest {

    @Test
    public void extractRemoteAddress() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn(null);
        Mockito.when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        Mockito.when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        Mockito.when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNotNull();
        assertThat(remoteAddress).isEqualTo("127.0.0.1");
    }

    @Test
    public void extractWLProxyClientIPHeader() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn(null);
        Mockito.when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        Mockito.when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("127.0.0.1");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNotNull();
        assertThat(remoteAddress).isEqualTo("127.0.0.1");
    }

    @Test
    public void extractInvalidWLProxyClientIPHeader() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn(null);
        Mockito.when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        Mockito.when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("300.400.0.15");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNull();
    }

    @Test
    public void extractInvalidWLProxyClientIPHeaderText() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn(null);
        Mockito.when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        Mockito.when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("This is not an IP4 address!");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNull();
    }

    @Test
    public void extractProxyClientIPHeader() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn(null);
        Mockito.when(request.getHeader("Proxy-Client-IP")).thenReturn("127.0.0.1");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNotNull();
        assertThat(remoteAddress).isEqualTo("127.0.0.1");
    }

    @Test
    public void extractInvalidProxyClientIPHeader() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn(null);
        Mockito.when(request.getHeader("Proxy-Client-IP")).thenReturn("300.400.0.15");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNull();
    }

    @Test
    public void extractInvalidProxyClientIPHeaderText() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn(null);
        Mockito.when(request.getHeader("Proxy-Client-IP")).thenReturn("This is not an IP4 address!");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNull();
    }

    @Test
    public void extractXForwardForHeader() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn("127.0.0.1");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNotNull();
        assertThat(remoteAddress).isEqualTo("127.0.0.1");
    }

    @Test
    public void extractInvalidXForwardForHeader() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn("300.400.0.15");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNull();
    }

    @Test
    public void extractInvalidXForwardForHeaderText() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("x-forwarded-for")).thenReturn("This is not an IP4 address!");

        final String remoteAddress = HttpRequestUtil.extractIPAddress(request);

        assertThat(remoteAddress).isNull();
    }

    @Test
    public void  extractAeroGearSenderHeader() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("aerogear-sender")).thenReturn("Java Sender");

        final String client = HttpRequestUtil.extractAeroGearSenderInformation(request);

        assertThat(client).isNotNull();
        assertThat(client).isEqualTo("Java Sender");
    }

    @Test
    public void  extractAeroGearSenderHeaderViaUserAgent() {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("aerogear-sender")).thenReturn(null);
        Mockito.when(request.getHeader("user-agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2)");

        final String client = HttpRequestUtil.extractAeroGearSenderInformation(request);

        assertThat(client).isNotNull();
        assertThat(client).isEqualTo("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2)");
    }

}
