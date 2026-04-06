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
import com.wso2.openbanking.demo.utils.ConfigLoader;
import com.wso2.openbanking.demo.utils.JwtUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** OAuthTokenService implementation. */
public final class OAuthTokenService {

    private final HttpTlsClient client;
    private final JwtTokenService jwtTokenService;

    public OAuthTokenService(HttpTlsClient client) throws GeneralSecurityException, IOException {
        this.client = client;
        this.jwtTokenService = JwtTokenService.getInstance();
    }

    public String getToken(String scope) throws AuthorizationException {
        try {
            String clientAssertion = jwtTokenService.createClientAssertion(JwtUtils.generateJti());
            String body = buildTokenRequestBody(scope, clientAssertion);
            return client.postJwt(ConfigLoader.getTokenUrl(), body);
        } catch (IOException e) {
            throw new AuthorizationException("Failed to contact token endpoint", e);
        } catch (GeneralSecurityException e) {
            throw new AuthorizationException("Failed to sign client assertion", e);
        }
    }

    public String initializeConsent(String token, String consentBody, String url)
            throws IOException, JSONException {
        String accessToken = new JSONObject(token).getString("access_token");
        return client.postConsentInit(url, consentBody, accessToken);
    }

    public String initializePaymentConsent(String token, String consentBody, String url)
            throws IOException, JSONException {
        String accessToken = new JSONObject(token).getString("access_token");
        return client.postPaymentConsentInit(url, consentBody, accessToken);
    }

    public String authorizeConsent(String consentResponse, String scope)
            throws GeneralSecurityException, IOException {
        String consentId = extractConsentId(consentResponse);
        String requestObject = jwtTokenService.createRequestObject(consentId);
        return client.postConsentAuthRequest(requestObject, ConfigLoader.getClientId(), scope);
    }

    private String extractConsentId(String consentResponse) {
        return new JSONObject(consentResponse)
                .getJSONObject("Data")
                .getString("ConsentId");
    }

    private String buildTokenRequestBody(String scope, String clientAssertion) {
        return "grant_type=client_credentials" +
                "&scope=" + scope +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }
}
