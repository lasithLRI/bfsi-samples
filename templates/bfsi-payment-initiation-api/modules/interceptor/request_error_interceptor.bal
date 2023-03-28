// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/http;
import ballerina/log;
import bfsi_payment_initiation_api.util;

# A `RequestErrorInterceptor` service class implementation. It allows intercepting
# the error that occurred in the request path and handle it accordingly
# A `RequestErrorInterceptor` service class can have only one resource function
public isolated service class RequestErrorInterceptor {
    *http:RequestErrorInterceptor;

    // The resource function inside a `RequestErrorInterceptor` is only allowed 
    // to have the default method and path. The error occurred in the interceptor
    // execution can be accessed by the mandatory argument: `error`.
    isolated resource function 'default [string... path](error err) returns util:BadRequest {
        log:printError("Invalid Request: ", err);

        return {
            mediaType: "application/org+json",
            body: {
                Message: err.message(),
                ErrorCode: util:CODE_HEADER_INVALID
            }
        };
    }
}
