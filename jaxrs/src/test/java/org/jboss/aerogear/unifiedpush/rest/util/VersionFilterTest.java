package org.jboss.aerogear.unifiedpush.rest.util;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;

public class VersionFilterTest {


    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain chain;

    @Test
    public void ignoreMissingAcceptHeader() throws Exception {
        MockitoAnnotations.initMocks(this);

        VersionFilter filter = new VersionFilter();
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}