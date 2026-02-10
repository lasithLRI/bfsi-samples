package com.wso2.openbanking.http;

import static org.apache.cxf.common.util.UrlUtils.urlEncode;

public class AuthUrlBuilder {

    private static final String AUTH_BASE_URL = "https://localhost:9446/oauth2/authorize";
    private static final String REDIRECT_URI = "https://tpp.local.ob/ob_demo_backend_war/init/redirected";
    private static final String STATE = "YWlzcDozMTQ2";
    private static final String NONCE = "nonce";
    private static final String PROMPT = "login";
    private static final String RESPONSE_TYPE = "code id_token";

    public static String build(String requestObjectJwt, String clientId, String scope) {
        StringBuilder url = new StringBuilder(AUTH_BASE_URL);

        url.append("?response_type=").append(urlEncode(RESPONSE_TYPE));
        url.append("&client_id=").append(clientId);
        url.append("&scope=").append(urlEncode(scope));
        url.append("&redirect_uri=").append(urlEncode(REDIRECT_URI));
        url.append("&state=").append(urlEncode(STATE));
        url.append("&request=").append(urlEncode(requestObjectJwt));
        url.append("&prompt=").append(urlEncode(PROMPT));
        url.append("&nonce=").append(urlEncode(NONCE));

        return url.toString();
    }

    private AuthUrlBuilder() {
        // Prevent instantiation
    }
}
