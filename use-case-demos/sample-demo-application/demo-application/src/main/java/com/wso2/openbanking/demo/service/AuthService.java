/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** AuthService implementation. */
public final class AuthService {

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final HttpTlsClient client;
    private String requestStatus = "accounts";
    private List<Account> lastFetchedAccounts = new ArrayList<>();
    private boolean lastPaymentSuccess = false;

    private AuthService(AccountService accountService,
                        PaymentService paymentService,
                        HttpTlsClient client) {
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.client = client;
    }

    public static AuthService create(AccountService accountService,
                                     PaymentService paymentService,HttpTlsClient client)
            throws SSLContextCreationException {
        return new AuthService(accountService, paymentService, client);
    }

    public void setRequestStatus(String status) {
        this.requestStatus = status;
    }

    public void processAuthorizationCallback(String code) throws AuthorizationException, IOException {
        String accessToken = exchangeCodeForToken(code);
        handleAuthorizationSuccess(accessToken);
    }

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

    private void handleAuthorizationSuccess(String accessToken) throws AuthorizationException, IOException {
        accountService.setAccessToken(accessToken);
        try {
            if ("accounts".equals(requestStatus)) {
                lastFetchedAccounts = accountService.createBankInContext();
            } else if ("payments".equals(requestStatus)) {
                lastPaymentSuccess = paymentService.processPaymentAuthorization(accessToken);
            }
        } catch (PaymentException e) {
            throw new AuthorizationException("Failed to add payment after successful authorization", e);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public String getRequestStatus() {
        return this.requestStatus;
    }


    public List<Account> getLastFetchedAccounts() {
        return lastFetchedAccounts;
    }

    public boolean isLastPaymentSuccess() {
        return lastPaymentSuccess;
    }
}
