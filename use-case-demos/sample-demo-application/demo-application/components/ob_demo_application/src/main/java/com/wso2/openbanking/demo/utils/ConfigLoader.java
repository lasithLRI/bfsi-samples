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

package com.wso2.openbanking.demo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** ConfigLoader implementation. */
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
     * Executes the getProperty operation and modify the payload if necessary.
     *
     * @param key             The key parameter
     */
    public static String getProperty(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }
        return value;
    }

    /**
     * Executes the getProperty operation and modify the payload if necessary.
     *
     * @param key             The key parameter
     * @param defaultValue    The defaultValue parameter
     */
    public static String getProperty(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }

    /**
     * Executes the getClientId operation and modify the payload if necessary.
     */
    public static String getClientId() {
        return getProperty("oauth.client.id");
    }

    /**
     * Executes the getClientKid operation and modify the payload if necessary.
     */
    public static String getClientKid() {
        return getProperty("oauth.client.kid");
    }

    /**
     * Executes the getOAuthAlgorithm operation and modify the payload if necessary.
     */
    public static String getOAuthAlgorithm() {
        return getProperty("oauth.algorithm");
    }

    /**
     * Executes the getTokenType operation and modify the payload if necessary.
     */
    public static String getTokenType() {
        return getProperty("oauth.token.type");
    }

    /**
     * Executes the getTokenUrl operation and modify the payload if necessary.
     */
    public static String getTokenUrl() {
        return getProperty("oauth.token.url");
    }

    /**
     * Executes the getAuthorizeUrl operation and modify the payload if necessary.
     */
    public static String getAuthorizeUrl() {
        return getProperty("oauth.authorize.url");
    }

    /**
     * Executes the getRedirectUri operation and modify the payload if necessary.
     */
    public static String getRedirectUri() {
        return getProperty("oauth.redirect.uri");
    }

    /**
     * Executes the getOAuthState operation and modify the payload if necessary.
     */
    public static String getOAuthState() {
        return getProperty("oauth.state");
    }

    /**
     * Executes the getOAuthNonce operation and modify the payload if necessary.
     */
    public static String getOAuthNonce() {
        return getProperty("oauth.nonce");
    }

    /**
     * Executes the getOAuthPrompt operation and modify the payload if necessary.
     */
    public static String getOAuthPrompt() {
        return getProperty("oauth.prompt");
    }

    /**
     * Executes the getResponseType operation and modify the payload if necessary.
     */
    public static String getResponseType() {
        return getProperty("oauth.response.type");
    }

    /**
     * Executes the getAccountBaseUrl operation and modify the payload if necessary.
     */
    public static String getAccountBaseUrl() {
        return getProperty("openbanking.account.base.url");
    }

    /**
     * Executes the getPaymentBaseUrl operation and modify the payload if necessary.
     */
    public static String getPaymentBaseUrl() {
        return getProperty("openbanking.payment.base.url");
    }

    /**
     * Executes the getFapiFinancialId operation and modify the payload if necessary.
     */
    public static String getFapiFinancialId() {
        return getProperty("openbanking.fapi.financial.id");
    }

    /**
     * Executes the getCertificatePath operation and modify the payload if necessary.
     */
    public static String getCertificatePath() {
        return getProperty("ssl.certificate.path");
    }

    /**
     * Executes the getKeyPath operation and modify the payload if necessary.
     */
    public static String getKeyPath() {
        return getProperty("ssl.key.path");
    }

    /**
     * Executes the getTruststorePath operation and modify the payload if necessary.
     */
    public static String getTruststorePath() {
        return getProperty("ssl.truststore.path");
    }

    /**
     * Executes the getTruststorePassword operation and modify the payload if necessary.
     */
    public static String getTruststorePassword() {
        return getProperty("ssl.truststore.password");
    }

    /**
     * Executes the getFrontendHomeUrl operation and modify the payload if necessary.
     */
    public static String getFrontendHomeUrl() {
        return getProperty("frontend.home.url");
    }

    /**
     * Executes the getBackendBaseUrl operation and modify the payload if necessary.
     */
    public static String getBackendBaseUrl() {
        return getProperty("backend.base.url");
    }

    /**
     * Executes the getMockBankName operation and modify the payload if necessary.
     */
    public static String getMockBankName() {
        return getProperty("mock.bank.name");
    }

    /**
     * Executes the getMockBankLogo operation and modify the payload if necessary.
     */
    public static String getMockBankLogo() {
        return getProperty("mock.bank.logo");
    }

    /**
     * Executes the getMockBankPrimaryColor operation and modify the payload if necessary.
     */
    public static String getMockBankPrimaryColor() {
        return getProperty("mock.bank.color.primary");
    }

    /**
     * Executes the getMockBankSecondaryColor operation and modify the payload if necessary.
     */
    public static String getMockBankSecondaryColor() {

        return getProperty("mock.bank.color.secondary");
    }

    /**
     * Executes the getCorsAllowedOrigin operation and modify the payload if necessary.
     */
    public static String getCorsAllowedOrigin() {

        return getProperty("cors.allowed.origin");
    }

    /**
     * Executes the getIsBaseUrl operation and modify the payload if necessary.
     */
    public static String getIsBaseUrl() {
        return getProperty("is.base.url");
    }
}
