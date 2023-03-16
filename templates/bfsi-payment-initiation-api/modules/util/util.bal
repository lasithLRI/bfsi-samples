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
import ballerina/time;
import ballerina/regex;
import bfsi_payment_initiation_api.model;

const ipv4 = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
const ipv6 = "((([0-9a-fA-F]){1,4})\\:){7}([0-9a-fA-F]){1,4}";
const uuid = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

# Get Domestic Payment Initiation payload.
#
# + return - a domestic payment initiation payload.
public isolated function getDomesticPaymentInitiation() returns json => {
    "InstructionIdentification": "ACME412",
    "EndToEndIdentification": "FRESCO.21302.GFX.20",
    "InstructedAmount": {
        "Amount": "165.88",
        "Currency": "GBP"
    },
    "CreditorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "08080021325698",
        "Name": "ACME Inc",
        "SecondaryIdentification": "0002"
    },
    "RemittanceInformation": {
        "Reference": "FRESCO-101",
        "Unstructured": "Internal ops code 5120101"
    }
};

# Get Domestic scheduled Payment Initiation payload.
#
# + return - a domestic scheduled payment initiation payload.
public isolated function getDomesticScheduledPaymentInitiation() returns json => {
    "InstructionIdentification": "89f0a53a91ee47f6a383536f851d6b5a",
    "RequestedExecutionDateTime": "2018-08-06T00:00:00+00:00",
    "InstructedAmount": {
        "Amount": "200.00",
        "Currency": "GBP"
    },
    "DebtorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "11280001234567",
        "Name": "Andrea Frost"
    },
    "CreditorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "08080021325698",
        "Name": "Tom Kirkman"
    },
    "RemittanceInformation": {
        "Reference": "DSR-037",
        "Unstructured": "Internal ops code 5120103"
    }
};

# Get Domestic standing order Payment Initiation payload.
#
# + return - a domestic standing order payment initiation payload.
public isolated function getDomesticStandingOrderPaymentInitiation() returns json => {
    "Frequency": "EvryDay",
    "Reference": "Pocket money for Damien",
    "FirstPaymentDateTime": "2023-06-06T06:06:06+00:00",
    "FirstPaymentAmount": {
        "Amount": "6.66",
        "Currency": "GBP"
    },
    "RecurringPaymentAmount": {
        "Amount": "7.00",
        "Currency": "GBP"
    },
    "FinalPaymentDateTime": "2025-06-06T06:06:06+00:00",
    "FinalPaymentAmount": {
        "Amount": "7.00",
        "Currency": "GBP"
    },
    "DebtorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "11280001234567",
        "Name": "Andrea Smith"
    },
    "CreditorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "08080021325698",
        "Name": "Bob Clements"
    }
};

# Get a file payment initiation payload.
#
# + return - a file payment initiation payload.
public isolated function getFilePaymentInitiation() returns json => {
    "FileType": "UK.OBIE.pain.001.001.08",
    "FileHash": "m5ah/h1UjLvJYMxqAoZmj9dKdjZnsGNm+yMkJp/KuqQ",
    "FileReference": "GB2OK238",
    "NumberOfTransactions": "100",
    "ControlSum": 3459.30
};

# Get a International payment initiation payload.
#
# + return - an international payment initiation payload.
public isolated function getInternationalPaymentInitiation() returns json => {
    "InstructionIdentification": "ACME412",
    "EndToEndIdentification": "FRESCO.21302.GFX.20",
    "InstructionPriority": "Normal",
    "CurrencyOfTransfer": "USD",
    "InstructedAmount": {
        "Amount": "165.88",
        "Currency": "GBP"
    },
    "CreditorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "08080021325698",
        "Name": "ACME Inc",
        "SecondaryIdentification": "0002"
    },
    "RemittanceInformation": {
        "Reference": "FRESCO-101",
        "Unstructured": "Internal ops code 5120101"
    },
    "ExchangeRateInformation": {
        "UnitCurrency": "GBP",
        "RateType": "Actual"
    }
};

# Get the international scheduled payment initiation payload.
#
# + return - an international scheduled payment initiation payload.
public isolated function getInternatioanlScheduledPaymentInitiation() returns json => {
    "InstructionIdentification": "ACME412",
    "EndToEndIdentification": "FRESCO.21302.GFX.20",
    "RequestedExecutionDateTime": "2023-06-06T06:06:06+00:00",
    "InstructedAmount": {
        "Amount": "165.88",
        "Currency": "USD"
    },
    "CurrencyOfTransfer": "USD",
    "CreditorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "08080021325698",
        "Name": "ACME Inc",
        "SecondaryIdentification": "0002"
    },
    "RemittanceInformation": {
        "Reference": "FRESCO-101",
        "Unstructured": "Internal ops code 5120101"
    },
    "ExchangeRateInformation": {
        "UnitCurrency": "GBP",
        "RateType": "Actual"
    }
};

# Get the international standing order payment initiation payload.
#
# + return - an international standing order payment initiation payload.
public isolated function getInternatioanlStandingOrderPaymentInitiation() returns json => {
    "Frequency": "EvryWorkgDay",
    "FirstPaymentDateTime": "2023-06-06T06:06:06+00:00",
    "FinalPaymentDateTime": "2025-06-06T06:06:06+00:00",
    "DebtorAccount": {
        "SchemeName": "UK.OBIE.SortCodeAccountNumber",
        "Identification": "11280001234567",
        "Name": "Andrea Frost"
    },
    "CreditorAccount": {
        "SchemeName": "UK.OBIE.IBAN",
        "Identification": "DE89370400440532013000",
        "Name": "Tom Kirkman"
    },
    "InstructedAmount": {
        "Amount": "20",
        "Currency": "EUR"
    },
    "CurrencyOfTransfer": "EUR"
};

# Exract creditor account from the payload.
#
# + payload - the payload
# + path - the path
# + return - the creditor account
public isolated function extractCreditorAccount(anydata payload, string path) returns model:CreditorAccount|error {

    if (path.includes("domestic-payment")) {
        model:DomesticPaymentInitiation initiation = check extractDomesticPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    } else if (path.includes("domestic-scheduled-payment")) {
        model:DomesticScheduledPaymentInitiation initiation = check extractDomesticScheduledPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    } else if (path.includes("domestic-standing-order")) {
        model:DomesticStandingOrderInitiation initiation = check extractDomesticStandingOrderInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    } else if (path.includes("international-payment")) {
        model:InternationalPaymentInitiation initiation = check extractInternationalPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    } else if (path.includes("international-scheduled-payment")) {
        model:InternationalScheduledPaymentInitiation initiation = check extractInternationalScheduledPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    } else if (path.includes("international-standing-orders")) {
        model:InternationalStandingOrderInitiation initiation = check extractInternationalStandingOrderInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    } else {
        return error("Invalid path", ErrorCode = "UK.OBIE.Field.Invalid");
    }
}

# Exract debtor account from the payload.
#
# + payload - the payload
# + path - the path
# + return - the creditor account
public isolated function extractDebtorAccount(anydata payload, string path) returns model:DebtorAccount|error|() {

    if (path.includes("domestic-payment")) {
        model:DomesticPaymentInitiation initiation = check extractDomesticPaymentInitiation(payload);
        if (initiation.DebtorAccount is ()) {
            return ();
        }
        return initiation.DebtorAccount.cloneWithType();

    } else if (path.includes("domestic-scheduled-payment")) {
        model:DomesticScheduledPaymentInitiation initiation = check extractDomesticScheduledPaymentInitiation(payload);
        if (initiation.DebtorAccount is ()) {
            return ();
        }
        return initiation.DebtorAccount.cloneWithType();

    } else if (path.includes("domestic-standing-order")) {
        model:DomesticStandingOrderInitiation initiation = check extractDomesticStandingOrderInitiation(payload);
        if (initiation.DebtorAccount is ()) {
            return ();
        }
        return initiation.DebtorAccount.cloneWithType();

    } else if (path.includes("international-payment")) {
        model:InternationalPaymentInitiation initiation = check extractInternationalPaymentInitiation(payload);
        if (initiation.DebtorAccount is ()) {
            return ();
        }
        return initiation.DebtorAccount.cloneWithType();

    } else if (path.includes("international-scheduled-payment")) {
        model:InternationalScheduledPaymentInitiation initiation = check extractInternationalScheduledPaymentInitiation(payload);
        if (initiation.DebtorAccount is ()) {
            return ();
        }
        return initiation.DebtorAccount.cloneWithType();

    } else if (path.includes("international-standing-order")) {
        model:InternationalStandingOrderInitiation initiation = check extractInternationalStandingOrderInitiation(payload);
        if (initiation.DebtorAccount is ()) {
            return ();
        }
        return initiation.DebtorAccount.cloneWithType();

    } else if (path.includes("file-payment")) {
        model:FilePaymentInitiation initiation = check extractFilePaymentInitiation(payload);
        if (initiation.DebtorAccount is ()) {
            return ();
        }
        return initiation.DebtorAccount.cloneWithType();

    } else {
        return error("Invalid path", ErrorCode = "UK.OBIE.Field.Invalid");
    }
}

# Exract Domestic Payment Initiation from the payload.
#
# + payload - the payload
# + return - the Domestic Payment Initiation
public isolated function extractDomesticPaymentInitiation(anydata payload) returns model:DomesticPaymentInitiation|error {
    model:DomesticPaymentRequest request = check payload.cloneWithType();
    model:DomesticPaymentRequestData data = check request.Data.cloneWithType();
    return check data.Initiation.cloneWithType();
}

# Exract Domestic Scheduled Payment Initiation from the payload.
#
# + payload - the payload
# + return - the Domestic Scheduled Payment Initiation
public isolated function extractDomesticScheduledPaymentInitiation(anydata payload) returns model:DomesticScheduledPaymentInitiation|error {
    model:DomesticScheduledPaymentRequest request = check payload.cloneWithType();
    model:DomesticScheduledPaymentData data = check request.Data.cloneWithType();
    return data.Initiation.cloneWithType();
}

# Exract Domestic Standing Order Payment Initiation from the payload.
#
# + payload - the payload
# + return - the Domestic Standing Order Payment Initiation
public isolated function extractDomesticStandingOrderInitiation(anydata payload) returns model:DomesticStandingOrderInitiation|error {
    model:DomesticStandingOrderRequest request = check payload.cloneWithType();
    model:DomesticStandingOrderData data = check request.Data.cloneWithType();
    return check data.Initiation.cloneWithType();
}

# Exract International Payment Initiation from the payload.
#
# + payload - the payload
# + return - the International Payment Initiation
public isolated function extractInternationalPaymentInitiation(anydata payload) returns model:InternationalPaymentInitiation|error {
    model:InternationalPaymentRequest request = check payload.cloneWithType();
    model:InternationalPaymentData data = check request.Data.cloneWithType();
    return data.Initiation.cloneWithType();
}

# Exract International Scheduled Payment Initiation from the payload.
#
# + payload - the payload
# + return - the International Scheduled Payment Initiation
public isolated function extractInternationalScheduledPaymentInitiation(anydata payload) returns model:InternationalScheduledPaymentInitiation|error {
    model:InternationalScheduledPaymentRequest request = check payload.cloneWithType();
    model:InternationalScheduledPaymentData data = check request.Data.cloneWithType();
    return data.Initiation.cloneWithType();
}

# Exract International Standing Order Payment Initiation from the payload.
#
# + payload - the payload
# + return - the International Standing Order Payment Initiation
public isolated function extractInternationalStandingOrderInitiation(anydata payload) returns model:InternationalStandingOrderInitiation|error {
    model:InternationalStandingOrderRequest request = check payload.cloneWithType();
    model:InternationalStandingOrderData data = check request.Data.cloneWithType();
    return data.Initiation.cloneWithType();
}

# Exract File Payment Initiation from the payload.
#
# + payload - the payload
# + return - the File Payment Initiation
public isolated function extractFilePaymentInitiation(anydata payload) returns model:FilePaymentInitiation|error {
    model:FilePaymentRequest request = check payload.cloneWithType();
    model:FilePaymentData data = check request.Data.cloneWithType();
    return data.Initiation.cloneWithType();
}

# Validate x-fapi-auth-date header 
# 
# + authDateHeader - x-fapi-auth-date Hedaer
# + return - error? 
public isolated function validateAuthDateHeader(string? authDateHeader) returns error? {
        if !(authDateHeader is ()) && authDateHeader != "" {
            time:Utc currentTime = time:utcNow();
            time:Utc timeInHeader;
            do {
                timeInHeader = check time:utcFromString(authDateHeader);
            } on fail var e {
                return error(e.message());
            }
            time:Seconds seconds = time:utcDiffSeconds(currentTime, timeInHeader);

            if seconds > 0d {
                return ();
            } else {
                return error("Invalid Date found in the header");
            }
        }
    }

    # Validate x-fapi-customer-ip-address header 
# 
# + ipAddress - x-fapi-customer-ip-address Hedaer
# + return - error? 
    public isolated function validateIpAddress(string? ipAddress) returns error? {
        if !(ipAddress is ()) && ipAddress != "" {
            boolean isIpv4 = regex:matches(ipAddress, ipv4);
            boolean isIpv6 = regex:matches(ipAddress, ipv6);

            if isIpv4 || isIpv6 {
                return ();
            } else {
                return error("Found invalid ip address in headers");
            }
        }
    }

    # Validate x-fapi-interaction-id header 
    # 
    # + uuidHeader - x-fapi-interaction-id Hedaer
    # + return - error? 
    public isolated function validateUUID(string? uuidHeader) returns error? {
        if !(uuidHeader is ()) && uuidHeader != "" {
            boolean isUuid = regex:matches(uuidHeader, uuid);
            return isUuid ? () : error("Found invalid UUID in headers");
        }
    }

    # Validates the payload
    #
    # + payload - Payload to be validated
    # + return - Returns error if validation fails
    public isolated function validatePayload(anydata payload) returns model:InvalidPayloadError? {
        log:printInfo("Executing PaymentRequestBodyValidator");

        if payload == "" {
            return error("Payload is empty", ErrorCode = CODE_RESOURCE_INVALID_FORMAT);
        }
        if payload is json {
            if payload.Data == "" || payload.Data == null {
                return error("Request Payload is not in correct JSON format", ErrorCode = CODE_RESOURCE_INVALID_FORMAT);
            }

            if payload.Data is json {
                if payload.Data.Initiation is json {
                    return;
                } else {
                    return error("Request Payload is not in correct JSON format", ErrorCode = CODE_RESOURCE_INVALID_FORMAT);
                }
            } else {
                return error("Request Payload is not in correct JSON format", ErrorCode = CODE_RESOURCE_INVALID_FORMAT);
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

        string schemeName = creditorAccount.SchemeName;
        if schemeName == "" {
            return error("Creditor Account SchemeName is missing", ErrorCode = CODE_FIELD_MISSING);
        } else if schemeName != "UK.OBIE.IBAN" && schemeName != "UK.OBIE.SortCodeAccountNumber" {
            return error("Creditor Account SchemeName is invalid", ErrorCode = CODE_FIELD_INVALID);
        }

        string identification = creditorAccount.Identification;
        if identification == "" {
            return error("Creditor Account Identification is missing", ErrorCode = CODE_FIELD_MISSING);
        } else if identification.length() > 256 {
            return error("Creditor Account Identification is invalid", ErrorCode = CODE_FIELD_INVALID);
        }
    }

    # Validates the Debtor Account
    #
    # + debtorAccount - Debtor Account to be validated
    # + return - Returns error if validation fails
    public isolated function validateDebtorAccount(model:DebtorAccount debtorAccount)
                            returns model:InvalidPayloadError? {
        log:printInfo("Executing DebtorAccountValidator");

        string schemeName = debtorAccount.SchemeName;
        if schemeName == "" {
            return error("Debtor Account SchemeName is missing", ErrorCode = CODE_FIELD_MISSING);
        } else if schemeName != "UK.OBIE.IBAN" && schemeName != "UK.OBIE.SortCodeAccountNumber" {
            return error("Debtor Account SchemeName is invalid", ErrorCode = CODE_FIELD_INVALID);
        }

        string identification = debtorAccount.Identification;
        if identification == "" {
            return error("Debtor Account Identification is missing", ErrorCode = CODE_FIELD_MISSING);
        } else if identification.length() > 256 {
            return error("Debtor Account Identification is invalid", ErrorCode = CODE_FIELD_INVALID);
        }
    }

# Represents a subtype of BAD_REQUEST error
public type BadRequest record {|
    *http:BadRequest;

    # Open Banking error format
    record {
        # Low level texual error code
        string ErrorCode;
        # Description of the error occured
        string Message;
        # Recommended but optional reference to the JSON Path of the field with error
        string Path?;
        # URL to help remediate the problem, or provide more information, or to API Reference, or help etc
        string Url?;
    } body;
|};
