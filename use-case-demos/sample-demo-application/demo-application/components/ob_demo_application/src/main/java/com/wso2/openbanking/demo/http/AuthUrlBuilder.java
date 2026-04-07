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

import com.wso2.openbanking.demo.utils.ConfigLoader;

import static org.apache.cxf.common.util.UrlUtils.urlEncode;

/** AuthUrlBuilder implementation. */
public class AuthUrlBuilder {


    /**
     * Builds an OAuth authorization URL with the required query parameters.
     *
     * @param requestObjectJwt the signed JWT request object to be included as the {@code request} parameter
     * @param clientId         the OAuth client ID
     * @param scope            the requested OAuth scopes
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
