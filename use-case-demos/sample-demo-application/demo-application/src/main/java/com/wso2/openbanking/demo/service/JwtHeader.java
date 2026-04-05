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

package com.wso2.openbanking.demo.service;

import org.json.JSONObject;

/** JwtHeader implementation. */
public class JwtHeader {

    private final String alg;
    private final String kid;
    private final String typ;

    public JwtHeader(String alg, String kid, String typ) {
        this.alg = alg;
        this.kid = kid;
        this.typ = typ;
    }

    /**
     * Executes the toJson operation and modify the payload if necessary.
     */
    public String toJson() {
        return new JSONObject()
                .put("alg", alg)
                .put("kid", kid)
                .put("typ", typ)
                .toString();
    }
}
