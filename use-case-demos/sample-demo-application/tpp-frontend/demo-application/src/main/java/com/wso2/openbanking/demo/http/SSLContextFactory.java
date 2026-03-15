package com.wso2.openbanking.demo.http;

import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.services.KeyReader;

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
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
/**
 * Factory for creating a mutually authenticated TLS SSLContext from classpath resources.
 * Loads the client certificate and private key for the key manager, and optionally
 * loads a truststore for the trust manager. If no truststore path is provided,
 * the JVM default trust store is used.
 */
public class SSLContextFactory {

    /**
     * Creates and initializes a TLS SSLContext using the provided client certificate,
     * private key, and truststore material.
     *
     * @param certPath           classpath path to the PEM-encoded client certificate.
     * @param keyPath            classpath path to the PEM-encoded PKCS8 private key.
     * @param trustStorePath     classpath path to the truststore file, or null to use the JVM default.
     * @param trustStorePassword password for the truststore, or null if not required.
     * @return a fully initialized SSLContext ready for use in mTLS connections.
     * @throws SSLContextCreationException if the SSLContext cannot be initialized for any reason.
     */
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

    /**
     * Builds a KeyManager array from the client certificate and private key.
     * The certificate and key are loaded from the classpath and stored in an
     * in-memory PKCS12 keystore before being passed to the KeyManagerFactory.
     *
     * @param certPath classpath path to the PEM-encoded client certificate.
     * @param keyPath  classpath path to the PEM-encoded PKCS8 private key.
     * @return an array of KeyManagers initialized with the client key material.
     * @throws SSLContextCreationException if any key material cannot be loaded or parsed.
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
        } catch (Exception e) {
            throw new SSLContextCreationException("Failed to read key material", e);
        }
    }

    /**
     * Builds a TrustManager array from the truststore at the given classpath path.
     * If trustStorePath is null, an empty array is returned and the JVM default
     * trust store will be used by the SSLContext.
     *
     * @param trustStorePath     classpath path to the truststore file, or null to use the JVM default.
     * @param trustStorePassword password for the truststore, or null if not required.
     * @return an array of TrustManagers initialized from the truststore, or an empty array.
     * @throws SSLContextCreationException if the truststore cannot be loaded or parsed.
     */
    private static TrustManager[] createTrustManagers(String trustStorePath, String trustStorePassword)
            throws SSLContextCreationException {
        if (trustStorePath == null) {
            return new TrustManager[0];
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
}
