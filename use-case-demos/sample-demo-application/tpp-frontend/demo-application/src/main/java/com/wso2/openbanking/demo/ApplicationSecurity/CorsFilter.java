package com.wso2.openbanking.demo.ApplicationSecurity;

import com.wso2.openbanking.demo.utils.ConfigLoader;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsFilter implements Filter {

    private static final String HEADER_ALLOW_ORIGIN      = "Access-Control-Allow-Origin";
    private static final String HEADER_ALLOW_METHODS     = "Access-Control-Allow-Methods";
    private static final String HEADER_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String HEADER_ALLOW_HEADERS     = "Access-Control-Allow-Headers";

    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String ALLOWED_HEADERS = "Content-Type, Authorization, X-Requested-With";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest)
                || !(servletResponse instanceof HttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        httpResponse.setHeader(HEADER_ALLOW_ORIGIN, ConfigLoader.getCorsAllowedOrigin());
        httpResponse.setHeader(HEADER_ALLOW_METHODS, ALLOWED_METHODS);
        httpResponse.setHeader(HEADER_ALLOW_CREDENTIALS, "true");
        httpResponse.setHeader(HEADER_ALLOW_HEADERS, ALLOWED_HEADERS);

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
