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

package com.wso2.openbanking.demo.http;

import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.service.KeyReader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/** SSLContextFactory implementation. */
public class SSLContextFactory {
    private SSLContextFactory() {
        /* This utility class should not be instantiated */
    }

    /**
     * Creates and initializes a TLS SSLContext using the provided certificate and private key.
     *
     * @param certPath path to the PEM-encoded certificate file
     * @param keyPath  path to the PEM-encoded private key file
     * @return an initialized {@link SSLContext} configured with the provided credentials
     * @throws SSLContextCreationException if the TLS algorithm is unavailable or SSL context initialization fails
     */
    public static SSLContext create(String certPath, String keyPath)
            throws SSLContextCreationException {
        try {
            KeyManager[] keyManagers = createKeyManagers(certPath, keyPath);
            TrustManager[] trustManagers = createTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextCreationException("TLS algorithm not available", e);
        } catch (KeyManagementException e) {
            throw new SSLContextCreationException("Failed to initialize SSL context", e);
        }
    }

    /**
     * Creates an array of {@link KeyManager}s from the provided certificate and private key files.
     *
     * @param certPath path to the PEM-encoded X.509 certificate file
     * @param keyPath  path to the PEM-encoded private key file
     * @return an array of {@link KeyManager}s initialized with the provided credentials
     * @throws SSLContextCreationException if any key material file is missing, unparseable, or fails to load into the key store
     */
    private static KeyManager[] createKeyManagers(String certPath, String keyPath)
            throws SSLContextCreationException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate clientCert;

            try (InputStream certInput = SSLContextFactory.class.getResourceAsStream(certPath)) {
                if (certInput == null) {
                    throw new FileNotFoundException("Certificate not found: " + certPath);
                }
                clientCert = (X509Certificate) cf.generateCertificate(certInput);
            }

            PrivateKey privateKey = KeyReader.loadPrivateKeyFromStream(
                    Objects.requireNonNull(SSLContextFactory.class.getResourceAsStream(keyPath))
            );

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry("client", privateKey, new char[0],
                    new java.security.cert.Certificate[]{clientCert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm()
            );
            kmf.init(keyStore, new char[0]);

            return kmf.getKeyManagers();

        } catch (FileNotFoundException e) {
            throw new SSLContextCreationException("Key material file not found: " + e.getMessage(), e);
        } catch (CertificateException e) {
            throw new SSLContextCreationException("Failed to parse client certificate", e);
        } catch (KeyStoreException e) {
            throw new SSLContextCreationException("Failed to initialize PKCS12 key store", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextCreationException("Key manager algorithm not available", e);
        } catch (UnrecoverableKeyException e) {
            throw new SSLContextCreationException("Failed to recover key from key store", e);
        } catch (IOException | InvalidKeySpecException e) {
            throw new SSLContextCreationException("Failed to read key material", e);
        }
    }

    private static TrustManager[] createTrustManagers(){
        return null;
    }
}
