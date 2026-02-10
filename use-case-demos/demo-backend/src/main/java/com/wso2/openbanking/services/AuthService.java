package com.wso2.openbanking.services;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.UUID;

public class AuthService {

    private static final String TOKEN_URL = "https://localhost:9446/oauth2/token";
    private static final String CLIENT_ID = "onKy05vpqDjTenzZSRjfSOfb3ZMa";
    private static final String CLIENT_SECRET = "sCekNgSWIauQ34klRhDGqfwpjc4";
    private static final String REDIRECT_URI = "https://tpp.local.ob/ob_demo_backend_war/init/redirected";

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final HttpTlsClient client;
    private String requestStatus = "accounts";

    public AuthService(AccountService accountService, PaymentService paymentService) throws Exception {
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.client = new HttpTlsClient("/obtransport.pem", "/obtransport.key",
                "/client-truststore.jks", "123456");
    }

    public void setRequestStatus(String status) {
        this.requestStatus = status;
    }

    public void processAuthorizationCallback(String code, String state, String sessionState, String idToken)
            throws Exception {

        System.out.println("Processing authorization callback - Code: " + code);

        String accessToken = exchangeCodeForToken(code);
        handleAuthorizationSuccess(accessToken);
    }

    private String exchangeCodeForToken(String code) throws Exception {
        String jti = generateJti();
        AppContext context = new AppContext(CLIENT_ID, CLIENT_SECRET, "PS256", "JWT", jti);

        String body = buildTokenRequestBody(code, context);
        String response = client.postAccesstoken(TOKEN_URL, body);

        return parseAccessToken(response);
    }

    private String buildTokenRequestBody(String code, AppContext context) throws Exception {
        return "grant_type=authorization_code" +
                "&code=" + code +
                "&scope=accounts openid" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + context.getClientId() +
                "&client_assertion=" + context.createClientAsserstion() +
                "&redirect_uri=" + REDIRECT_URI;
    }

    private String parseAccessToken(String response) {
        JSONObject json = new JSONObject(response);

        System.out.println("Token response: " + response);

        String accessToken = json.getString("access_token");
        String refreshToken = json.getString("refresh_token");
        String scope = json.getString("scope");
        String idToken = json.getString("id_token");
        int expiresIn = json.getInt("expires_in");

        logTokenDetails(accessToken, refreshToken, scope, idToken, expiresIn);

        return accessToken;
    }

    private void handleAuthorizationSuccess(String accessToken) throws Exception {
        System.out.println("Authorization successful. Access Token: " + accessToken);

        accountService.setAccessToken(accessToken);

        if ("accounts".equals(requestStatus)) {
            System.out.println("Processing account authorization");
            accountService.addMockBankAccountsInformation();
        } else if ("payments".equals(requestStatus)) {
            System.out.println("Processing payment authorization");
            paymentService.addPaymentToAccount();
        }
    }

    private void logTokenDetails(String accessToken, String refreshToken, String scope,
                                 String idToken, int expiresIn) {
        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);
        System.out.println("Scope: " + scope);
        System.out.println("ID Token: " + idToken);
        System.out.println("Expires In: " + expiresIn);
    }

    private String generateJti() {
        return new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
    }
}
