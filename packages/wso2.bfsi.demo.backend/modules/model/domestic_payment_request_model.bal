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

#Represents a domestic pament intitiation request payload.
public type DomesticPaymentRequest record {
    #Represents data of the domestic pament intitiation request
    DomesticPaymentRequestData Data;
    # The Risk section is sent by the initiating party to the bank.
    # It is used to specify additional details for risk scoring for Payments.
    Risk Risk;
};

#Represents data of the domestic pament intitiation request.
public type DomesticPaymentRequestData record {
    # OB: Unique identification as assigned by the bank to uniquely identify the consent resource.
    @constraint:String {maxLength: 128, minLength: 1}
    readonly string ConsentId;
    # The Initiation payload is sent by the initiating party to the bank. It is used to request movement of funds from the debtor account to a creditor for a single domestic payment.
    DomesticPaymentInitiation Initiation;
};

# The Risk section is sent by the initiating party to the bank.
# It is used to specify additional details for risk scoring for Payments.
public type Risk record {
    # Specifies the payment context
    # * BillPayment - @deprecated
    # * EcommerceGoods - @deprecated
    # * EcommerceServices - @deprecated
    # * Other - @deprecated
    # * PartyToParty - @deprecated
    string PaymentContextCode?;
    # Category code conform to ISO 18245, related to the type of services or goods the merchant provides for the transaction.
    @constraint:String {maxLength: 4, minLength: 3}
    string MerchantCategoryCode?;
    # The unique customer identifier of the user with the merchant.
    @constraint:String {maxLength: 70, minLength: 1}
    string MerchantCustomerIdentification?;
    # Indicates if Payee has a contractual relationship with the TPP.
    boolean ContractPresentInidicator?;
    # Indicates if TPP has immutably prepopulated payment details in for the user.
    boolean BeneficiaryPrepopulatedIndicator?;
    # Category code, related to the type of services or goods that corresponds to the underlying purpose of the payment that conforms to Recommended Purpose Code in ISO 20022 Payment Messaging List
    @constraint:String {maxLength: 4, minLength: 3}
    string PaymentPurposeCode?;
    # Specifies the extended type of account.
    string BeneficiaryAccountType?;
    # Information that locates and identifies a specific address,
    # as defined by postal services or in free format text.
    PostalAddress DeliveryAddress?;
};
