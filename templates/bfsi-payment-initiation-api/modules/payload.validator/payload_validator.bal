// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import wso2bfsi/wso2.bfsi.demo.backend.model;

# This class is used in the `RequestInterceptor' to implement the `Chain of responsibility` pattern
public class PayloadValidator {
    private IPayloadValidator[] validators;

    # Initializes the `PayloadValidator` object
    public isolated function init() {
        self.validators = [];
    }

    # Adds a new validator to the chain
    # 
    # + validator - The validator to be added
    # + return - The updated `PayloadValidator` object
    public isolated function add(IPayloadValidator validator) returns PayloadValidator {
        self.validators.push(validator);
        return self;
    }

    # Validates the payload
    # 
    # + return - An `error` if the validation fails
    public isolated function validate() returns ()|model:InvalidPayloadError{
        foreach IPayloadValidator validator in self.validators {
            ()|model:InvalidPayloadError result = validator.validate();
            if result is error {
                return result;
            }
        }
        return ();
    }
}
