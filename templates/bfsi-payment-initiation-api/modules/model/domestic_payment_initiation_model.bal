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
# funds from the debtor account to a creditor for a single domestic payment.
public type DomesticPaymentInitiation record {|
    # Unique identification as assigned by an instructing party for an instructed party to unambiguously 
    # identify the instruction. Usage: the  instruction identification is a point to point reference that 
    # can be used between the instructing party and the instructed party to refer to the individual 
    # instruction. It can be included in several messages related to the instruction.
    @constraint:String {maxLength: 35, minLength: 1}
    string InstructionIdentification;
    # Unique identification assigned by the initiating party to unambiguously identify the transaction. 
    # This identification is passed on, unchanged, throughout the entire end-to-end chain. 
    # Usage: The end-to-end identification can be used for reconciliation or to link tasks relating to 
    # the transaction. It can be included in several messages related to the transaction.
    # OB: The Faster Payments Scheme can only access 31 characters for the EndToEndIdentification field.
    @constraint:String {maxLength: 35, minLength: 1}
    string EndToEndIdentification;
    # User community specific instrument.
    # Usage: This element is used to specify a local instrument, local clearing option and/or further 
    # qualify the service or service level.
    string LocalInstrument?;
    # Amount of money to be moved between the debtor and creditor, before deduction of charges, expressed 
    # in the currency as ordered by the initiating party.
    # Usage: This amount has to be transported unchanged through the transaction chain.
    Amount InstructedAmount;
    # Unambiguous identification of the account of the debtor to which a debit entry will be made as a 
    # result of the transaction.
    DebtorAccount DebtorAccount?;
    # Unambiguous identification of the account of the creditor to which a credit entry will be posted 
    # as a result of the payment transaction.
    CreditorAccount CreditorAccount;
    # Information that locates and identifies a specific address, as defined by postal services.
    PostalAddress CreditorPostalAddress?;
    # Information supplied to enable the matching of an entry with the items that the transfer is intended 
    # to settle, such as commercial invoices in an accounts' receivable system.
    RemittanceInformation RemittanceInformation?;
    # Additional information that can not be captured in the structured fields and/or any other specific 
    # block.
    anydata SupplementaryData?;
|};

# Information supplied to enable the matching of an entry with the items that the transfer is intended to 
# settle, such as commercial invoices in an accounts' receivable system.
public type RemittanceInformation record {|
    # Information supplied to enable the matching/reconciliation of an entry with the items that the 
    # payment is intended to settle, such as commercial invoices in an accounts' receivable system, 
    # in an unstructured form.
    @constraint:String {maxLength: 140, minLength: 1}
    string Unstructured?;
    # Unique reference, as assigned by the creditor, to unambiguously refer to the payment transaction.
    # Usage: If available, the initiating party should provide this reference in the structured remittance 
    # information, to enable reconciliation by the creditor upon receipt of the amount of money.
    # If the business context requires the use of a creditor reference or a payment remit identification, 
    # and only one identifier can be passed through the end-to-end chain, the creditor's reference or 
    # payment remittance identification should be quoted in the end-to-end transaction identification.
    # OB: The Faster Payments Scheme can only accept 18 characters for the ReferenceInformation field - 
    # which is where this ISO field will be mapped.
    @constraint:String {maxLength: 35, minLength: 1}
    string Reference?;
|};
