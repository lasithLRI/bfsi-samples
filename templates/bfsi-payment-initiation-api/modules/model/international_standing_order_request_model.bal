// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/constraint;

# Represents an international standing order request payload.
public type InternationalStandingOrderRequest record {|
    # Represents the data of an international standing order request.
    InternationalStandingOrderData Data;
    # The Risk section is sent by the initiating party to the bank.
    # It is used to specify additional details for risk scoring for Payments.
    Risk Risk;
|};

# Represents the data of an international standing order request.
public type InternationalStandingOrderData record {|
    # OB: Unique identification as assigned by the bank to uniquely identify the consent resource.
    @constraint:String {maxLength: 128, minLength: 1}
    string ConsentId;
    # The Initiation payload is sent by the initiating party to the bank. It is used to request movement
    # of funds from the debtor account to a creditor for an international standing order.
    InternationalStandingOrderInitiation Initiation;
|};
