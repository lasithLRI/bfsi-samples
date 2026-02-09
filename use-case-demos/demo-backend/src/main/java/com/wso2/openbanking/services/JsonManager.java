package com.wso2.openbanking.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class JsonManager {
    private final ObjectMapper mapper = new ObjectMapper();
    private final PrivateKey privateKey;

    public JsonManager() throws Exception {

        try(InputStream stream = JsonManager.class.getResourceAsStream("/obsigning.key")){
            if(stream == null){
                throw new IllegalArgumentException("No Sign File found");
            }

            System.out.println(stream);
            if (stream == null) {
                throw new FileNotFoundException("obsigning.key not found in classpath");
            }

            this.privateKey = KeyReader.loadPrivateKeyFromStream(stream);

        }catch (Exception e){
            System.err.println("Error reading or parsing JSON file: " + e.getMessage());
            throw new RuntimeException("Failed to load configurations.", e);
        }


    }

    // ===== Base64 URL encoding =====
    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    // ===== Generate key ID (kid) from public key =====
    private String generateKidFromPublicKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(
                new X509EncodedKeySpec(privateKey.getEncoded())
        );
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(publicKey.getEncoded());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    // ===== PS256 signing =====
    private String signPS256(String data) throws Exception {
        Signature signature = Signature.getInstance("RSASSA-PSS");
        signature.setParameter(new java.security.spec.PSSParameterSpec(
                "SHA-256",
                "MGF1",
                java.security.spec.MGF1ParameterSpec.SHA256,
                32,
                1
        ));
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        return base64UrlEncode(signature.sign());
    }

    // ===== JWT Claims model =====
    public static class JwtClaims {
        public String iss;
        public String sub;
        public String aud;
        public long iat;
        public long nbf;
        public long exp;
        public String jti;

        public JwtClaims(String iss, String sub, String aud, long expiresInSeconds) {
            long now = System.currentTimeMillis() / 1000;
            this.iss = iss;
            this.sub = sub;
            this.aud = aud;
            this.iat = now;
            this.nbf = now;
            this.exp = now + expiresInSeconds;
            this.jti = java.util.UUID.randomUUID().toString();
        }
    }

    // ===== Create JWT PS256 with claims + optional custom payload =====
    public String createPS256JWT(String iss,
                                 String sub,
                                 String aud,
                                 long expiresInSeconds,
                                 Map<String, Object> customPayload) throws Exception {

        // 1. Base claims
        JwtClaims claims = new JwtClaims(iss, sub, aud, expiresInSeconds);

        // 2. Convert claims to JSON
        ObjectNode payloadNode = (ObjectNode) mapper.readTree(mapper.writeValueAsString(claims));

        // 3. Merge custom payload if provided
        if (customPayload != null) {
            customPayload.forEach((key, value) -> {
                payloadNode.putPOJO(key, value);
            });
        }

        // 4. JWT header with PS256 + kid
        ObjectNode headerNode = mapper.createObjectNode();
        headerNode.put("alg", "PS256");
        headerNode.put("typ", "JWT");
        headerNode.put("kid", "sCekNgSWIauQ34klRhDGqfwpjc4");

        String headerJson = mapper.writeValueAsString(headerNode);
        String payloadJson = mapper.writeValueAsString(payloadNode);

        // 5. Base64URL encode
        String encodedHeader = base64UrlEncode(headerJson.getBytes());
        String encodedPayload = base64UrlEncode(payloadJson.getBytes());

        // 6. Create signing input
        String signingInput = encodedHeader + "." + encodedPayload;

        // 7. Sign using PS256
        String signature = signPS256(signingInput);

        // 8. Return compact JWT
        return encodedHeader + "." + encodedPayload + "." + signature;
    }
}
