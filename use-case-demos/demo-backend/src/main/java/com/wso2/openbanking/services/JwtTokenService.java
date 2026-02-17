package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class JwtTokenService {

    private static final String SIGNING_KEY_PATH = "/obsigning.key";
    private static final String SIGNATURE_ALGORITHM = "RSASSA-PSS";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String MGF_ALGORITHM = "MGF1";
    private static final int SALT_LENGTH = 32;
    private static final int TRAILER_FIELD = 1;
    private static final long TOKEN_VALIDITY_MINUTES = 5;

    private static JwtTokenService instance;
    private final PrivateKey privateKey;

    private JwtTokenService() throws Exception {
        this.privateKey = loadPrivateKey();
    }

    public static synchronized JwtTokenService getInstance() throws Exception {
        if (instance == null) {
            instance = new JwtTokenService();
        }
        return instance;
    }

    public String createClientAssertion(String jti) throws Exception {
        long issuedAt = getCurrentTimeSeconds();
        long expiration = issuedAt + TimeUnit.MINUTES.toSeconds(TOKEN_VALIDITY_MINUTES);

        JwtHeader header = new JwtHeader(
                ConfigLoader.getOAuthAlgorithm(),
                ConfigLoader.getClientSecret(),
                ConfigLoader.getTokenType()
        );

        ClientAssertionPayload payload = new ClientAssertionPayload(
                ConfigLoader.getClientId(),
                ConfigLoader.getClientId(),
                expiration,
                issuedAt,
                jti,
                ConfigLoader.getTokenUrl()
        );

        return buildJwt(header.toJson(), payload.toJson());
    }

    public String createRequestObject(String consentId, String jti) throws Exception {
        long currentTime = getCurrentTimeSeconds();
        long expiration = currentTime + TimeUnit.MINUTES.toSeconds(TOKEN_VALIDITY_MINUTES);

        JwtHeader header = new JwtHeader(
                ConfigLoader.getOAuthAlgorithm(),
                ConfigLoader.getClientSecret(),
                ConfigLoader.getTokenType()
        );

        RequestObjectPayload payload = new RequestObjectPayload(
                ConfigLoader.getClientId(),
                ConfigLoader.getResponseType(),
                ConfigLoader.getRedirectUri(),
                ConfigLoader.getOAuthState(),
                ConfigLoader.getOAuthNonce(),
                ConfigLoader.getTokenUrl(),
                currentTime,
                expiration,
                "openid accounts payments",
                consentId
        );

        return buildJwt(header.toJson(), payload.toJson());
    }

    private String buildJwt(String headerJson, String payloadJson) throws Exception {
        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = encodedHeader + "." + encodedPayload;
        return signingInput + "." + signData(signingInput);
    }

    private String signData(String data) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.setParameter(new PSSParameterSpec(
                HASH_ALGORITHM,
                MGF_ALGORITHM,
                new MGF1ParameterSpec(HASH_ALGORITHM),
                SALT_LENGTH,
                TRAILER_FIELD
        ));
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.US_ASCII));
        return base64UrlEncode(signature.sign());
    }

    private PrivateKey loadPrivateKey() throws Exception {
        return KeyReader.loadPrivateKeyFromStream(
                JwtTokenService.class.getResourceAsStream(SIGNING_KEY_PATH)
        );
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
