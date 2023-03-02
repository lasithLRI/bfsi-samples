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

# Set of elements used to define the scheduled payment details.
public type ScheduledPayment record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # A unique and immutable identifier used to identify the scheduled payment resource. This identifier has no meaning to the account owner.
    readonly string ScheduledPaymentId;
    # The date on which the scheduled payment will be made.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ScheduledPaymentDateTime = util:getFutureDateTime();
    # Specifies the scheduled payment date type requested
    string ScheduledType;
    # Unique reference, as assigned by the creditor, to unambiguously refer to the payment transaction.
    # Usage: If available, the initiating party should provide this reference in the structured remittance information, to enable reconciliation by the creditor upon receipt of the amount of money.
    # If the business context requires the use of a creditor reference or a payment remit identification, and only one identifier can be passed through the end-to-end chain, the creditor's reference or payment remittance identification should be quoted in the end-to-end transaction identification.
    string Reference?;
    # A reference value provided by the user to the TPP while setting up the scheduled payment.
    string DebtorReference?;
    # Amount of money to be moved between the debtor and creditor, before deduction of charges, expressed in the currency as ordered by the initiating party.
    # Usage: This amount has to be transported unchanged through the transaction chain.
    Amount InstructedAmount;
    # Party that manages the account on behalf of the account owner, that is manages the registration and booking of entries on the account, calculates balances on the account and provides information about the account.
    # This is the servicer of the beneficiary account.
    CreditorAgent CreditorAgent?;
    # Provides the details to identify the beneficiary account.
    CreditorAccount CreditorAccount?;
};
