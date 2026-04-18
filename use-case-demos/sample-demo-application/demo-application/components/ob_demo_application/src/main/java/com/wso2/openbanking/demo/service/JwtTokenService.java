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

/** JwtTokenService implementation. */
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

    public static synchronized JwtTokenService getInstance()
            throws GeneralSecurityException, IOException {
        if (instance == null) {
            instance = new JwtTokenService();
        }
        return instance;
    }

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
     * Creates a signed JWT request object containing the authorization claims for the given consent ID.
     *
     * @param consentId the consent ID to include in the request object payload
     * @return a signed JWT string representing the request object
     * @throws GeneralSecurityException if JWT signing fails
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
     * Builds a signed JWT string from the given header and payload JSON.
     *
     * @param headerJson  the JSON string representing the JWT header
     * @param payloadJson the JSON string representing the JWT payload
     * @return a signed JWT string in the format {@code header.payload.signature}
     * @throws GeneralSecurityException if signing the JWT fails
     */
    private String buildJwt(String headerJson, String payloadJson)
            throws GeneralSecurityException {
        String encodedHeader  = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput   = encodedHeader + "." + encodedPayload;
        return signingInput + "." + signData(signingInput);
    }

    /**
     * Signs the given data using RSA-PSS and returns the Base64URL-encoded signature.
     *
     * @param data the string to be signed
     * @return the Base64URL-encoded RSA-PSS signature
     * @throws GeneralSecurityException if the signing algorithm is unavailable or signing fails
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

    private PrivateKey loadPrivateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        InputStream keyStream = JwtTokenService.class.getResourceAsStream(SIGNING_KEY_PATH);
        if (keyStream == null) {
            throw new IOException("Signing key not found on classpath: " + SIGNING_KEY_PATH);
        }
        return KeyReader.loadPrivateKeyFromStream(keyStream);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
