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
import bfsi_payment_initiation_api.util;
import wso2bfsi/wso2.bfsi.demo.backend.model;

# Validates Creditor Account
public class CreditorAccountValidator {
    *IPayloadValidator;

    # Initializes the Creditor Account Validator
    #
    # + payload - Payload
    # + path - Path
    public isolated function init(anydata payload, string path) {
        self.payload = payload;
        self.path = path;
    }

    # Validates the Creditor Account
    #
    # + return - Returns an error if validation fails
    isolated function validate() returns ()|model:InvalidPayloadError {
        log:printInfo("Executing CreditorAccountValidator");

        if (self.payload == "") {
            return error("Payload is missing", ErrorCode = util:CODE_FIELD_MISSING);
        }
        if (self.path == "") {
            return error("Path is missing", ErrorCode = util:CODE_FIELD_MISSING);
        }
        if (self.path == "file-payments") {
            return ();
        }
        do {
            model:CreditorAccount|error creditorAccount = check extractCreditorAccount(self.payload, self.path);

            if (creditorAccount is error) {
                return error("Creditor Account is missing", ErrorCode = util:CODE_FIELD_MISSING);
            }

            if (creditorAccount.SchemeName == "") {
                return error("Creditor Account SchemeName is missing", ErrorCode = util:CODE_FIELD_MISSING);
            } else {
                if (creditorAccount.SchemeName != "UK.OBIE.IBAN" &&
                                    creditorAccount.SchemeName != "UK.OBIE.SortCodeAccountNumber") {
                    return error("Creditor Account SchemeName is invalid", ErrorCode = util:CODE_FIELD_INVALID);
                }
            }

            if (creditorAccount.Identification == "") {
                return error("Creditor Account Identification is missing", ErrorCode = util:CODE_FIELD_MISSING);
            } else {
                if (creditorAccount.Identification.length() > 256) {
                    return error("Creditor Account Identification is invalid", ErrorCode = util:CODE_FIELD_INVALID);
                }
            }
        } on fail var e {
            return error(e.message(), ErrorCode = util:CODE_FIELD_INVALID);
        }
        return ();
    }
}
