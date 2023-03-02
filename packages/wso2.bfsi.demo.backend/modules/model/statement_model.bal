// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import wso2.bfsi.demo.backend.util;

# Provides further details on a statement resource.
public type Statement record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # Unique identifier for the statement resource within an servicing institution. This identifier is both unique and immutable.
    readonly string StatementId;
    # Unique reference for the statement. This reference may be optionally populated if available.
    string StatementReference?;
    # Statement type, in a coded form.
    string Type;
    # Date and time at which the statement period starts.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string StartDateTime = util:getDateTime();
    # Date and time at which the statement period ends.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string EndDateTime = util:getFutureDateTime();
    # Date and time at which the resource was created.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string CreationDateTime = util:getPastDateTime();
    #Describes statement descriptions list
    string[] StatementDescription?;
    #Describes statement benefits list
    StatementBenefit[] StatementBenefit?;
    #Describes statement fees list
    StatementDetails[] StatementFee?;
    #Describes statement intrests list
    StatementDetails[] StatementInterest?;
    #Describes statement amount list
    BalanceAmount[] StatementAmount?;
    #Describes statement date time list
    StatementDateTime[] StatementDateTime?;
    #Describes statement rate list
    StatementRate[] StatementRate?;
    #Describes statement value list
    StatementValue[] StatementValue?;
};

# Set of elements used to provide details of a benefit or reward amount for the statement resource.
public type StatementBenefit record {
    # Benefit type, in a coded form.
    string Type;
    # Amount of money associated with the statement benefit type.
    Amount Amount;
};

# Set of elements used to provide details of a fee for the statement resource.
public type StatementDetails record {
    # Description that may be available for the statement fee.
    string Description?;
    # Indicates whether the amount is a credit or a debit. 
    # Usage: A zero amount is considered to be a credit amount.
    string CreditDebitIndicator;
    # Fee type, in a coded form.
    string Type;
    # Rate charged for Statement Fee (where it is charged in terms of a rate rather than an amount)
    int Rate?;
    # Description that may be available for the statement fee rate type.
    string RateType?;
    # How frequently the fee is applied to the Account.
    string Frequency?;
    # Amount of money associated with the statement fee type.
    Amount Amount;
};

# Set of elements used to provide details of a generic amount for the statement resource.
public type BalanceAmount record {
    # Indicates whether the amount is a credit or a debit. 
    # Usage: A zero amount is considered to be a credit amount.
    string CreditDebitIndicator;
    # Amount type, in a coded form.
    string Type;
    # Amount of money associated with the amount type.
    Amount Amount;
};

# Set of elements used to provide details of a generic date time for the statement resource.
public type StatementDateTime record {
    # Date and time associated with the date time type.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string DateTime = util:getDateTime();
    # Date time type, in a coded form.
    string Type;
};

# Set of elements used to provide details of a generic rate related to the statement resource.
public type StatementRate record {
    # Rate associated with the statement rate type.
    string Rate;
    # Statement rate type, in a coded form.
    string Type;
};

# Set of elements used to provide details of a generic number value related to the statement resource.
public type StatementValue record {
    # Value associated with the statement value type.
    string Value;
    # Statement value type, in a coded form.
    string Type;
};
