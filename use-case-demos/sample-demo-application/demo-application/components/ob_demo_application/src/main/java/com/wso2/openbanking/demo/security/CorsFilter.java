/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.demo.security;

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

/** CorsFilter implementation. */
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

    /**
     * Applies CORS headers to each response and short-circuits preflight OPTIONS requests.
     *
     * @param servletRequest  the incoming servlet request
     * @param servletResponse the outgoing servlet response
     * @param filterChain     the filter chain to pass the request and response to the next filter
     * @throws IOException      if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
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

    /**
     * Executes the destroy operation and modify the payload if necessary.
     */
    @Override
    public void destroy() {
    }
}
