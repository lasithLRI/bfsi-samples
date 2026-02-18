package com.wso2.openbanking.services;

import org.json.JSONArray;
import org.json.JSONObject;

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

    private RequestObjectPayload(Builder builder) {
        this.iss = builder.iss;
        this.responseType = builder.responseType;
        this.redirectUri = builder.redirectUri;
        this.state = builder.state;
        this.nonce = builder.nonce;
        this.clientId = builder.iss; // clientId is always derived from iss
        this.aud = builder.aud;
        this.nbf = builder.nbf;
        this.exp = builder.exp;
        this.scope = builder.scope;
        this.consentId = builder.consentId;
    }

    public static class Builder {

        private String iss;
        private String responseType;
        private String redirectUri;
        private String state;
        private String nonce;
        private String aud;
        private long nbf;
        private long exp;
        private String scope;
        private String consentId;

        public Builder iss(String iss) {
            this.iss = iss;
            return this;
        }

        public Builder responseType(String responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder aud(String aud) {
            this.aud = aud;
            return this;
        }

        public Builder nbf(long nbf) {
            this.nbf = nbf;
            return this;
        }

        public Builder exp(long exp) {
            this.exp = exp;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder consentId(String consentId) {
            this.consentId = consentId;
            return this;
        }

        public RequestObjectPayload build() {
            return new RequestObjectPayload(this);
        }
    }

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
