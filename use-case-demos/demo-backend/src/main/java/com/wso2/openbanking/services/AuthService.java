package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.UUID;

public class AuthService {

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final HttpTlsClient client;
    private String requestStatus = "accounts";

    public AuthService(AccountService accountService, PaymentService paymentService) throws Exception {
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.client = new HttpTlsClient(
                ConfigLoader.getCertificatePath(),
                ConfigLoader.getKeyPath(),
                ConfigLoader.getTruststorePath(),
                ConfigLoader.getTruststorePassword()
        );
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
        JwtTokenService jwtService = JwtTokenService.getInstance();

        String clientAssertion = jwtService.createClientAssertion(jti);
        String body = buildTokenRequestBody(code, clientAssertion);
        String response = client.postAccesstoken(ConfigLoader.getTokenUrl(), body);

        return parseAccessToken(response);
    }

    private String buildTokenRequestBody(String code, String clientAssertion) throws Exception {
        return "grant_type=authorization_code" +
                "&code=" + code +
                "&scope=accounts openid" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
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
