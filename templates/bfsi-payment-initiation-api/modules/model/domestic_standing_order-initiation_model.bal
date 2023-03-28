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

# The Initiation payload is sent by the initiating party to the bank. It is used to request movement of funds from the debtor account to a creditor for a domestic standing order.
public type DomesticStandingOrderInitiation record {|
    # Frequency of the standing order payment. Allowed values are EvryDay, EvryWorkgDay, IntrvlWkDay,
    # WkInMnthDay, IntrvlMnthDay and QtrDay.
    string Frequency;
    # Unique reference, as assigned by the creditor, to unambiguously refer to the payment transaction.
    # Usage: If available, the initiating party should provide this reference in the structured remittance
    # information, to enable reconciliation by the creditor upon receipt of the amount of money.
    # If the business context requires the use of a creditor reference or a payment remit identification, 
    # and only one identifier can be passed through the end-to-end chain, the creditor's reference or 
    # payment remittance identification should be quoted in the end-to-end transaction identification.
    @constraint:String {maxLength: 35, minLength: 1}
    string Reference?;
    # Number of the payments that will be made in completing this frequency sequence including any execute
    # d since the sequence start date.
    @constraint:String {maxLength: 35, minLength: 1}
    string NumberOfPayments?;
    # The date on which the first payment for a Standing Order schedule will be made.All dates in the 
    # JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FirstPaymentDateTime = getFutureDateTime();
    # The date on which the first recurring payment for a Standing Order schedule will be made. 
    # Usage: This must be populated only if the first recurring date is different to the first payment
    # date.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string RecurringPaymentDateTime?;
    # The date on which the final payment for a Standing Order schedule will be made.All dates in the 
    # JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FinalPaymentDateTime?;
    # The amount of the first Standing Order
    Amount FirstPaymentAmount;
    # The amount of the recurring Standing Order
    Amount RecurringPaymentAmount?;
    # The amount of the final Standing Order
    Amount FinalPaymentAmount?;
    # Unambiguous identification of the account of the debtor to which a debit entry will be made as a
    # result of the transaction.
    DebtorAccount DebtorAccount?;
    # Identification assigned by an institution to identify an account. This identification is known by
    # the account owner.
    CreditorAccount CreditorAccount;
    # Additional information that can not be captured in the structured fields and/or any other specific
    # block.
    anydata SupplementaryData?;
|};
