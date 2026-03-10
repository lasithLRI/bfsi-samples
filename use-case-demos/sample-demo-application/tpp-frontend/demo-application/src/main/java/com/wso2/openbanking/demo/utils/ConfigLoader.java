package com.wso2.openbanking.demo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and provides access to application configuration properties
 * from the application.properties file on the classpath.
 * This class is not instantiable.
 */
public class ConfigLoader {

    private static final Properties prop = new Properties();

    static {
        try (InputStream inputStream = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            prop.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    /**
     * Returns the value of the specified property key.
     *
     * @param key the property key to look up
     * @return the property value
     * @throws RuntimeException if the property key is not found
     */
    public static String getProperty(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }
        return value;
    }

    /**
     * Returns the value of the specified property key, or a default value if not found.
     *
     * @param key          the property key to look up
     * @param defaultValue the value to return if the key is not found
     * @return the property value or the default value
     */
    public static String getProperty(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }

    /**
     * Returns the OAuth client ID.
     *
     * @return the OAuth client ID
     */
    public static String getClientId() {
        return getProperty("oauth.client.id");
    }

    /**
     * Returns the OAuth client key ID (kid).
     *
     * @return the OAuth client kid
     */
    public static String getClientKid() {
        return getProperty("oauth.client.kid");
    }

    /**
     * Returns the OAuth signing algorithm.
     *
     * @return the OAuth algorithm
     */
    public static String getOAuthAlgorithm() {
        return getProperty("oauth.algorithm");
    }

    /**
     * Returns the OAuth token type.
     *
     * @return the OAuth token type
     */
    public static String getTokenType() {
        return getProperty("oauth.token.type");
    }

    /**
     * Returns the OAuth token endpoint URL.
     *
     * @return the token URL
     */
    public static String getTokenUrl() {
        return getProperty("oauth.token.url");
    }

    /**
     * Returns the OAuth authorization endpoint URL.
     *
     * @return the authorize URL
     */
    public static String getAuthorizeUrl() {
        return getProperty("oauth.authorize.url");
    }

    /**
     * Returns the OAuth redirect URI.
     *
     * @return the redirect URI
     */
    public static String getRedirectUri() {
        return getProperty("oauth.redirect.uri");
    }

    /**
     * Returns the OAuth state parameter.
     *
     * @return the OAuth state
     */
    public static String getOAuthState() {
        return getProperty("oauth.state");
    }

    /**
     * Returns the OAuth nonce parameter.
     *
     * @return the OAuth nonce
     */
    public static String getOAuthNonce() {
        return getProperty("oauth.nonce");
    }

    /**
     * Returns the OAuth prompt parameter.
     *
     * @return the OAuth prompt
     */
    public static String getOAuthPrompt() {
        return getProperty("oauth.prompt");
    }

    /**
     * Returns the OAuth response type.
     *
     * @return the OAuth response type
     */
    public static String getResponseType() {
        return getProperty("oauth.response.type");
    }

    /**
     * Returns the base URL for the open banking account API.
     *
     * @return the account base URL
     */
    public static String getAccountBaseUrl() {
        return getProperty("openbanking.account.base.url");
    }

    /**
     * Returns the base URL for the open banking payment API.
     *
     * @return the payment base URL
     */
    public static String getPaymentBaseUrl() {
        return getProperty("openbanking.payment.base.url");
    }

    /**
     * Returns the FAPI financial ID used in open banking API requests.
     *
     * @return the FAPI financial ID
     */
    public static String getFapiFinancialId() {
        return getProperty("openbanking.fapi.financial.id");
    }

    /**
     * Returns the file path to the SSL client certificate.
     *
     * @return the certificate path
     */
    public static String getCertificatePath() {
        return getProperty("ssl.certificate.path");
    }

    /**
     * Returns the file path to the SSL private key.
     *
     * @return the key path
     */
    public static String getKeyPath() {
        return getProperty("ssl.key.path");
    }

    /**
     * Returns the file path to the SSL truststore.
     *
     * @return the truststore path
     */
    public static String getTruststorePath() {
        return getProperty("ssl.truststore.path");
    }

    /**
     * Returns the password for the SSL truststore.
     *
     * @return the truststore password
     */
    public static String getTruststorePassword() {
        return getProperty("ssl.truststore.password");
    }

    /**
     * Returns the frontend home URL used for post-authorization redirects.
     *
     * @return the frontend home URL
     */
    public static String getFrontendHomeUrl() {
        return getProperty("frontend.home.url");
    }

    /**
     * Returns the base URL of this backend application, used in the OAuth redirect page
     * to call /processAuth after the bank redirects the user back.
     * Includes the context path and JAX-RS root segment with no trailing slash.
     * Example: https://tpp.local.ob/ob_demo_backend_war/init
     *
     * @return the backend base URL
     */
    public static String getBackendBaseUrl() {
        return getProperty("backend.base.url");
    }

    /**
     * Returns the display name of the mock bank.
     *
     * @return the mock bank name
     */
    public static String getMockBankName() {
        return getProperty("mock.bank.name");
    }

    /**
     * Returns the logo URL or path for the mock bank.
     *
     * @return the mock bank logo
     */
    public static String getMockBankLogo() {
        return getProperty("mock.bank.logo");
    }

    /**
     * Returns the primary color for the mock bank's branding.
     *
     * @return the mock bank primary color
     */
    public static String getMockBankPrimaryColor() {
        return getProperty("mock.bank.color.primary");
    }

    /**
     * Returns the secondary color for the mock bank's branding.
     *
     * @return the mock bank secondary color
     */
    public static String getMockBankSecondaryColor() {
        return getProperty("mock.bank.color.secondary");
    }

    /**
     * Returns the allowed origin for CORS configuration.
     *
     * @return the CORS allowed origin
     */
    public static String getCorsAllowedOrigin() {
        return getProperty("cors.allowed.origin");
    }

    public static String getIsBaseUrl(){
        return getProperty("is.base.url");
    }
}
