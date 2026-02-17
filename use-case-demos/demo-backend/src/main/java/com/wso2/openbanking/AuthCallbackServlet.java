package com.wso2.openbanking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AuthCallbackServlet extends HttpServlet {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CLIENT_ID = ConfigLoader.getProperty("asgardeo.secrets.clientId");
    private static final String CLIENT_SECRET = ConfigLoader.getProperty("asgardeo.secrets.clientSecret");
    private static final String TOKEN_ENDPOINT = ConfigLoader.getProperty("asgardeo.secrets.tokenEndpoint");
    private static final String REDIRECT_URI = ConfigLoader.getProperty("asgardeo.secrets.redirectUri");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String error = req.getParameter("error");
        String errorDescription = req.getParameter("error_description");

        if (error != null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "OAuth authentication failed: " + error + " - " + errorDescription);
            return;
        }

        if (code == null || code.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing authorization code.");
            return;
        }

        try {
            String jsonResponse = performTokenExchange(code);
            TokenResponse tokens = objectMapper.readValue(jsonResponse, TokenResponse.class);

            HttpSession existingSession = req.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("accessToken", tokens.getAccessToken());
            session.setAttribute("refreshToken", tokens.getRefreshToken());
            session.removeAttribute("authRedirectInProgress");

            setSessionCookieForCrossOrigin(resp, session.getId(), req.getContextPath());

            resp.sendRedirect("http://localhost:5173/callback");

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to complete authentication: " + e.getMessage());
        }
    }

    private void setSessionCookieForCrossOrigin(HttpServletResponse response, String sessionId, String contextPath) {
        String cookieHeader = String.format(
                "JSESSIONID=%s; Path=%s/; Domain=tpp.local.ob; HttpOnly; Secure; SameSite=None; Max-Age=3600",
                sessionId,
                contextPath
        );
        response.setHeader("Set-Cookie", cookieHeader);
    }

    private String performTokenExchange(String code) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(TOKEN_ENDPOINT);

        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));

            httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            String authString = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
            httpPost.setHeader("Authorization", "Basic " + encodedAuth);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    return responseBody;
                } else {
                    throw new IOException(String.format(
                            "Token exchange failed with status %d. Response: %s",
                            statusCode, responseBody
                    ));
                }
            }
        } finally {
            try {
                httpClient.close();
            } catch (IOException ignored) {
            }
        }
    }
}
