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

/** AuthService implementation. */
public final class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final HttpTlsClient client;
    private String requestStatus = "accounts";
    private List<Account> lastFetchedAccounts = new ArrayList<>();
    private boolean lastPaymentSuccess = false;

    private AuthService(AccountService accountService,
                        PaymentService paymentService,
                        HttpTlsClient client) throws SSLContextCreationException {
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.client = client.deepCopy();
        LOG.debug("AuthService instance created successfully.");
    }

    public static AuthService create(AccountService accountService,
                                     PaymentService paymentService, HttpTlsClient client)
            throws SSLContextCreationException {
        LOG.debug("Creating new AuthService instance.");
        return new AuthService(accountService, paymentService, client);
    }

    public void setRequestStatus(String status) {
        LOG.debug("Request status updated: {}", status);
        this.requestStatus = status;
    }

    public void processAuthorizationCallback(String code) throws AuthorizationException, IOException {
        LOG.debug("Processing authorization callback. Request status: {}", requestStatus);
        String accessToken = exchangeCodeForToken(code);
        LOG.debug("Access token obtained successfully. Proceeding to handle authorization.");
        handleAuthorizationSuccess(accessToken);
    }

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

    private String parseAccessToken(String response) {
        LOG.debug("Parsing access token from token endpoint response.");
        return new JSONObject(response).getString("access_token");
    }

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

    public String getRequestStatus() {
        return this.requestStatus;
    }

    public List<Account> getLastFetchedAccounts() {
        return new ArrayList<>(lastFetchedAccounts);
    }

    public boolean isLastPaymentSuccess() {
        return lastPaymentSuccess;
    }
}
