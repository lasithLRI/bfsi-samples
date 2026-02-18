package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.exception.AuthorizationException;
import com.wso2.openbanking.utils.JwtUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class AuthService {

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final HttpTlsClient client;
    private String requestStatus = "accounts";

    public AuthService(AccountService accountService, PaymentService paymentService)
            throws Exception {
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

    public void processAuthorizationCallback(String code) throws AuthorizationException {
        String accessToken = exchangeCodeForToken(code);
        handleAuthorizationSuccess(accessToken);
    }

    private String exchangeCodeForToken(String code) throws AuthorizationException {
        try {
            String clientAssertion = JwtTokenService.getInstance().createClientAssertion(JwtUtils.generateJti());
            String body = buildTokenRequestBody(code, clientAssertion);
            String response = client.postAccesstoken(ConfigLoader.getTokenUrl(), body);
            return parseAccessToken(response);
        } catch (IOException e) {
            throw new AuthorizationException("Failed to contact token endpoint", e);
        } catch (JSONException e) {
            throw new AuthorizationException("Token response did not contain a valid access_token", e);
        } catch (Exception e) {
            throw new AuthorizationException("Failed to create client assertion due to a security error", e);
        }
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

    private void handleAuthorizationSuccess(String accessToken) throws AuthorizationException {
        accountService.setAccessToken(accessToken);
        try {
            if ("accounts".equals(requestStatus)) {
                accountService.addMockBankAccountsInformation();
            } else if ("payments".equals(requestStatus)) {
                paymentService.addPaymentToAccount();
            }
        } catch (IOException e) {
            throw new AuthorizationException(
                    "Failed to persist data after successful authorization for scope: " + requestStatus, e);
        }
    }
}
