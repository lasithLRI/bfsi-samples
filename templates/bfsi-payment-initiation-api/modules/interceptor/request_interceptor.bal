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
import bfsi_payment_initiation_api.util;
import bfsi_payment_initiation_api.header.validator;

# Request interceptor to pre-process every API interaction.
public service class RequestInterceptor {
    *http:RequestInterceptor;

    // This will return a validation error if you do not send valid header values. 
    // Then, the execution will jump to the nearest `RequestErrorInterceptor`.
    isolated resource function 'default [string... path](http:RequestContext ctx, @http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id)
            returns anydata|http:NextService|error {

        validator:HeaderValidator headerValidator = new ();
        ()|error? headerValidatorResult = headerValidator
            .add(new validator:UUIDValidator('x\-fapi\-interaction\-id))
            .add(new validator:IpAddressValidator('x\-fapi\-customer\-ip\-address))
            .add(new validator:AuthDateVallidator('x\-fapi\-auth\-date))
            .validate();

        if (headerValidatorResult is error) {
            return error(headerValidatorResult.message(), ErrorCode = util:CODE_HEADER_INVALID);
        }

        return ctx.next();
    }
}
