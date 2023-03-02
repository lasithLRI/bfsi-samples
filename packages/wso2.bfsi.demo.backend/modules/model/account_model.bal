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

# Set of elements used to define the account details.
public type Account record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # Specifies the status of account resource in code form.
    string Status?;
    # Date and time at which the resource status was updated.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string StatusUpdateDateTime = util:getPastDateTime();
    # Identification of the currency in which the account is held. 
    # Usage: Currency should only be used in case one and the same account number covers several currencies
    # and the initiating party needs to identify which currency needs to be used for settlement on the account.
    string Currency = "USD";
    # Specifies the type of account (personal or business).
    string AccountType?;
    # Specifies the sub type of account (product family group).
    string AccountSubType?;
    # Specifies the description of the account type.
    string Description?;
    # The nickname of the account, assigned by the account owner in order to provide an additional means of identification of the account.
    string Nickname?;
    # Date on which the account and related basic services are effectively operational for the account owner.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string OpeningDate = util:getPastDateTime();
    # Maturity date of the account.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string MaturityDate = util:getFutureDateTime();
    # Specifies the switch status for the account, in a coded form.
    string SwitchStatus?;
    # Provides the details to identify an account.
    CreditorAccount[] Account?;
};

#Represent a generic response record with hateoas.
public type HateoasResponse record {
    # Response data
    anydata Data;
    # Links relevant to the payload
    Links Links?;
    # Meta Data relevant to the payload
    Meta Meta?;
};
