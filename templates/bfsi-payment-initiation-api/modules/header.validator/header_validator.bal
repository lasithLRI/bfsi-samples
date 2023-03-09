// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

# This class is used in the `RequestInterceptor' to implement the`Chain of responsibility` pattern
public class HeaderValidator {
    private IHeaderValidator[] validators;

    # This method is used to initialize the `HeaderValidator` object
    public isolated function init() {
        self.validators = [];
    }

    # This method is used to add a new validator to the chain
    #
    # + validator - The validator to be added
    # + return - Returns the `HeaderValidator` object
    public isolated function add(IHeaderValidator validator) returns HeaderValidator {
        self.validators.push(validator);
        return self;
    }

    # This method is used to validate the headers
    #
    # + return - Returns an error if the validation fails
    public isolated function validate() returns error? {
        foreach IHeaderValidator validator in self.validators {
            ()|error? result = validator.validate();
            if result is error {
                return result;
            }
        }
        return ();
    }
}
