package com.wso2.openbanking.http;

import com.wso2.openbanking.exception.SSLContextCreationException;
import com.wso2.openbanking.services.KeyReader;

import javax.net.ssl.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

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
                    SSLContextFactory.class.getResourceAsStream(keyPath)
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

    private static TrustManager[] createTrustManagers(String trustStorePath, String trustStorePassword)
            throws SSLContextCreationException {

        if (trustStorePath == null) {
            return new TrustManager[0]; // Fall back to JVM default trust store
        }

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = trustStorePassword != null ? trustStorePassword.toCharArray() : null;

            try (InputStream is = SSLContextFactory.class.getResourceAsStream(trustStorePath)) {
                if (is == null) {
                    throw new FileNotFoundException("Trust store not found: " + trustStorePath);
                }
                trustStore.load(is, password);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            tmf.init(trustStore);

            return tmf.getTrustManagers();

        } catch (FileNotFoundException e) {
            throw new SSLContextCreationException("Trust store file not found: " + e.getMessage(), e);
        } catch (KeyStoreException e) {
            throw new SSLContextCreationException("Failed to initialize trust store", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextCreationException("Trust manager algorithm not available", e);
        } catch (CertificateException e) {
            throw new SSLContextCreationException("Failed to load certificate into trust store", e);
        } catch (IOException e) {
            throw new SSLContextCreationException("Failed to read trust store", e);
        }
    }

    private SSLContextFactory() {}
}
