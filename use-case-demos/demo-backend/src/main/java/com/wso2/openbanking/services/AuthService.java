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
        String accessToken = exchangeCodeForToken(code);
        handleAuthorizationSuccess(accessToken);
    }

    private String exchangeCodeForToken(String code) throws Exception {
        String jti = generateJti();
        String clientAssertion = JwtTokenService.getInstance().createClientAssertion(jti);
        String body = buildTokenRequestBody(code, clientAssertion);
        String response = client.postAccesstoken(ConfigLoader.getTokenUrl(), body);
        return parseAccessToken(response);
    }

    private String buildTokenRequestBody(String code, String clientAssertion) {
        return "grant_type=authorization_code" +
                "&code=" + code +
                "&scope=accounts openid" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }

    private String parseAccessToken(String response) {
        return new JSONObject(response).getString("access_token");
    }

    private void handleAuthorizationSuccess(String accessToken) throws Exception {
        accountService.setAccessToken(accessToken);

        if ("accounts".equals(requestStatus)) {
            accountService.addMockBankAccountsInformation();
        } else if ("payments".equals(requestStatus)) {
            paymentService.addPaymentToAccount();
        }
    }

    private String generateJti() {
        return new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
    }
}
