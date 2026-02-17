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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * OAuth callback servlet that properly configures session cookies for cross-origin requests.
 * Sets SameSite=None and Secure=true to allow cookies from localhost:5173 to tpp.local.ob
 */
public class AuthCallbackServlet extends HttpServlet {
    private static final String CLIENT_ID = ConfigLoader.getProperty("asgardeo.secrets.clientId");
    private static final String CLIENT_SECRET = ConfigLoader.getProperty("asgardeo.secrets.clientSecret");
    private static final String TOKEN_ENDPOINT = ConfigLoader.getProperty("asgardeo.secrets.tokenEndpoint");
    private static final String REDIRECT_URI = ConfigLoader.getProperty("asgardeo.secrets.redirectUri");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        String error = req.getParameter("error");
        String errorDescription = req.getParameter("error_description");

        System.out.println("============================");
        System.out.println("OAuth Callback Received");
        System.out.println("Request URL: " + req.getRequestURL());
        System.out.println("Request URI: " + req.getRequestURI());

        // Check for OAuth errors
        if (error != null) {
            System.err.println("OAuth Error: " + error);
            System.err.println("Error Description: " + errorDescription);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "OAuth authentication failed: " + error + " - " + errorDescription);
            return;
        }

        // Check for authorization code
        if (code == null || code.trim().isEmpty()) {
            System.err.println("Missing authorization code");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing authorization code.");
            return;
        }

        try {
            // Exchange code for tokens
            String jsonResponse = performTokenExchange(code);
            System.out.println("Token Exchange Response: " + jsonResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            TokenResponse tokens = objectMapper.readValue(jsonResponse, TokenResponse.class);

            // Get existing session or create new one
            HttpSession existingSession = req.getSession(false);

            // Invalidate old session if it exists to ensure clean state
            if (existingSession != null) {
                System.out.println("Invalidating existing session: " + existingSession.getId());
                existingSession.invalidate();
            }

            // Create new session
            HttpSession session = req.getSession(true);

            // Store tokens in session
            session.setAttribute("accessToken", tokens.getAccessToken());
            session.setAttribute("refreshToken", tokens.getRefreshToken());

            // Clear any redirect flags from the session
            session.removeAttribute("authRedirectInProgress");

            System.out.println("Access token stored in session: " +
                    tokens.getAccessToken().substring(0, Math.min(20, tokens.getAccessToken().length())) + "...");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Max Inactive Interval: " + session.getMaxInactiveInterval() + " seconds");

            // CRITICAL: Set session cookie with SameSite=None for cross-origin requests
            setSessionCookieForCrossOrigin(resp, session.getId(), req.getContextPath());

            // Redirect to React app
            String redirectPath = "http://localhost:5173/callback";
            System.out.println("Redirecting to: " + redirectPath);

            resp.sendRedirect(redirectPath);

            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

        } catch (Exception e) {
            System.err.println("Error during token exchange: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to complete authentication: " + e.getMessage());
        }
    }

    /**
     * Sets the JSESSIONID cookie with proper attributes for cross-origin requests.
     * Required attributes:
     * - SameSite=None: Allows cross-origin cookie sending
     * - Secure=true: Required when SameSite=None
     * - HttpOnly=true: Prevents JavaScript access (security)
     */
    private void setSessionCookieForCrossOrigin(HttpServletResponse response, String sessionId, String contextPath) {
        // Build cookie header manually to ensure all attributes are set correctly
        String cookieHeader = String.format(
                "JSESSIONID=%s; Path=%s/; Domain=tpp.local.ob; HttpOnly; Secure; SameSite=None; Max-Age=3600",
                sessionId,
                contextPath
        );

        // Set the cookie using the Set-Cookie header
        response.setHeader("Set-Cookie", cookieHeader);

        System.out.println("✅ Session cookie configured for cross-origin:");
        System.out.println("   Cookie: " + cookieHeader);
        System.out.println("   This cookie will be sent from localhost:5173 to tpp.local.ob");
    }

    /**
     * Exchanges the authorization code for access and refresh tokens
     */
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
                    String errorMessage = String.format(
                            "Token exchange failed with status code %d. Response: %s",
                            statusCode,
                            responseBody
                    );
                    System.err.println(errorMessage);
                    throw new IOException(errorMessage);
                }
            }
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                System.err.println("Error closing HTTP client: " + e.getMessage());
            }
        }
    }
}
