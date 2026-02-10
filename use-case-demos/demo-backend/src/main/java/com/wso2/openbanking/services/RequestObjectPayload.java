package com.wso2.openbanking.services;

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
        this.clientId = iss; // Same as iss
        this.aud = aud;
        this.nbf = nbf;
        this.exp = exp;
        this.scope = scope;
        this.consentId = consentId;
    }

    public String toJson() {
        return "{\n" +
                "  \"iss\": \"" + iss + "\",\n" +
                "  \"response_type\": \"" + responseType + "\",\n" +
                "  \"redirect_uri\": \"" + redirectUri + "\",\n" +
                "  \"state\": \"" + state + "\",\n" +
                "  \"nonce\": \"" + nonce + "\",\n" +
                "  \"client_id\": \"" + clientId + "\",\n" +
                "  \"aud\": \"" + aud + "\",\n" +
                "  \"nbf\": " + nbf + ",\n" +
                "  \"exp\": " + exp + ",\n" +
                "  \"scope\": \"" + scope + "\",\n" +
                "  \"claims\": {\n" +
                "    \"id_token\": {\n" +
                "      \"acr\": {\n" +
                "        \"values\": [\n" +
                "          \"urn:openbanking:psd2:sca\",\n" +
                "          \"urn:openbanking:psd2:ca\"\n" +
                "        ],\n" +
                "        \"essential\": true\n" +
                "      },\n" +
                "      \"openbanking_intent_id\": {\n" +
                "        \"value\": \"" + consentId + "\",\n" +
                "        \"essential\": true\n" +
                "      },\n" +
                "      \"auth_time\": {\n" +
                "        \"essential\": true\n" +
                "      }\n" +
                "    },\n" +
                "    \"userinfo\": {\n" +
                "      \"openbanking_intent_id\": {\n" +
                "        \"value\": \"" + consentId + "\",\n" +
                "        \"essential\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
