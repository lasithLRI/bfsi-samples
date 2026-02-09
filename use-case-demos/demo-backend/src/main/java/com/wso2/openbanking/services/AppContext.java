package com.wso2.openbanking.services;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class AppContext {

    String clientId;
    String kid;
    String alg;
    String typ;
    String jti;
    long currentTime;

    public AppContext(String clientId, String kid, String alg, String typ, String jti) {
        this.clientId = clientId;
        this.kid = kid;
        this.alg = alg;
        this.typ = typ;
        this.jti = jti;
        this.currentTime = System.currentTimeMillis()/1000;
    }

    public PrivateKey getPrivateKey() throws Exception {
        return KeyReader.loadPrivateKeyFromStream(AppContext.class.getResourceAsStream("/obsigning.key"));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String createClientAsserstion() throws Exception {

        long issuedAtSec = this.currentTime;

        long expirationTimeSec = issuedAtSec + TimeUnit.MINUTES.toSeconds(5);
        String audience = "https://localhost:9446/oauth2/token";

        String headerJson = "{"
                + "\"alg\":\"" + this.alg + "\","
                + "\"kid\":\"" + this.kid + "\","
                + "\"typ\":\"" + this.typ + "\""
                + "}";
        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

        String payloadJson = "{"
                + "\"iss\":\"" + this.clientId + "\","
                + "\"sub\":\"" + this.clientId + "\","
                + "\"exp\":" + expirationTimeSec + ","
                + "\"iat\":" + issuedAtSec + ","
                + "\"jti\":\"" + this.jti + "\","
                + "\"aud\":\"" + audience + "\""
                + "}";
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

        String signingInput = encodedHeader + "." + encodedPayload;
        byte[] signingInputBytes = signingInput.getBytes(StandardCharsets.US_ASCII);

        PrivateKey privateKey = getPrivateKey();

        // The Java security provider name for RSASSA-PSS with SHA-256 is "SHA256withRSAandMGF1".
        Signature signature = Signature.getInstance("RSASSA-PSS");

        // Set PSS parameters for PS256 (RSASSA-PSS using SHA-256):
        // Salt length equal to the hash length (256 bits = 32 bytes)
        PSSParameterSpec pssSpec = new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1);
        signature.setParameter(pssSpec);

        signature.initSign(privateKey);
        signature.update(signingInputBytes);
        byte[] rawSignature = signature.sign();

        String encodedSignature = base64UrlEncode(rawSignature);


        return signingInput + "." + encodedSignature;
    }

    public String getClientId() {
        return clientId;
    }


    public String makeRequestObject(String consentId) throws Exception {


        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        long expirationTimeSec = currentTimeSeconds + TimeUnit.MINUTES.toSeconds(5);

        String headerJson = "{"
                + "\"alg\":\"" + this.alg + "\","
                + "\"kid\":\"" + this.kid + "\","
                + "\"typ\":\"" + this.typ + "\""
                + "}";
        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

        String redirectUri = "https://tpp.local.ob/ob_demo_backend_war/init/redirected";
        String state = "sxQKprDMX1";
        String nonce = "nonce";
        String aud = "https://localhost:9446/oauth2/token";
        String scope = "openid accounts payments";
        String responseType = "code id_token";

//        String hardcodedClaimsJson =
//                "\"claims\": {\n" +
//                        "    \"id_token\": {\n" +
//                        "        \"acr\": {\n" +
//                        "            \"values\": [\n" +
//                        "                \"urn:openbanking:psd2:sca\",\n" +
//                        "                \"urn:openbanking:psd2:ca\"\n" +
//                        "            ],\n" +
//                        "            \"essential\": true\n" +
//                        "        },\n" +
//                        "        \"openbanking_intent_id\": {\n" +
//                        "            \"value\": \"" + consentId + "\",\n" +
//                        "            \"essential\": true\n" +
//                        "        }\n" +
//                        "    },\n" +
//                        "    \"userinfo\": {\n" +
//                        "        \"openbanking_intent_id\": {\n" +
//                        "            \"value\": \"" + consentId + "\",\n" +
//                        "            \"essential\": true\n" +
//                        "        }\n" +
//                        "    }\n" +
//                        "}";

        String requestObjectJson = "{\n" +
                "  \"iss\": \"" + clientId + "\",\n" +
                "  \"response_type\": \"" + responseType + "\",\n" +
                "  \"redirect_uri\": \"" + redirectUri + "\",\n" +
                "  \"state\": \"" + state + "\",\n" +
                "  \"nonce\": \"" + nonce + "\",\n" +
                "  \"client_id\": \"" + clientId + "\",\n" +
                "  \"aud\": \"" + aud + "\",\n" +
                "  \"nbf\": " + currentTimeSeconds + ",\n" +
                "  \"exp\": " + expirationTimeSec + ",\n" +
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

        System.out.println("requestObjectJson: " + requestObjectJson);

        String encodedPayload = base64UrlEncode(requestObjectJson.getBytes(StandardCharsets.UTF_8));

        String signingInput = encodedHeader + "." + encodedPayload;
        byte[] signingInputBytes = signingInput.getBytes(StandardCharsets.US_ASCII);

        PrivateKey privateKey = getPrivateKey();

        Signature signature = Signature.getInstance("RSASSA-PSS");

        PSSParameterSpec pssSpec = new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1);
        signature.setParameter(pssSpec);

        signature.initSign(privateKey);
        signature.update(signingInputBytes);
        byte[] rawSignature = signature.sign();

        String encodedSignature = base64UrlEncode(rawSignature);

        return signingInput + "." + encodedSignature;
    }

    public String requestAccessToken(){

        return null;
    }
}
