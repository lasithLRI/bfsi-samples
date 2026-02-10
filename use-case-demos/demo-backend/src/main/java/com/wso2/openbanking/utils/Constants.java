package com.wso2.openbanking.utils;

public class Constants {

    // OAuth Constants
    public static final String TOKEN_URL = "https://localhost:9446/oauth2/token";
    public static final String CLIENT_ID = "onKy05vpqDjTenzZSRjfSOfb3ZMa";
    public static final String CLIENT_SECRET = "sCekNgSWIauQ34klRhDGqfwpjc4";
    public static final String REDIRECT_URI = "https://tpp.local.ob/ob_demo_backend_war/init/redirected";

    // Open Banking URLs
    public static final String ACCOUNT_BASE_URL = "https://localhost:8243/open-banking/v3.1/aisp";
    public static final String PAYMENT_BASE_URL = "https://localhost:8243/open-banking/v3.1/pisp";

    // TLS Certificate Paths
    public static final String CERT_PATH = "/obtransport.pem";
    public static final String KEY_PATH = "/obtransport.key";
    public static final String TRUSTSTORE_PATH = "/client-truststore.jks";
    public static final String TRUSTSTORE_PASSWORD = "123456";

    // Frontend
    public static final String FRONTEND_HOME_URL = "http://localhost:5173/";

    private Constants() {
        // Prevent instantiation
    }
}
