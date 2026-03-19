package com.wso2.openbanking.demo.services;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.exceptions.PaymentException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import com.wso2.openbanking.demo.utils.JwtUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Handles the OAuth authorization callback and routes the resulting access token
 * to the appropriate service depending on whether the flow was for accounts or payments.
 */
public class AuthService {

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final HttpTlsClient client;
    private String requestStatus = "accounts";

    public AuthService(AccountService accountService, PaymentService paymentService)
            throws SSLContextCreationException {
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.client = new HttpTlsClient(
                ConfigLoader.getCertificatePath(),
                ConfigLoader.getKeyPath(),
                ConfigLoader.getTruststorePath(),
                ConfigLoader.getTruststorePassword()
        );
    }

    /** Sets whether the current flow is for "accounts" or "payments". */
    public void setRequestStatus(String status) {
        this.requestStatus = status;
    }

    /** Exchanges the authorization code for an access token and triggers post-auth processing. */
    public void processAuthorizationCallback(String code) throws AuthorizationException {
        String accessToken = exchangeCodeForToken(code);
        handleAuthorizationSuccess(accessToken);
    }

    /** Builds and sends the token exchange request, returning the raw access token string. */
    private String exchangeCodeForToken(String code) throws AuthorizationException {
        try {
            String clientAssertion = JwtTokenService.getInstance().createClientAssertion(JwtUtils.generateJti());
            String body = buildTokenRequestBody(code, clientAssertion);
            String response = client.postAccessToken(ConfigLoader.getTokenUrl(), body);
            return parseAccessToken(response);
        } catch (IOException e) {
            throw new AuthorizationException("Failed to contact token endpoint", e);
        } catch (JSONException e) {
            throw new AuthorizationException("Token response did not contain a valid access_token", e);
        } catch (Exception e) {
            throw new AuthorizationException("Failed to create client assertion due to a security error", e);
        }
    }

    /** Builds the URL-encoded token request body for the authorization_code grant. */
    private String buildTokenRequestBody(String code, String clientAssertion) {
        return "grant_type=authorization_code" +
                "&code=" + code +
                "&scope=accounts openid" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }

    /** Extracts the access_token field from the token endpoint JSON response. */
    private String parseAccessToken(String response) {
        return new JSONObject(response).getString("access_token");
    }

    /**
     * Passes the access token to the relevant service and triggers data persistence.
     * Routes to account fetching or payment processing based on requestStatus.
     */
    private void handleAuthorizationSuccess(String accessToken) throws AuthorizationException {
        accountService.setAccessToken(accessToken);
        try {
            if ("accounts".equals(requestStatus)) {
                accountService.addMockBankAccountsInformation();
            } else if ("payments".equals(requestStatus)) {
                paymentService.addPaymentToAccount(accessToken);
            }
        } catch (IOException e) {
            throw new AuthorizationException(
                    "Failed to persist data after successful authorization for scope: " + requestStatus, e);
        } catch (BankInfoLoadException e) {
            throw new AuthorizationException(
                    "Failed to load bank data after successful authorization", e);
        } catch (PaymentException e) {
            throw new AuthorizationException(
                    "Failed to add payment after successful authorization", e);
        }
    }
}
