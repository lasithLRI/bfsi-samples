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
import ballerina/regex;
import ballerina/time;
import bfsi_payment_initiation_api.model;

const IPV4 = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
const IPV6 = "((([0-9a-fA-F]){1,4})\\:){7}([0-9a-fA-F]){1,4}";
const UUID = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

# Get Domestic Payment Initiation payload.
#
# + return - a domestic payment initiation payload.
public isolated function getDomesticPaymentInitiation() returns map<json> => {
    InstructionIdentification: "ACME412",
    EndToEndIdentification: "FRESCO.21302.GFX.20",
    InstructedAmount: {
        Amount: "165.88",
        Currency: "GBP"
    },
    CreditorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "08080021325698",
        Name: "ACME Inc",
        SecondaryIdentification: "0002"
    },
    RemittanceInformation: {
        Reference: "FRESCO-101",
        Unstructured: "Internal ops code 5120101"
    }
};

# Get Domestic scheduled Payment Initiation payload.
#
# + return - a domestic scheduled payment initiation payload.
public isolated function getDomesticScheduledPaymentInitiation() returns map<json> => {
    InstructionIdentification: "89f0a53a91ee47f6a383536f851d6b5a",
    RequestedExecutionDateTime: "2018-08-06T00:00:00+00:00",
    InstructedAmount: {
        Amount: "200.00",
        Currency: "GBP"
    },
    DebtorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "11280001234567",
        Name: "Andrea Frost"
    },
    CreditorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "08080021325698",
        Name: "Tom Kirkman"
    },
    RemittanceInformation: {
        Reference: "DSR-037",
        Unstructured: "Internal ops code 5120103"
    }
};

# Get Domestic standing order Payment Initiation payload.
#
# + return - a domestic standing order payment initiation payload.
public isolated function getDomesticStandingOrderPaymentInitiation() returns map<json> => {
    Frequency: "EvryDay",
    Reference: "Pocket money for Damien",
    FirstPaymentDateTime: "2023-06-06T06:06:06+00:00",
    FirstPaymentAmount: {
        Amount: "6.66",
        Currency: "GBP"
    },
    RecurringPaymentAmount: {
        Amount: "7.00",
        Currency: "GBP"
    },
    FinalPaymentDateTime: "2025-06-06T06:06:06+00:00",
    FinalPaymentAmount: {
        Amount: "7.00",
        Currency: "GBP"
    },
    DebtorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "11280001234567",
        Name: "Andrea Smith"
    },
    CreditorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "08080021325698",
        Name: "Bob Clements"
    }
};

# Get a file payment initiation payload.
#
# + return - a file payment initiation payload.
public isolated function getFilePaymentInitiation() returns map<json> => {
    FileType: "UK.OBIE.pain.001.001.08",
    FileHash: "m5ah/h1UjLvJYMxqAoZmj9dKdjZnsGNm+yMkJp/KuqQ",
    FileReference: "GB2OK238",
    NumberOfTransactions: "100",
    ControlSum: 3459.30
};

# Get a International payment initiation payload.
#
# + return - an international payment initiation payload.
public isolated function getInternationalPaymentInitiation() returns map<json> => {
    InstructionIdentification: "ACME412",
    EndToEndIdentification: "FRESCO.21302.GFX.20",
    InstructionPriority: "Normal",
    CurrencyOfTransfer: "USD",
    InstructedAmount: {
        Amount: "165.88",
        Currency: "GBP"
    },
    CreditorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "08080021325698",
        Name: "ACME Inc",
        SecondaryIdentification: "0002"
    },
    RemittanceInformation: {
        Reference: "FRESCO-101",
        Unstructured: "Internal ops code 5120101"
    },
    ExchangeRateInformation: {
        UnitCurrency: "GBP",
        RateType: "Actual"
    }
};

# Get the international scheduled payment initiation payload.
#
# + return - an international scheduled payment initiation payload.
public isolated function getInternatioanlScheduledPaymentInitiation() returns map<json> => {
    InstructionIdentification: "ACME412",
    EndToEndIdentification: "FRESCO.21302.GFX.20",
    RequestedExecutionDateTime: "2023-06-06T06:06:06+00:00",
    InstructedAmount: {
        Amount: "165.88",
        Currency: "USD"
    },
    CurrencyOfTransfer: "USD",
    CreditorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "08080021325698",
        Name: "ACME Inc",
        SecondaryIdentification: "0002"
    },
    RemittanceInformation: {
        Reference: "FRESCO-101",
        Unstructured: "Internal ops code 5120101"
    },
    ExchangeRateInformation: {
        UnitCurrency: "GBP",
        RateType: "Actual"
    }
};

# Get the international standing order payment initiation payload.
#
# + return - an international standing order payment initiation payload.
public isolated function getInternatioanlStandingOrderPaymentInitiation() returns map<json> => {
    Frequency: "EvryWorkgDay",
    FirstPaymentDateTime: "2023-06-06T06:06:06+00:00",
    FinalPaymentDateTime: "2025-06-06T06:06:06+00:00",
    DebtorAccount: {
        SchemeName: "UK.OBIE.SortCodeAccountNumber",
        Identification: "11280001234567",
        Name: "Andrea Frost"
    },
    CreditorAccount: {
        SchemeName: "UK.OBIE.IBAN",
        Identification: "DE89370400440532013000",
        Name: "Tom Kirkman"
    },
    InstructedAmount: {
        Amount: "20",
        Currency: "EUR"
    },
    CurrencyOfTransfer: "EUR"
};

# Exract creditor account from the payload.
#
# + payload - the payload
# + path - the path
# + return - the creditor account
public isolated function extractCreditorAccount(json payload, string path) returns model:CreditorAccount|error {

    if path.includes(DOMESTIC_PAYMENT) {
        model:DomesticPaymentInitiation initiation = check extractDomesticPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    }
    if path.includes(DOMESTIC_SCHEDULED_PAYMENT) {
        model:DomesticScheduledPaymentInitiation initiation = check extractDomesticScheduledPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    }
    if path.includes(DOMESTIC_STANDING_ORDER_PAYMENT) {
        model:DomesticStandingOrderInitiation initiation = check extractDomesticStandingOrderInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    }
    if path.includes(INTERNATIONAL_PAYMENT) {
        model:InternationalPaymentInitiation initiation = check extractInternationalPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    }
    if path.includes(INTERNATIONAL_SCHEDULED_PAYMENT) {
        model:InternationalScheduledPaymentInitiation initiation = check extractInternationalScheduledPaymentInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    }
    if path.includes(INTERNATIONAL_STANDING_ORDER_PAYMENT) {
        model:InternationalStandingOrderInitiation initiation = check extractInternationalStandingOrderInitiation(payload);
        return initiation.CreditorAccount.cloneWithType();

    }
    return error("Invalid path", ErrorCode = "UK.OBIE.Field.Invalid");
}

# Exract debtor account from the payload.
#
# + payload - the payload
# + path - the path
# + return - the creditor account
public isolated function extractDebtorAccount(json payload, string path) returns model:DebtorAccount|error|() {

    if path.includes(DOMESTIC_PAYMENT) {
        model:DomesticPaymentInitiation initiation = check extractDomesticPaymentInitiation(payload);
        if initiation.DebtorAccount is () {
            return ();
        }
        return initiation.DebtorAccount.ensureType();

    }
    if path.includes(DOMESTIC_SCHEDULED_PAYMENT) {
        model:DomesticScheduledPaymentInitiation initiation = check extractDomesticScheduledPaymentInitiation(payload);
        if initiation.DebtorAccount is () {
            return ();
        }
        return initiation.DebtorAccount.ensureType();

    }
    if path.includes(DOMESTIC_STANDING_ORDER_PAYMENT) {
        model:DomesticStandingOrderInitiation initiation = check extractDomesticStandingOrderInitiation(payload);
        if initiation.DebtorAccount is () {
            return ();
        }
        return initiation.DebtorAccount.ensureType();

    }
    if path.includes(INTERNATIONAL_PAYMENT) {
        model:InternationalPaymentInitiation initiation = check extractInternationalPaymentInitiation(payload);
        if initiation.DebtorAccount is () {
            return ();
        }
        return initiation.DebtorAccount.ensureType();

    }
    if path.includes(INTERNATIONAL_SCHEDULED_PAYMENT) {
        model:InternationalScheduledPaymentInitiation initiation = check extractInternationalScheduledPaymentInitiation(payload);
        if initiation.DebtorAccount is () {
            return ();
        }
        return initiation.DebtorAccount.ensureType();

    }
    if path.includes(INTERNATIONAL_STANDING_ORDER_PAYMENT) {
        model:InternationalStandingOrderInitiation initiation = check extractInternationalStandingOrderInitiation(payload);
        if initiation.DebtorAccount is () {
            return ();
        }
        return initiation.DebtorAccount.ensureType();

    }
    if path.includes(FILE_PAYMENT) {
        model:FilePaymentInitiation initiation = check extractFilePaymentInitiation(payload);
        if initiation.DebtorAccount is () {
            return ();
        }
        return initiation.DebtorAccount.ensureType();

    }
    return error("Invalid path", ErrorCode = "UK.OBIE.Field.Invalid");
}

# Exract Domestic Payment Initiation from the payload.
#
# + payload - the payload
# + return - the Domestic Payment Initiation
public isolated function extractDomesticPaymentInitiation(json payload)
    returns model:DomesticPaymentInitiation|error => 
                        (check payload.Data.Initiation).cloneWithType();


# Exract Domestic Scheduled Payment Initiation from the payload.
#
# + payload - the payload
# + return - the Domestic Scheduled Payment Initiation
public isolated function extractDomesticScheduledPaymentInitiation(json payload)
    returns model:DomesticScheduledPaymentInitiation|error => 
                        (check payload.Data.Initiation).cloneWithType();


# Exract Domestic Standing Order Payment Initiation from the payload.
#
# + payload - the payload
# + return - the Domestic Standing Order Payment Initiation
public isolated function extractDomesticStandingOrderInitiation(json payload)
    returns model:DomesticStandingOrderInitiation|error => 
                        (check payload.Data.Initiation).cloneWithType();


# Exract International Payment Initiation from the payload.
#
# + payload - the payload
# + return - the International Payment Initiation
public isolated function extractInternationalPaymentInitiation(json payload)
    returns model:InternationalPaymentInitiation|error => 
                        (check payload.Data.Initiation).cloneWithType();

# Exract International Scheduled Payment Initiation from the payload.
#
# + payload - the payload
# + return - the International Scheduled Payment Initiation
public isolated function extractInternationalScheduledPaymentInitiation(json payload)
    returns model:InternationalScheduledPaymentInitiation|error => 
                        (check payload.Data.Initiation).cloneWithType();

# Exract International Standing Order Payment Initiation from the payload.
#
# + payload - the payload
# + return - the International Standing Order Payment Initiation
public isolated function extractInternationalStandingOrderInitiation(json payload)
    returns model:InternationalStandingOrderInitiation|error => 
                        (check payload.Data.Initiation).cloneWithType();

# Exract File Payment Initiation from the payload.
#
# + payload - the payload
# + return - the File Payment Initiation
public isolated function extractFilePaymentInitiation(anydata payload) 
                                returns model:FilePaymentInitiation|error {
    model:FilePaymentRequest request = check payload.cloneWithType();
    model:FilePaymentData data = check request.Data.cloneWithType();
    return data.Initiation.cloneWithType();
}

# Validate x-fapi-auth-date header 
#
# + authDateHeader - x-fapi-auth-date Hedaer
# + return - Returns an error if the auth date header is future date
public isolated function validateAuthDateHeader(string? authDateHeader) returns error? {
    if authDateHeader ==  () || authDateHeader.length() == 0 {
        return;
    }

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

# Validate x-fapi-customer-ip-address header 
#
# + ipAddress - x-fapi-customer-ip-address Hedaer
# + return - Return an error if the ip address is invalid
public isolated function validateIpAddress(string? ipAddress) returns error? {
    if ipAddress ==  () || ipAddress.length() == 0 {
        return;
    }
    
    boolean isIpv4 = regex:matches(ipAddress, IPV4);
    boolean isIpv6 = regex:matches(ipAddress, IPV6);

    if isIpv4 || isIpv6 {
        return ();
    } else {
        return error("Found invalid ip address in headers");
    }
}

# Validate x-fapi-interaction-id header 
#
# + uuidHeader - x-fapi-interaction-id Hedaer
# + return - Return an error if the uuid is invalid
public isolated function validateUUID(string? uuidHeader) returns error? {
    if uuidHeader ==  () || uuidHeader.length() == 0 {
        return;
    }
    return regex:matches(uuidHeader, UUID) ? () : error("Found invalid UUID in headers");
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
    }
    if schemeName != "UK.OBIE.IBAN" && schemeName != "UK.OBIE.SortCodeAccountNumber" {
        return error("Creditor Account SchemeName is invalid", ErrorCode = CODE_FIELD_INVALID);
    }

    string identification = creditorAccount.Identification;
    if identification == "" {
        return error("Creditor Account Identification is missing", ErrorCode = CODE_FIELD_MISSING);
    }
    if identification.length() > 256 {
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
    }
    if schemeName != "UK.OBIE.IBAN" && schemeName != "UK.OBIE.SortCodeAccountNumber" {
        return error("Debtor Account SchemeName is invalid", ErrorCode = CODE_FIELD_INVALID);
    }

    string identification = debtorAccount.Identification;
    if identification == "" {
        return error("Debtor Account Identification is missing", ErrorCode = CODE_FIELD_MISSING);
    }
    if identification.length() > 256 {
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

# Validate whether the value is empty
# 
# + value - Value to be validated
# + return - Returns true if the value is empty
isolated function isEmptyValue(anydata value) returns boolean => 
        value == "" || value == null;
