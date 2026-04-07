/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.demo.service;

import org.json.JSONObject;

/** ClientAssertionPayload implementation. */
public class ClientAssertionPayload {

    private final String iss;

    private final String sub;

    private final long exp;

    private final long iat;

    private final String jti;

    private final String aud;

    public ClientAssertionPayload(String iss, String sub, long exp, long iat, String jti, String aud) {
        this.iss = iss;
        this.sub = sub;
        this.exp = exp;
        this.iat = iat;
        this.jti = jti;
        this.aud = aud;
    }

    public String toJson() {
        return new JSONObject()
                .put("iss", iss)
                .put("sub", sub)
                .put("exp", exp)
                .put("iat", iat)
                .put("jti", jti)
                .put("aud", aud)
                .toString();
    }
}
