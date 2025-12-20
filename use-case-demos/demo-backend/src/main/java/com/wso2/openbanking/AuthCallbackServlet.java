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
    private static final String CLIENT_ID = ConfigLoader.getProperty("asgardeo.secrets.clientId");
    private static final String CLIENT_SECRET = ConfigLoader.getProperty("asgardeo.secrets.clientSecret");
    private static final String TOKEN_ENDPOINT = ConfigLoader.getProperty("asgardeo.secrets.tokenEndpoint");
    private static final String REDIRECT_URI = ConfigLoader.getProperty("asgardeo.secrets.redirectUri");


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        String returnTo = req.getParameter("returnTo");
        System.out.println("============================");

        String jsonResponse = performTokenExchange(code);

        System.out.println("jsonResponse = " + jsonResponse);

        ObjectMapper objectMapper = new ObjectMapper();

        TokenResponse tokens = objectMapper.readValue(jsonResponse, TokenResponse.class);

        HttpSession session = req.getSession(true);

        session.setAttribute("accessToken", tokens.getAccessToken());
        session.setAttribute("refreshToken", tokens.getRefreshToken());
        System.out.println(tokens.getAccessToken());

        String frontendRedirect = (returnTo != null) ? returnTo : "/accounts-central/home";
        resp.sendRedirect("http://localhost:5173" + frontendRedirect);

        if (code == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing code or state.");
            return;
        }

        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");




    }

    public String performTokenExchange(String code) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(TOKEN_ENDPOINT);

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
                System.out.println(responseBody);
                return responseBody;
            } else {
                String errorMessage = String.format("Token exchange failed with status code %d. Response: %s", statusCode, responseBody);
                throw new IOException(errorMessage);
            }
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
