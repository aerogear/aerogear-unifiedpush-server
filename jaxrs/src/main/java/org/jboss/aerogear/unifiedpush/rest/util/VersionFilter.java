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

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This filter will change the older api versions to the current one using json diff transform.
 */
@WebFilter(urlPatterns = "*")
public class VersionFilter implements Filter {

    private static final String AEROGEAR_VERSION_PREFIX = "aerogear.v";
    public static final int VERSION_LENGTH = 3;

    @Inject
    private RequestTransformer requestTransformer;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String accept = httpRequest.getHeader("accept");
        if (accept != null) {
            final int index = accept.indexOf(AEROGEAR_VERSION_PREFIX);
            if (index != -1) {
                final int beginIndex = index + AEROGEAR_VERSION_PREFIX.length();
                final String version = accept.substring(beginIndex, beginIndex + VERSION_LENGTH);
                chain.doFilter(new TransformHttpServletRequestWrapper(version, httpRequest), response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
   }

    private class TransformHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final StringBuilder jsonRequest = new StringBuilder();
        private final String path;
        private final String version;

        public TransformHttpServletRequestWrapper(String version, HttpServletRequest httpRequest) throws IOException {
            super(httpRequest);

            final BufferedReader reader = httpRequest.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonRequest.append(line);
            }

            final String contextPath = httpRequest.getContextPath();
            this.path = httpRequest.getRequestURI().substring(contextPath.length());
            this.version = version;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            StringBuilder data = requestTransformer.transform(path, version, jsonRequest);
            final byte[] bytes = data.toString().getBytes();
            return new ServletInputStream() {
                private InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));

                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }
            };
        }

    }
}
