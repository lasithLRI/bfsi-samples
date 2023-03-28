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

# Response model for Payment Details retrieval endpoint
public type PaymentDetailsResponse record {|
    # Payment details response data
    PaymentDetailsResponseData Data;
    # Links relevant to the payload
    Links Links?;
    # Meta Data relevant to the payload
    Meta Meta?;
|};

# Data model for the payment details response.
public type PaymentDetailsResponseData record {|
    # Payment status details
    PaymentDetailsResponseDataPaymentStatus[] PaymentStatus?;
|};

# Payment status details.
public type PaymentDetailsResponseDataPaymentStatus record {|
    # Unique identifier for the transaction within an servicing institution. This identifier is both 
    # unique and immutable.
    @constraint:String {maxLength: 210, minLength: 1}
    string PaymentTransactionId;
    # Status of a transfe, as assigned by the transaction administrator.
    string Status;
    # Date and time at which the status was assigned to the transfer.All dates in the JSON payloads are
    # represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string StatusUpdateDateTime;
    # Payment status details as per underlying Payment Rail.
    PaymentDetailsResponseDataStatusDetail StatusDetail?;
|};

# Payment status details as per underlying Payment Rail.
public type PaymentDetailsResponseDataStatusDetail record {|
    # User community specific instrument.
    # Usage: This element is used to specify a local instrument, local clearing option and/or further
    # qualify the service or service level.
    string LocalInstrument?;
    # Status of a transfer, as assigned by the transaction administrator.
    @constraint:String {maxLength: 128, minLength: 1}
    string Status;
    # Reason Code provided for the status of a transfer.
    string StatusReason?;
    # Reason provided for the status of a transfer.
    @constraint:String {maxLength: 256, minLength: 1}
    string StatusReasonDescription?;
|};
