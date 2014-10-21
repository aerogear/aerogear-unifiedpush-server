package org.jboss.aerogear.unifiedpush.service.filter;


import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter only to hold the HTTPServletContext and update the SearchManager
 * The goal is to provide multiple views between admin and developer
 * Unfortunately the version of WELD on AS7 does not support HTTPServletContext injection
 */
@WebFilter(filterName = "HttpContextHolderFilter", urlPatterns = {"/*"})
public class HttpContextFilter implements Filter {

    @Inject
    private SearchManager searchManager;

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        searchManager.setHttpServletRequest(httpServletRequest);

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    public void destroy() {
    }

}