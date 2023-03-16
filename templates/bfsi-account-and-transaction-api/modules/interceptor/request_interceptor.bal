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

import bfsi_account_and_transaction_api.validator;

# Request interceptor to pre-process every API interaction.
public isolated service class RequestInterceptor {
    *http:RequestInterceptor;

    // This will return a validation error if you do not send valid header values. 
    // Then, the execution will jump to the nearest `RequestErrorInterceptor`.
    resource isolated function 'default [string... path](http:RequestContext ctx,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id)
            returns anydata|http:Response|http:StatusCodeResponse|http:NextService|error? {
        check validator:validateUUIDHeader(x\-fapi\-interaction\-id);
        check validator:validateIpAddressHeader(x\-fapi\-customer\-ip\-address);

        return ctx.next();
    }
}
