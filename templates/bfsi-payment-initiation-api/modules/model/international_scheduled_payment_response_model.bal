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

# Represents an international scheduled payment response payload.
public type InternationalScheduledPaymentResponse record {|
    # Data object International Scheduled Payment Response
    InternationalScheduledResponseData Data;
    # Links relevant to the payload
    Links Links?;
    # Meta Data relevant to the payload
    Meta Meta?;
|};

# Represents the data of an international scheduled payment response.
public type InternationalScheduledResponseData record {|
    # OB: Unique identification as assigned by the bank to uniquely identify the international scheduled
    # payment resource.
    @constraint:String {maxLength: 40, minLength: 1}
    string InternationalScheduledPaymentId;
    # OB: Unique identification as assigned by the bank to uniquely identify the consent resource.
    @constraint:String {maxLength: 128, minLength: 1}
    string ConsentId;
    # Date and time at which the message was created.All dates in the JSON payloads are represented in 
    # ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string CreationDateTime = getPastDateTime();
    # Specifies the status of the payment order resource.
    string Status;
    # Date and time at which the resource status was updated.All dates in the JSON payloads are represented
    # in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string StatusUpdateDateTime = getPastDateTime();
    # Expected execution date and time for the payment resource.All dates in the JSON payloads are 
    # represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ExpectedExecutionDateTime?;
    # Expected settlement date and time for the payment resource.All dates in the JSON payloads are 
    # represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ExpectedSettlementDateTime?;
    #
    DataRefund Refund?;
    #
    DataCharges[] Charges?;
    # Further detailed information on the exchange rate that has been used in the payment transaction.
    ExchangeRateInformation ExchangeRateInformation?;
    # The Initiation payload is sent by the initiating party to the bank. It is used to request 
    # movement of funds from the debtor account to a creditor for a single scheduled international payment.
    InternationalScheduledPaymentInitiation Initiation;
    # The multiple authorisation flow response from the bank.
    MultiAuthorisation MultiAuthorisation?;
    # ^ Only incuded in the response if `Data. ReadRefundAccount` is set to `Yes` in the consent.
    CreditorAccount Debtor?;
|};
