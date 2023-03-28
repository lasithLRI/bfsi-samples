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
import bfsi_payment_initiation_api.model;

// A `ResponseErrorInterceptor` service class implementation. It allows intercepting
// the error that occurred in the response path and handle it accordingly.
// A `ResponseErrorInterceptor` service class can have only one resource function.
public isolated service class ResponseErrorInterceptor {
    *http:ResponseErrorInterceptor;

    // The resource function inside a `ResponseErrorInterceptor` is only allowed 
    // to have the default method and path. The error occurred in the interceptor
    // execution can be accessed by the mandatory argument: `error`.
    isolated remote function interceptResponseError(error err) returns util:BadRequest {
        // In this case, all of the errors are sent as `400 BadRequest` responses with a customized
        // media type and body. You can also send different status code responses according to
        // the error type. Furthermore, you can also call `ctx.next()` if you want to continue the 
        // request flow after fixing the error.
        log:printError("Invalid Request: ", err);

        if err is model:InvalidResourceIdError {
            return self.generateErrorResponse(err.message(), err.detail().get("ErrorCode"), "Path.PaymentId");
        }
        if err is model:PayloadParseError {
            return self.generateErrorResponse(err.message(), err.detail().get("ErrorCode"), "Request");

        }
        if err is model:InvalidPayloadError {
            return self.generateErrorResponse(err.message(), err.detail().get("ErrorCode"), "Request.Payload");
        }

        return self.generateErrorResponse(err.message(), util:CODE_FIELD_INVALID, null);
    }

    private isolated function generateErrorResponse(string errorMessage, string errorCode, string? path)
    returns util:BadRequest => {
        mediaType: "application/org+json",
        body: {
            ErrorCode: errorCode,
            Message: errorMessage,
            Path: path
        }
    };

}
