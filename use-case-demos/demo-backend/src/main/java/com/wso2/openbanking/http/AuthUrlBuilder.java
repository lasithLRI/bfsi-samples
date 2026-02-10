package com.wso2.openbanking.http;

import com.wso2.openbanking.ConfigLoader;

import static org.apache.cxf.common.util.UrlUtils.urlEncode;

public class AuthUrlBuilder {

    public static String build(String requestObjectJwt, String clientId, String scope) {
        StringBuilder url = new StringBuilder(ConfigLoader.getAuthorizeUrl());

        url.append("?response_type=").append(urlEncode(ConfigLoader.getResponseType()));
        url.append("&client_id=").append(clientId);
        url.append("&scope=").append(urlEncode(scope));
        url.append("&redirect_uri=").append(urlEncode(ConfigLoader.getRedirectUri()));
        url.append("&state=").append(urlEncode(ConfigLoader.getOAuthState()));
        url.append("&request=").append(urlEncode(requestObjectJwt));
        url.append("&prompt=").append(urlEncode(ConfigLoader.getOAuthPrompt()));
        url.append("&nonce=").append(urlEncode(ConfigLoader.getOAuthNonce()));

        return url.toString();
    }

    private AuthUrlBuilder() {
        // Prevent instantiation
    }
}
