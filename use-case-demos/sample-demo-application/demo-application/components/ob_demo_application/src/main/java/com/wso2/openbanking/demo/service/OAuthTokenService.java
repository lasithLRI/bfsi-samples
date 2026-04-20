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
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import com.wso2.openbanking.demo.utils.JwtUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** Handles OAuth token requests, consent initialization, and consent authorization. */
public final class OAuthTokenService {

    private final HttpTlsClient client;
    private final JwtTokenService jwtTokenService;

    /**
     * Creates an OAuthTokenService using the given TLS client.
     *
     * @param client TLS HTTP client for making API calls
     * @throws GeneralSecurityException   if JWT service initialization fails
     * @throws IOException                if the signing key cannot be read
     * @throws SSLContextCreationException if the TLS client copy fails
     */
    public OAuthTokenService(HttpTlsClient client)
            throws GeneralSecurityException, IOException, SSLContextCreationException {
        this.client = client.deepCopy();
        this.jwtTokenService = JwtTokenService.getInstance();
    }

    /**
     * Requests a client credentials access token for the given scope.
     *
     * @param scope OAuth scope to request the token for
     * @return raw token response JSON string
     * @throws AuthorizationException if the token request or signing fails
     */
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

    /**
     * Initializes an account consent using the given token and request body.
     *
     * @param token       raw token response JSON containing the access token
     * @param consentBody JSON request body for the consent
     * @param url         account consent endpoint URL
     * @return consent response JSON string
     * @throws IOException    if the API call fails
     * @throws JSONException  if the token response cannot be parsed
     */
    public String initializeConsent(String token, String consentBody, String url)
            throws IOException, JSONException {
        String accessToken = new JSONObject(token).getString("access_token");
        return client.postConsentInit(url, consentBody, accessToken);
    }

    /**
     * Initializes a payment consent using the given token and request body.
     *
     * @param token       raw token response JSON containing the access token
     * @param consentBody JSON request body for the payment consent
     * @param url         payment consent endpoint URL
     * @return payment consent response JSON string
     * @throws IOException   if the API call fails
     * @throws JSONException if the token response cannot be parsed
     */
    public String initializePaymentConsent(String token, String consentBody, String url)
            throws IOException, JSONException {
        String accessToken = new JSONObject(token).getString("access_token");
        return client.postPaymentConsentInit(url, consentBody, accessToken);
    }

    /**
     * Builds and sends a consent authorization request, returning the redirect URL.
     *
     * @param consentResponse consent response JSON from the consent initialization step
     * @param scope           OAuth scope for the authorization request
     * @return authorization redirect URL string
     * @throws GeneralSecurityException if request object signing fails
     * @throws IOException              if the API call fails
     */
    public String authorizeConsent(String consentResponse, String scope)
            throws GeneralSecurityException, IOException {
        String consentId = extractConsentId(consentResponse);
        String requestObject = jwtTokenService.createRequestObject(consentId);
        return client.postConsentAuthRequest(requestObject, ConfigLoader.getClientId(), scope);
    }

    /**
     * Extracts the consent ID from a consent response JSON string.
     *
     * @param consentResponse consent response JSON string
     * @return consent ID string
     */
    private String extractConsentId(String consentResponse) {
        return new JSONObject(consentResponse)
                .getJSONObject("Data")
                .getString("ConsentId");
    }

    /**
     * Builds the URL-encoded body for a client credentials token request.
     *
     * @param scope           OAuth scope to include in the request
     * @param clientAssertion signed JWT used as the client credential
     * @return URL-encoded token request body string
     */
    private String buildTokenRequestBody(String scope, String clientAssertion) {
        return "grant_type=client_credentials" +
                "&scope=" + scope +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }
}
