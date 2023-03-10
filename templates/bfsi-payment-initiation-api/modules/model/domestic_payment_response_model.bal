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

# Represents the payload of the domestic payment response.
public type DomesticPaymentResponse record {
    # Data object Domestic Payment Response
    DomesticPaymentResponseData Data;
    # Links relevant to the payload
    Links Links?;
    # Meta Data relevant to the payload
    Meta Meta?;
};

# Represents the data inside the payload of the domestic payment response.
public type DomesticPaymentResponseData record {
    # OB: Unique identification as assigned by the bank to uniquely identify the domestic payment resource.
    @constraint:String {maxLength: 40, minLength: 1}
    string DomesticPaymentId;
    # OB: Unique identification as assigned by the bank to uniquely identify the consent resource.
    @constraint:String {maxLength: 128, minLength: 1}
    string ConsentId;
    # Date and time at which the message was created.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string CreationDateTime = getPastDateTime();
    # Specifies the status of the payment information group.
    string Status;
    # Date and time at which the resource status was updated.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string StatusUpdateDateTime = getPastDateTime();
    # Expected execution date and time for the payment resource.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ExpectedExecutionDateTime?;
    # Expected settlement date and time for the payment resource.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ExpectedSettlementDateTime?;
    # Unambiguous identification of the refund account to which a refund will be made as a result of the transaction.
    DataRefund Refund?;
    # Set of elements used to provide details of a charge for the payment initiation.
    DataCharges[] Charges?;
    # The Initiation payload is sent by the initiating party to the bank. It is used to request movement of funds from the debtor account to a creditor for a single domestic payment.
    DomesticPaymentInitiation Initiation;
    # The multiple authorisation flow response from the bank.
    MultiAuthorisation MultiAuthorisation?;
    # ^ Only incuded in the response if `Data. ReadRefundAccount` is set to `Yes` in the consent.
    CreditorAccount Debtor?;
};

# Unambiguous identification of the refund account to which a refund will be made as a result of the transaction.
public type DataRefund record {
    # Provides the details to identify an account.
    CreditorAccount Account;
};

# Set of elements used to provide details of a charge for the payment initiation.
public type DataCharges record {
    # Specifies which party/parties will bear the charges associated with the processing of the payment transaction.
    string ChargeBearer;
    # Charge type, in a coded form.
    string Type;
    # Amount of money associated with the charge type.
    Amount Amount;
};

# The multiple authorisation flow response from the bank.
public type MultiAuthorisation record {
    # Specifies the status of the authorisation flow in code form.
    string Status;
    # Number of authorisations required for payment order (total required at the start of the multi authorisation journey).
    int NumberRequired?;
    # Number of authorisations received.
    int NumberReceived?;
    # Last date and time at the authorisation flow was updated.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string LastUpdateDateTime?;
    # Date and time at which the requested authorisation flow must be completed.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ExpirationDateTime?;
};
