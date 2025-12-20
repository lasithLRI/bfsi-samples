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
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        System.out.println("SessionSecurityFilter.doFilter");

        HttpSession session = httpRequest.getSession(true);
        boolean isAuthenticated = (session != null && session.getAttribute("accessToken") != null);

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        if (path.matches("/oauth2callback.*")||path.matches("/login")) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        if (path.startsWith("/init/data")){

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

                System.out.println("token refreshed: " + accessToken);

                session.setAttribute("accessToken", accessToken);
            }

            String decodedScopes = getScopeFromJwt(accessToken);

            if (!hasRequiredScope(decodedScopes,"basic")) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

        }
        filterChain.doFilter(httpRequest, httpResponse);
        return;

    }

    @Override
    public void destroy() {

    }

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String requestedWith = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");

        if ("XMLHttpRequest".equals(requestedWith) || (acceptHeader != null && acceptHeader.contains("application/json"))) {
            response.setHeader("X-Login-Url", buildAsgardeoAuthUrl());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
        }
    }

    public static String buildAsgardeoAuthUrl() {
        return ConfigLoader.getProperty("asgardeo.secrets.authEndpoint") +
                "?response_type=code" +
                "&client_id=" + ConfigLoader.getProperty("asgardeo.secrets.clientId")+
                "&scope=openid basic" +
                "&redirect_uri="+ ConfigLoader.getProperty("asgardeo.secrets.redirectUri");
    }

    public boolean isExpired(String accessToken) {
        String[] parts = accessToken.split("\\.");
        if (parts.length != 3) return true;

        byte[] bytes = Base64.getDecoder().decode(parts[1]);
        String payload = new String(bytes);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(payload);

            if (node.has("exp")) {

                long expirationTime = Long.parseLong(node.get("exp").asText());
                long now = System.currentTimeMillis() / 1000;

                return now > expirationTime;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;

    }

    private TokenResponse getTokenResponse(String token) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(ConfigLoader.getProperty("asgardeo.secrets.tokenEndpoint"));

        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("refresh_token", token));

        String clientId = ConfigLoader.getProperty("asgardeo.secrets.clientId");
        String clientSecret = ConfigLoader.getProperty("asgardeo.secrets.clientSecret");
        String auth = clientId+":"+clientSecret;

        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        httpPost.setHeader("Authorization", "Basic " + encodedAuth);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        try(CloseableHttpResponse response = httpClient.execute(httpPost)){
            String json = EntityUtils.toString(response.getEntity());

            return new ObjectMapper().readValue(json, TokenResponse.class);
        }
    }

    private String getScopeFromJwt(String accessToken) {
        String[] parts = accessToken.split("\\.");
        if (parts.length != 3) return null;

        byte[] bytes = Base64.getDecoder().decode(parts[1]);
        String payload = new String(bytes);

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode node = mapper.readTree(payload);
            return node.has("scope") ? node.get("scope").asText() : null;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasRequiredScope(String userScope, String targetScope) {
        if (userScope == null || userScope.trim().isEmpty()){
            return false;
        }

        String[] scopes = userScope.trim().split("\\s+");
        for (String scope : scopes) {
            if (scope.equalsIgnoreCase(targetScope)) {
                return true;
            }
        }
        return false;
    }
}
