// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/log;
import wso2bfsi/wso2.bfsi.demo.backend.model;

# Validates Payment Type
public class PaymentRequestBodyValidator {
    *IPayloadValidator;

    # Initiates the PaymentRequestBodyValidator
    # 
    # + payload - Payload to be validated
    # + path - Path to be validated
    public isolated function init(anydata payload, string path) {
        self.payload = payload;
        self.path = path;
    }

    # Validates the payload
    # 
    # + return - Returns error if validation fails
    isolated function validate() returns ()|model:InvalidPayloadError {
        log:printInfo("Executing PaymentRequestBodyValidator");

        if (self.payload == "") {
            return error("Payload is empty", ErrorCode = "UK.OBIE.Resource.InvalidFormat");
        }
        if self.payload is json {
            json payload = <json>self.payload;
             if (payload.Data == "" || payload.Data == null) {
                return error("Request Payload is not in correct JSON format", ErrorCode = "UK.OBIE.Resource.InvalidFormat");
            } 
            
            if (payload.Data is json) {
                if (payload.Data.Initiation is json) {
                    return ();
                } else {
                    return error("Request Payload is not in correct JSON format", ErrorCode = "UK.OBIE.Resource.InvalidFormat");
                }
            } else {
                return  error("Request Payload is not in correct JSON format", ErrorCode = "UK.OBIE.Resource.InvalidFormat");
            }
        }
    
    }
}
