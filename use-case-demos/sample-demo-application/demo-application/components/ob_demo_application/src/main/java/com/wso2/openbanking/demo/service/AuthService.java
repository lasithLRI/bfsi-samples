/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.demo.service;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.PaymentException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.models.Account;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import com.wso2.openbanking.demo.utils.JwtUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Handles OAuth authorization callbacks for account and payment flows. */
public final class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final HttpTlsClient client;
    private String requestStatus = "accounts";
    private List<Account> lastFetchedAccounts = new ArrayList<>();
    private boolean lastPaymentSuccess = false;

    /**
     * Creates an AuthService with the given account, payment, and HTTP client dependencies.
     *
     * @param accountService service for fetching account data
     * @param paymentService service for processing payments
     * @param client         TLS HTTP client for making API calls
     * @throws SSLContextCreationException if the TLS client copy fails
     */
    private AuthService(AccountService accountService,
                        PaymentService paymentService,
                        HttpTlsClient client) throws SSLContextCreationException {
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.client = client.deepCopy();
        LOG.debug("AuthService instance created successfully.");
    }

    /**
     * Creates a new AuthService instance with the given dependencies.
     *
     * @param accountService service for fetching account data
     * @param paymentService service for processing payments
     * @param client         TLS HTTP client for making API calls
     * @return new AuthService instance
     * @throws SSLContextCreationException if TLS client setup fails
     */
    public static AuthService create(AccountService accountService,
                                     PaymentService paymentService, HttpTlsClient client)
            throws SSLContextCreationException {
        LOG.debug("Creating new AuthService instance.");
        return new AuthService(accountService, paymentService, client);
    }

    /**
     * Sets the current request status to track the active flow type.
     *
     * @param status flow type identifier (e.g. "accounts" or "payments")
     */
    public void setRequestStatus(String status) {
        LOG.debug("Request status updated: {}", status);
        this.requestStatus = status;
    }

    /**
     * Processes the OAuth callback code and triggers account or payment handling.
     *
     * @param code authorization code received from the OAuth callback
     * @throws AuthorizationException if token exchange or handling fails
     * @throws IOException            if an API call fails during handling
     */
    public void processAuthorizationCallback(String code) throws AuthorizationException, IOException {
        LOG.debug("Processing authorization callback. Request status: {}", requestStatus);
        String accessToken = exchangeCodeForToken(code);
        LOG.debug("Access token obtained successfully. Proceeding to handle authorization.");
        handleAuthorizationSuccess(accessToken);
    }

    /**
     * Exchanges an authorization code for an OAuth access token.
     *
     * @param code authorization code from the OAuth callback
     * @return access token string
     * @throws AuthorizationException if the token request or parsing fails
     */
    private String exchangeCodeForToken(String code) throws AuthorizationException {
        LOG.debug("Exchanging authorization code for access token.");
        try {
            String clientAssertion = JwtTokenService.getInstance().createClientAssertion(JwtUtils.generateJti());
            LOG.debug("Client assertion created successfully.");
            String body = buildTokenRequestBody(code, clientAssertion);
            String response = client.postAccessToken(ConfigLoader.getTokenUrl(), body);
            LOG.debug("Received response from token endpoint.");
            return parseAccessToken(response);
        } catch (IOException e) {
            LOG.error("Failed to contact token endpoint: {}", e.getMessage(), e);
            throw new AuthorizationException("Failed to contact token endpoint", e);
        } catch (JSONException e) {
            LOG.error("Token response did not contain a valid access_token: {}", e.getMessage(), e);
            throw new AuthorizationException("Token response did not contain a valid access_token", e);
        } catch (Exception e) {
            LOG.error("Failed to create client assertion due to a security error: {}", e.getMessage(), e);
            throw new AuthorizationException("Failed to create client assertion due to a security error", e);
        }
    }

    /**
     * Builds the URL-encoded token request body for the authorization code grant.
     *
     * @param code            authorization code from the OAuth callback
     * @param clientAssertion signed JWT used as the client credential
     * @return URL-encoded token request body string
     */
    private String buildTokenRequestBody(String code, String clientAssertion) {
        LOG.debug("Building token request body for client ID: {}", ConfigLoader.getClientId());
        return "grant_type=authorization_code" +
                "&code=" + code +
                "&scope=accounts openid" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }

    /**
     * Parses the access token from the token endpoint JSON response.
     *
     * @param response raw JSON response string from the token endpoint
     * @return access token string
     */
    private String parseAccessToken(String response) {
        LOG.debug("Parsing access token from token endpoint response.");
        return new JSONObject(response).getString("access_token");
    }

    /**
     * Handles post-authorization logic by fetching accounts or processing a payment.
     *
     * @param accessToken valid OAuth access token from the token exchange
     * @throws AuthorizationException if payment processing fails
     * @throws IOException            if an API call fails
     */
    private void handleAuthorizationSuccess(String accessToken) throws AuthorizationException, IOException {
        LOG.debug("Handling authorization success. Request status: {}", requestStatus);
        accountService.setAccessToken(accessToken);
        try {
            if ("accounts".equals(requestStatus)) {
                LOG.debug("Fetching accounts from bank context.");
                lastFetchedAccounts = accountService.createBankInContext();
                LOG.debug("Accounts fetched successfully. Count: {}", lastFetchedAccounts.size());
            } else if ("payments".equals(requestStatus)) {
                LOG.debug("Processing payment authorization.");
                lastPaymentSuccess = paymentService.processPaymentAuthorization(accessToken);
                LOG.debug("Payment authorization completed. Success: {}", lastPaymentSuccess);
            } else {
                LOG.warn("Unrecognized request status during authorization handling: {}", requestStatus);
            }
        } catch (PaymentException e) {
            LOG.error("Failed to process payment after successful authorization: {}", e.getMessage(), e);
            throw new AuthorizationException("Failed to add payment after successful authorization", e);
        } catch (IOException e) {
            LOG.error("IO error during authorization handling: {}", e.getMessage(), e);
            throw new IOException(e);
        }
    }

    /**
     * Returns the current request status.
     *
     * @return current flow type identifier
     */
    public String getRequestStatus() {
        return this.requestStatus;
    }

    /**
     * Returns a copy of the last fetched accounts list.
     *
     * @return list of accounts fetched in the last authorization flow
     */
    public List<Account> getLastFetchedAccounts() {
        return new ArrayList<>(lastFetchedAccounts);
    }

    /**
     * Returns whether the last payment was processed successfully.
     *
     * @return true if the last payment succeeded, false otherwise
     */
    public boolean isLastPaymentSuccess() {
        return lastPaymentSuccess;
    }
}
