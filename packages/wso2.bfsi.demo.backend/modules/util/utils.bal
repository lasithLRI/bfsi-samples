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
import ballerina/random;
import ballerina/time;
import ballerina/uuid;

# Method to generate the current date and time.
#
# + return - current date and time.
public isolated function getDateTime() returns string => time:utcToString(time:utcNow());

# Method to generate a future date and time.
#
# + return - future date and time.
public isolated function getFutureDateTime() returns string =>
    time:utcToString(time:utcAddSeconds(time:utcNow(), generateRandomSeconds()));

# Method to generate a past date and time.
#
# + return - past date and time.
public isolated function getPastDateTime() returns string =>
    time:utcToString(time:utcAddSeconds(time:utcNow(), generateRandomSeconds(true)));

# Method to generate a random time in seconds.
#
# + isNegative - if true, return a negative random time in seconds.
# + return - a random time in seconds.
isolated function generateRandomSeconds(boolean isNegative = false) returns time:Seconds {
    int randomSeconds;
    do {
        randomSeconds = check random:createIntInRange(86400, 864000);
    } on fail var e {
        log:printDebug("failed to generate a random integer. Caused by, ", e);
        randomSeconds = 86400;
    }

    return isNegative ? <time:Seconds>(randomSeconds * -1) : <time:Seconds>randomSeconds;
}

# Method to generate a random amount.
#
# + return - a random amount.
public isolated function getRandomAmount() returns string => (random:createDecimal() * 1000).toFixedString(2);

# Method to generate a random UUID.
#
# + return - a random UUID.
public isolated function getRandomId() returns string => uuid:createType4AsString();

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
public isolated function getInternationalScheduledPaymentInitiation() returns json => {
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
public isolated function getInternationalStandingOrderPaymentInitiation() returns json => {
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
