package com.wso2.openbanking.services;

import org.json.JSONArray;
import org.json.JSONObject;

public class RequestObjectPayload {

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

    public RequestObjectPayload(String iss, String responseType, String redirectUri,
                                String state, String nonce, String aud,
                                long nbf, long exp, String scope, String consentId) {
        this.iss = iss;
        this.responseType = responseType;
        this.redirectUri = redirectUri;
        this.state = state;
        this.nonce = nonce;
        this.clientId = iss;
        this.aud = aud;
        this.nbf = nbf;
        this.exp = exp;
        this.scope = scope;
        this.consentId = consentId;
    }

    public String toJson() {
        JSONObject intentId = new JSONObject()
                .put("value", consentId)
                .put("essential", true);

        JSONObject acr = new JSONObject()
                .put("values", new JSONArray()
                        .put("urn:openbanking:psd2:sca")
                        .put("urn:openbanking:psd2:ca"))
                .put("essential", true);

        JSONObject idToken = new JSONObject()
                .put("acr", acr)
                .put("openbanking_intent_id", intentId)
                .put("auth_time", new JSONObject().put("essential", true));

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
