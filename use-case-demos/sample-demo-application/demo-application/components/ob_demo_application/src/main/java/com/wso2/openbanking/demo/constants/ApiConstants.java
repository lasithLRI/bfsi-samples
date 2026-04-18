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

package com.wso2.openbanking.demo.constants;

/** Constants for API response fields and request status values. */
public final class ApiConstants {

    private ApiConstants() { }

    // Request status
    public static final String STATUS_ACCOUNTS = "accounts";
    public static final String STATUS_PAYMENTS = "payments";

    // Response field keys
    public static final String FIELD_TYPE    = "type";
    public static final String FIELD_STATUS  = "status";
    public static final String FIELD_SUCCESS = "success";
    public static final String FIELD_ERROR   = "error";

    // Response values
    public static final String VALUE_SUCCESS = "success";
    public static final String VALUE_REVOKED = "revoked";
}