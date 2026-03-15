package com.wso2.openbanking.demo.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for loading private keys from PEM-encoded input streams.
 */
public class KeyReader {

    /**
     * Reads a PKCS8 PEM private key from the given stream, strips the header/footer
     * and whitespace, then decodes and returns it as an RSA PrivateKey.
     *
     * @param in the input stream containing the PEM-encoded PKCS8 private key.
     * @return the parsed RSA PrivateKey.
     * @throws IOException if the stream cannot be read.
     * @throws NoSuchAlgorithmException if the RSA algorithm is unavailable.
     * @throws InvalidKeySpecException if the key content is malformed or not a valid PKCS8 key.
     */
    public static PrivateKey loadPrivateKeyFromStream(InputStream in)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String keyPem = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        keyPem = keyPem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }
}
