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

import org.json.JSONArray;
import org.json.JSONObject;

/** Represents the JWT request object payload for OAuth authorization requests. */
public class RequestObjectPayload {

    private static final String FIELD_ESSENTIAL = "essential";

    private final String iss;
    private final String responseType;
    private final String redirectUri;
    private final String state;
    private final String nonce;
    private final String clientId;
    private final String aud;
    private final long nbf;
    private final long exp;
    private final String scope;
    private final String consentId;

    /**
     * Creates a RequestObjectPayload from the given builder.
     *
     * @param builder builder instance with all payload fields set
     */
    private RequestObjectPayload(Builder builder) {
        this.iss = builder.iss;
        this.responseType = builder.responseType;
        this.redirectUri = builder.redirectUri;
        this.state = builder.state;
        this.nonce = builder.nonce;
        this.clientId = builder.iss;
        this.aud = builder.aud;
        this.nbf = builder.nbf;
        this.exp = builder.exp;
        this.scope = builder.scope;
        this.consentId = builder.consentId;
    }

    /** Builder for constructing a RequestObjectPayload with individual field setters. */
    public static class Builder {

        String iss;
        private String responseType;
        private String redirectUri;
        private String state;
        private String nonce;
        private String aud;
        private long nbf;
        private long exp;
        private String scope;
        private String consentId;

        /**
         * Sets the issuer claim.
         *
         * @param iss OAuth client ID used as the issuer
         * @return this builder
         */
        public Builder iss(String iss) {
            this.iss = iss;
            return this;
        }

        /**
         * Sets the response type claim.
         *
         * @param responseType expected OAuth response type
         * @return this builder
         */
        public Builder responseType(String responseType) {
            this.responseType = responseType;
            return this;
        }

        /**
         * Sets the redirect URI claim.
         *
         * @param redirectUri OAuth callback redirect URI
         * @return this builder
         */
        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        /**
         * Sets the state claim.
         *
         * @param state OAuth state parameter for CSRF protection
         * @return this builder
         */
        public Builder state(String state) {
            this.state = state;
            return this;
        }

        /**
         * Sets the nonce claim.
         *
         * @param nonce unique value to prevent replay attacks
         * @return this builder
         */
        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        /**
         * Sets the audience claim.
         *
         * @param aud intended audience, typically the token endpoint URL
         * @return this builder
         */
        public Builder aud(String aud) {
            this.aud = aud;
            return this;
        }

        /**
         * Sets the not-before time claim.
         *
         * @param nbf Unix timestamp before which the token is not valid
         * @return this builder
         */
        public Builder nbf(long nbf) {
            this.nbf = nbf;
            return this;
        }

        /**
         * Sets the expiration time claim.
         *
         * @param exp Unix timestamp at which the token expires
         * @return this builder
         */
        public Builder exp(long exp) {
            this.exp = exp;
            return this;
        }

        /**
         * Sets the scope claim.
         *
         * @param scope space-separated OAuth scopes to request
         * @return this builder
         */
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Sets the consent ID claim.
         *
         * @param consentId Open Banking consent ID to include in the request
         * @return this builder
         */
        public Builder consentId(String consentId) {
            this.consentId = consentId;
            return this;
        }

        /**
         * Builds and returns the RequestObjectPayload.
         *
         * @return new RequestObjectPayload with the configured fields
         */
        public RequestObjectPayload build() {
            return new RequestObjectPayload(this);
        }
    }

    /**
     * Serializes the payload to a JSON string including all OAuth and claims fields.
     *
     * @return JSON string representation of the request object payload
     */
    public String toJson() {
        JSONObject intentId = new JSONObject()
                .put("value", consentId)
                .put(FIELD_ESSENTIAL, true);

        JSONObject acr = new JSONObject()
                .put("values", new JSONArray()
                        .put("urn:openbanking:psd2:sca")
                        .put("urn:openbanking:psd2:ca"))
                .put(FIELD_ESSENTIAL, true);

        JSONObject idToken = new JSONObject()
                .put("acr", acr)
                .put("openbanking_intent_id", intentId)
                .put("auth_time", new JSONObject().put(FIELD_ESSENTIAL, true));

        JSONObject userInfo = new JSONObject()
                .put("openbanking_intent_id", intentId);

        return new JSONObject()
                .put("iss", iss)
                .put("response_type", responseType)
                .put("redirect_uri", redirectUri)
                .put("state", state)
                .put("nonce", nonce)
                .put("client_id", clientId)
                .put("aud", aud)
                .put("nbf", nbf)
                .put("exp", exp)
                .put("scope", scope)
                .put("claims", new JSONObject()
                        .put("id_token", idToken)
                        .put("userinfo", userInfo))
                .toString();
    }
}
