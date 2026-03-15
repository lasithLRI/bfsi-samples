package com.wso2.openbanking.demo.services;


import com.wso2.openbanking.demo.utils.ConfigLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Singleton service that creates signed JWTs for OAuth flows.
 * Handles both client assertions (for token requests) and
 * request objects (for consent authorization).
 */
public final class JwtTokenService {

    private static final String SIGNING_KEY_PATH      = "/obsigning.key";
    private static final String SIGNATURE_ALGORITHM   = "RSASSA-PSS";
    private static final String HASH_ALGORITHM        = "SHA-256";
    private static final String MGF_ALGORITHM         = "MGF1";
    private static final int    SALT_LENGTH           = 32;
    private static final int    TRAILER_FIELD         = 1;
    private static final long   TOKEN_VALIDITY_MINUTES = 5;

    private static JwtTokenService instance;
    private final PrivateKey privateKey;

    private JwtTokenService() throws GeneralSecurityException, IOException {
        this.privateKey = loadPrivateKey();
    }

    /**
     * Returns the singleton instance, creating it on the first call.
     *
     * @return the shared JwtTokenService instance.
     * @throws GeneralSecurityException if the private key cannot be parsed.
     * @throws IOException if the signing key file cannot be read.
     */
    public static synchronized JwtTokenService getInstance()
            throws GeneralSecurityException, IOException {
        if (instance == null) {
            instance = new JwtTokenService();
        }
        return instance;
    }

    /**
     * Creates a signed client assertion JWT for authenticating to the token endpoint.
     * The JWT is signed using RSASSA-PSS / SHA-256 and is valid for five minutes
     * from the time of creation.
     *
     * @param jti a unique identifier for this token, used to prevent replay attacks.
     * @return a compact, signed JWT string in the form header.payload.signature.
     * @throws GeneralSecurityException if signing fails.
     * @throws IOException if the signing key cannot be read.
     */
    public String createClientAssertion(String jti)
            throws GeneralSecurityException, IOException {
        long issuedAt = getCurrentTimeSeconds();
        long expiration = issuedAt + TimeUnit.MINUTES.toSeconds(TOKEN_VALIDITY_MINUTES);

        JwtHeader header = new JwtHeader(
                ConfigLoader.getOAuthAlgorithm(),
                ConfigLoader.getClientKid(),
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

    /**
     * Creates a signed request object JWT carrying the consent ID for the authorization flow.
     * The JWT is signed using RSASSA-PSS / SHA-256 and is valid for five minutes
     * from the time of creation.
     *
     * @param consentId the consent ID returned by the bank after consent initiation.
     * @return a compact, signed JWT string in the form header.payload.signature.
     * @throws GeneralSecurityException if signing fails.
     * @throws IOException if the signing key cannot be read.
     */
    public String createRequestObject(String consentId)
            throws GeneralSecurityException {
        long currentTime = getCurrentTimeSeconds();
        long expiration = currentTime + TimeUnit.MINUTES.toSeconds(TOKEN_VALIDITY_MINUTES);

        JwtHeader header = new JwtHeader(
                ConfigLoader.getOAuthAlgorithm(),
                ConfigLoader.getClientKid(),
                ConfigLoader.getTokenType()
        );

        RequestObjectPayload payload = new RequestObjectPayload.Builder()
                .iss(ConfigLoader.getClientId())
                .responseType(ConfigLoader.getResponseType())
                .redirectUri(ConfigLoader.getRedirectUri())
                .state(ConfigLoader.getOAuthState())
                .nonce(ConfigLoader.getOAuthNonce())
                .aud(ConfigLoader.getTokenUrl())
                .nbf(currentTime)
                .exp(expiration)
                .scope("openid accounts payments")
                .consentId(consentId)
                .build();

        return buildJwt(header.toJson(), payload.toJson());
    }

    /**
     * Base64URL-encodes the header and payload, then appends the PSS signature
     * to produce a compact JWT string.
     *
     * @param headerJson  the serialized JWT header as a JSON string.
     * @param payloadJson the serialized JWT payload as a JSON string.
     * @return a compact JWT string in the form header.payload.signature.
     * @throws GeneralSecurityException if signing fails.
     */
    private String buildJwt(String headerJson, String payloadJson)
            throws GeneralSecurityException {
        String encodedHeader  = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput   = encodedHeader + "." + encodedPayload;
        return signingInput + "." + signData(signingInput);
    }

    /**
     * Signs the given data with the loaded private key using RSASSA-PSS / SHA-256.
     *
     * @param data the ASCII signing input, typically header.payload.
     * @return the Base64URL-encoded PSS signature string.
     * @throws GeneralSecurityException if the signing operation fails.
     */
    private String signData(String data) throws GeneralSecurityException {
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

    /**
     * Loads the RSA private key from the classpath signing key file.
     *
     * @return the parsed RSA PrivateKey.
     * @throws NoSuchAlgorithmException if the RSA algorithm is unavailable.
     * @throws InvalidKeySpecException if the key file content is malformed.
     * @throws IOException if the signing key file cannot be found or read.
     */
    private PrivateKey loadPrivateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        InputStream keyStream = JwtTokenService.class.getResourceAsStream(SIGNING_KEY_PATH);
        if (keyStream == null) {
            throw new IOException("Signing key not found on classpath: " + SIGNING_KEY_PATH);
        }
        return KeyReader.loadPrivateKeyFromStream(keyStream);
    }

    /**
     * Encodes bytes to a Base64URL string without padding, as required by the JWT spec.
     *
     * @param bytes the raw bytes to encode.
     * @return a Base64URL-encoded string with no trailing padding characters.
     */
    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the current time in seconds since the Unix epoch.
     *
     * @return the current Unix timestamp in seconds.
     */
    private long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
