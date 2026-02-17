package com.wso2.openbanking;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties prop = new Properties();

    static {
        try (InputStream inputStream = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            prop.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    public static String getProperty(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }

    public static String getClientId() { return getProperty("oauth.client.id"); }
    public static String getClientSecret() { return getProperty("oauth.client.secret"); }
    public static String getOAuthAlgorithm() { return getProperty("oauth.algorithm"); }
    public static String getTokenType() { return getProperty("oauth.token.type"); }
    public static String getTokenUrl() { return getProperty("oauth.token.url"); }
    public static String getAuthorizeUrl() { return getProperty("oauth.authorize.url"); }
    public static String getRedirectUri() { return getProperty("oauth.redirect.uri"); }
    public static String getOAuthState() { return getProperty("oauth.state"); }
    public static String getOAuthNonce() { return getProperty("oauth.nonce"); }
    public static String getOAuthPrompt() { return getProperty("oauth.prompt"); }
    public static String getResponseType() { return getProperty("oauth.response.type"); }

    public static String getAccountBaseUrl() { return getProperty("openbanking.account.base.url"); }
    public static String getPaymentBaseUrl() { return getProperty("openbanking.payment.base.url"); }

    public static String getFapiFinancialId() { return getProperty("openbanking.fapi.financial.id"); }
    public static String getPaymentIdempotencyKey() { return getProperty("openbanking.payment.idempotency.key"); }

    public static String getCertificatePath() { return getProperty("ssl.certificate.path"); }
    public static String getKeyPath() { return getProperty("ssl.key.path"); }
    public static String getTruststorePath() { return getProperty("ssl.truststore.path"); }
    public static String getTruststorePassword() { return getProperty("ssl.truststore.password"); }

    public static String getFrontendHomeUrl() { return getProperty("frontend.home.url"); }

    public static String getMockBankName() { return getProperty("mock.bank.name"); }
    public static String getMockBankLogo() { return getProperty("mock.bank.logo"); }
    public static String getMockBankPrimaryColor() { return getProperty("mock.bank.color.primary"); }
    public static String getMockBankSecondaryColor() { return getProperty("mock.bank.color.secondary"); }

    private ConfigLoader() {}
}
