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

# The Initiation payload is sent by the initiating party to the bank. It is used to request movement of 
# funds from the debtor account to a creditor for an international standing order.
public type InternationalStandingOrderInitiation record {|
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
    # Number of the payments that will be made in completing this frequency sequence including any 
    # executed since the sequence start date.
    @constraint:String {maxLength: 35, minLength: 1}
    string NumberOfPayments?;
    # The date on which the first payment for a Standing Order schedule will be made.All dates in the
    # JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FirstPaymentDateTime;
    # The date on which the final payment for a Standing Order schedule will be made.All dates in the
    # JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FinalPaymentDateTime?;
    # Specifies the external purpose code in the format of character string with a maximum length of
    # 4 characters.
    # The list of valid codes is an external code list published separately.
    # External code sets can be downloaded from www.iso20022.org.
    @constraint:String {maxLength: 4, minLength: 1}
    string Purpose?;
    # Specifies the purpose of an international payment, when there is no corresponding 4 character 
    # code available in the ISO20022 list of Purpose Codes.
    @constraint:String {maxLength: 140, minLength: 1}
    string ExtendedPurpose?;
    # Specifies which party/parties will bear the charges associated with the processing of the payment
    # transaction.
    string ChargeBearer?;
    # Specifies the currency of the to be transferred amount, which is different from the currency of 
    # the debtor's account.
    string CurrencyOfTransfer;
    # Country in which Credit Account is domiciled. Code to identify a country, a dependency, or another
    # area of particular geopolitical interest, on the basis of country names obtained from the United 
    # Nations (ISO 3166, Alpha-2 code).
    string DestinationCountryCode?;
    # Amount of money to be moved between the debtor and creditor, before deduction of charges, expressed
    # in the currency as ordered by the initiating party.
    # Usage: This amount has to be transported unchanged through the transaction chain.
    Amount InstructedAmount;
    # Unambiguous identification of the account of the debtor to which a debit entry will be made as a 
    # result of the transaction.
    DebtorAccount DebtorAccount?;
    # Party to which an amount of money is due.
    Creditor Creditor?;
    # Party that manages the account on behalf of the account owner, that is manages the registration 
    # and booking of entries on the account, calculates balances on the account and provides information
    # about the account.
    # This is the servicer of the beneficiary account.
    CreditorAgent CreditorAgent?;
    # Provides the details to identify the beneficiary account.
    CreditorAccount CreditorAccount;
    # Additional information that can not be captured in the structured fields and/or any other specific
    # block.
    anydata SupplementaryData?;
|};
