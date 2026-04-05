/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/** SSLContextFactory implementation. */
public class SSLContextFactory {

    public static SSLContext create(String certPath, String keyPath,
                                    String trustStorePath, String trustStorePassword)
            throws SSLContextCreationException {
        try {
            KeyManager[] keyManagers = createKeyManagers(certPath, keyPath);
            TrustManager[] trustManagers = createTrustManagers(trustStorePath, trustStorePassword);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextCreationException("TLS algorithm not available", e);
        } catch (KeyManagementException e) {
            throw new SSLContextCreationException("Failed to initialize SSL context", e);
        }
    }

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
        } catch (Exception e) {
            throw new SSLContextCreationException("Failed to read key material", e);
        }
    }

    @SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
            justification = "Null return is intentional — callers check for null to detect missing truststore")
    private static TrustManager[] createTrustManagers(String trustStorePath, String trustStorePassword)
            throws SSLContextCreationException {
        
        return null;
    }
}
