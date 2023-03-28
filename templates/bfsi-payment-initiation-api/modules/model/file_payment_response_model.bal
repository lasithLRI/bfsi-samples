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

# Set of elements used to define the file payment response.
public type FilePaymentResponse record {|
    # Data object File Payment Response
    FilePaymentsResponseData Data;
    # Links relevant to the payload
    Links Links?;
    # Meta Data relevant to the payload
    Meta Meta?;
|};

# Represents data of the file payment response.
public type FilePaymentsResponseData record {|
    # OB: Unique identification as assigned by the bank to uniquely identify the file payment resource.
    @constraint:String {maxLength: 40, minLength: 1}
    string FilePaymentId;
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
    # Set of elements used to provide details of a charge for the payment initiation.
    DataCharges[] Charges?;
    # The Initiation payload is sent by the initiating party to the bank. It is used to request movement
    # of funds using a payment file.
    FilePaymentInitiation Initiation;
    # The multiple authorisation flow response from the bank.
    MultiAuthorisation MultiAuthorisation?;
    # ^ Only incuded in the response if `Data. ReadRefundAccount` is set to `Yes` in the consent.
    CreditorAccount Debtor?;
|};
