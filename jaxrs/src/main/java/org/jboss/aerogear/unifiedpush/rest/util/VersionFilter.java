package org.jboss.aerogear.unifiedpush.rest.util;

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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String accept = httpRequest.getHeader("accept");
        if (accept.contains(AEROGEAR_VERSION_PREFIX)) {
            chain.doFilter(new TransformHttpServletRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
   }

    private static class TransformHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final StringBuilder data = new StringBuilder();

        public TransformHttpServletRequestWrapper(HttpServletRequest httpRequest) throws IOException {
            super(httpRequest);

            StringBuilder jsonRequest = new StringBuilder();

            final BufferedReader reader = httpRequest.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonRequest.append(line);
            }

            if (jsonRequest.length() > 0) {
//                List transformSpec = JsonUtils.classpathToList("/transform.json");
//                Chainr chainr = Chainr.fromSpec(transformSpec);
//
//                final Object transform = chainr.transform(JsonUtils.jsonToObject(jsonRequest.toString()));
//                System.out.println("transform = " + transform);
//                data.append(JsonUtils.toJsonString(transform));
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
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
