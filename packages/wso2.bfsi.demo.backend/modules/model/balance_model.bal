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

# Set of elements used to define the balance details.
public type Balance record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # A unique and immutable identifier used to identify the balance resource. This identifier has no meaning to the account owner.
    readonly string BalanceId;
    # Indicates whether the balance is a credit or a debit balance. 
    # Usage: A zero balance is considered to be a credit balance.
    string CreditDebitIndicator;
    # Balance type, in a coded form.
    string Type;
    # Indicates the date (and time) of the balance.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string DateTime = util:getDateTime();
    # Amount of money of the cash balance.
    Amount Amount?;
    # Set of elements used to provide details on the credit line.
    Creditline[] CreditLine?;
};

# Amount of money of the cash balance.
public type Amount record {
    # A number of monetary units specified in an active currency where the unit of currency is explicit and compliant with ISO 4217.
    string Amount = util:getRandomAmount();
    # A code allocated to a currency by a Maintenance Agency under an international identification scheme, as described in the latest edition of the international standard ISO 4217 (Codes for the representation of currencies and funds).
    string Currency = "USD";
};

# Set of elements used to provide details on the credit line.
public type Creditline record {
    # Indicates whether or not the credit line is included in the balance of the account.
    # Usage: If not present, credit line is not included in the balance amount of the account.
    boolean Included;
    # Limit type, in a coded form.
    string Type?;
    # Amount of money of the credit line.
    Amount Amount?;
};
