package com.wso2.openbanking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SessionSecurityFilter implements Filter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        HttpSession session = httpRequest.getSession(true);
        boolean isAuthenticated = (session != null && session.getAttribute("accessToken") != null);

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        if (path.matches("/oauth2callback.*") || path.matches("/login")) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        if (path.startsWith("/init/data") ||
                path.startsWith("/init/initialize") ||
                path.startsWith("/init/bank") ||
                path.startsWith("/init/load-payment") ||
                path.startsWith("/init/payment") ||
                path.startsWith("/init/accounts") ||
                path.startsWith("/init/addaccounts")) {

            if (!isAuthenticated) {
                handleUnauthorized(httpRequest, httpResponse);
                return;
            }

            String accessToken = (String) session.getAttribute("accessToken");

            if (isExpired(accessToken)) {
                String refreshToken = (String) session.getAttribute("refreshToken");

                if (refreshToken == null) {
                    handleUnauthorized(httpRequest, httpResponse);
                    return;
                }

                TokenResponse refreshed = getTokenResponse(refreshToken);
                accessToken = refreshed.getAccessToken();
                session.setAttribute("accessToken", accessToken);
            }

            String decodedScopes = getScopeFromJwt(accessToken);

            if (!hasRequiredScope(decodedScopes, "basic")) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        filterChain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {
    }

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestedWith = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");

        if ("XMLHttpRequest".equals(requestedWith) ||
                (acceptHeader != null && acceptHeader.contains("application/json"))) {
            response.setHeader("X-Login-Url", buildAsgardeoAuthUrl());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
        }
    }

    public static String buildAsgardeoAuthUrl() {
        return ConfigLoader.getProperty("asgardeo.secrets.authEndpoint") +
                "?response_type=code" +
                "&client_id=" + ConfigLoader.getProperty("asgardeo.secrets.clientId") +
                "&scope=openid basic" +
                "&redirect_uri=" + ConfigLoader.getProperty("asgardeo.secrets.redirectUri");
    }

    public boolean isExpired(String accessToken) {
        try {
            JsonNode payload = decodeJwtPayload(accessToken);
            if (payload != null && payload.has("exp")) {
                long expirationTime = Long.parseLong(payload.get("exp").asText());
                long now = System.currentTimeMillis() / 1000;
                return now > expirationTime;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    private String getScopeFromJwt(String accessToken) {
        try {
            JsonNode payload = decodeJwtPayload(accessToken);
            return (payload != null && payload.has("scope"))
                    ? payload.get("scope").asText()
                    : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode decodeJwtPayload(String accessToken) throws JsonProcessingException {
        String[] parts = accessToken.split("\\.");
        if (parts.length != 3) return null;

        byte[] bytes = Base64.getUrlDecoder().decode(parts[1]);
        return objectMapper.readTree(new String(bytes, StandardCharsets.UTF_8));
    }

    private TokenResponse getTokenResponse(String refreshToken) throws IOException {
        String clientId = ConfigLoader.getProperty("asgardeo.secrets.clientId");
        String clientSecret = ConfigLoader.getProperty("asgardeo.secrets.clientSecret");
        String encodedAuth = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)
        );

        HttpPost httpPost = new HttpPost(ConfigLoader.getProperty("asgardeo.secrets.tokenEndpoint"));
        httpPost.setHeader("Authorization", "Basic " + encodedAuth);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String json = EntityUtils.toString(response.getEntity());
            return objectMapper.readValue(json, TokenResponse.class);
        }
    }

    private boolean hasRequiredScope(String userScope, String targetScope) {
        if (userScope == null || userScope.trim().isEmpty()) {
            return false;
        }
        for (String scope : userScope.trim().split("\\s+")) {
            if (scope.equalsIgnoreCase(targetScope)) {
                return true;
            }
        }
        return false;
    }
}
