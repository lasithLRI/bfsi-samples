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
import bfsi_payment_initiation_api.model;
import bfsi_payment_initiation_api.util;

# This class is used in the `RequestInterceptor' to implement the`Chain of responsibility` pattern
public isolated class PayloadValidator {

    # Validates the payload
    #
    # + payload - Payload to be validated
    # + return - Returns error if validation fails
    public isolated function validatePayload(anydata payload) returns model:InvalidPayloadError? {
        log:printInfo("Executing PaymentRequestBodyValidator");

        if payload == "" {
            return error("Payload is empty", ErrorCode = util:CODE_RESOURCE_INVALID_FORMAT);
        }
        if payload is json {
            if payload.Data == "" || payload.Data == null {
                return error("Request Payload is not in correct JSON format", ErrorCode = util:CODE_RESOURCE_INVALID_FORMAT);
            }

            if payload.Data is json {
                if payload.Data.Initiation is json {
                    return;
                } else {
                    return error("Request Payload is not in correct JSON format", ErrorCode = util:CODE_RESOURCE_INVALID_FORMAT);
                }
            } else {
                return error("Request Payload is not in correct JSON format", ErrorCode = util:CODE_RESOURCE_INVALID_FORMAT);
            }
        }

    }

    # Validates the Creditor Account
    #
    # + creditorAccount - Creditor Account to be validated
    # + return - Returns an error if validation fails
    public isolated function validateCreditorAccount(model:CreditorAccount creditorAccount) 
                            returns model:InvalidPayloadError? {
        log:printInfo("Executing CreditorAccountValidator");

        if creditorAccount.SchemeName == "" {
            return error("Creditor Account SchemeName is missing", ErrorCode = util:CODE_FIELD_MISSING);
        } else if creditorAccount.SchemeName != "UK.OBIE.IBAN" &&
                                creditorAccount.SchemeName != "UK.OBIE.SortCodeAccountNumber" {
            return error("Creditor Account SchemeName is invalid", ErrorCode = util:CODE_FIELD_INVALID);
        }

        if creditorAccount.Identification == "" {
            return error("Creditor Account Identification is missing", ErrorCode = util:CODE_FIELD_MISSING);
        } else if creditorAccount.Identification.length() > 256 {
            return error("Creditor Account Identification is invalid", ErrorCode = util:CODE_FIELD_INVALID);
        }
    }

    # Validates the Debtor Account
    #
    # + debtorAccount - Debtor Account to be validated
    # + return - Returns error if validation fails
    public isolated function validateDebtorAccount(model:DebtorAccount debtorAccount) 
                            returns model:InvalidPayloadError? {
        log:printInfo("Executing DebtorAccountValidator");
        
        if debtorAccount.SchemeName == "" {
            return error("Debtor Account SchemeName is missing", ErrorCode = util:CODE_FIELD_MISSING);
        } else if debtorAccount.SchemeName != "UK.OBIE.IBAN" && debtorAccount.SchemeName != "UK.OBIE.SortCodeAccountNumber" {
            return error("Debtor Account SchemeName is invalid", ErrorCode = util:CODE_FIELD_INVALID);
        }

        if debtorAccount.Identification == "" {
            return error("Debtor Account Identification is missing", ErrorCode = util:CODE_FIELD_MISSING);
        } else if debtorAccount.Identification.length() > 256 {
            return error("Debtor Account Identification is invalid", ErrorCode = util:CODE_FIELD_INVALID);
        }
    }
}
