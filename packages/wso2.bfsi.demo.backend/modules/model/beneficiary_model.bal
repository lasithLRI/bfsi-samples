// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

# Set of elements used to define the beneficiary details.
public type Beneficiary record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # A unique and immutable identifier used to identify the beneficiary resource. This identifier has no meaning to the account owner.
    readonly string BeneficiaryId;
    # Specifies the Beneficiary Type.
    string BeneficiaryType?;
    # Unique reference, as assigned by the creditor, to unambiguously refer to the payment transaction.
    # Usage: If available, the initiating party should provide this reference in the structured remittance information, to enable reconciliation by the creditor upon receipt of the amount of money.
    # If the business context requires the use of a creditor reference or a payment remit identification, and only one identifier can be passed through the end-to-end chain, the creditor's reference or payment remittance identification should be quoted in the end-to-end transaction identification.
    string Reference?;
    # Provides the details to identify the beneficiary account.
    CreditorAccount CreditorAccount?;
};
