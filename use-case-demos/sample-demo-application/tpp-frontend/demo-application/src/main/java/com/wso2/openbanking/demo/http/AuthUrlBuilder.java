package com.wso2.openbanking.demo.http;

import com.wso2.openbanking.demo.utils.ConfigLoader;

import static org.apache.cxf.common.util.UrlUtils.urlEncode;

/**
 * Utility class for building the authorization URL used in the OAuth2 authorization flow
 * for open banking operations.
 */
public class AuthUrlBuilder {

    /**
     * Builds the authorization URL with the required OAuth2 query parameters.
     *
     * @param requestObjectJwt the signed JWT request object containing authorization details
     * @param clientId         the OAuth2 client ID of the application
     * @param scope            the requested OAuth2 scopes
     * @return the fully constructed authorization URL as a string
     */
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
}
