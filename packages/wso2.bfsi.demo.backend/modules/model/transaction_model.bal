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

# Provides further details on an entry in the report.
public type Transaction record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # Unique identifier for the transaction within an servicing institution. This identifier is both unique and immutable.
    readonly string TransactionId;
    # Unique reference for the transaction. This reference is optionally populated, and may as an example be the FPID in the Faster Payments context.
    string TransactionReference?;
    #Describes transaction statement reference
    string[] StatementReference?;
    # Indicates whether the transaction is a credit or a debit entry.
    string CreditDebitIndicator;
    # Status of a transaction entry on the books of the account servicer.
    string Status;
    # Specifies the Mutability of the Transaction record.
    string TransactionMutability?;
    # Date and time when a transaction entry is posted to an account on the account servicer's books.
    # Usage: Booking date is the expected booking date, unless the status is booked, in which case it is the actual booking date.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string BookingDateTime = util:getPastDateTime();
    # Date and time at which assets become available to the account owner in case of a credit entry, or cease to be available to the account owner in case of a debit transaction entry.
    # Usage: If transaction entry status is pending and value date is present, then the value date refers to an expected/requested value date.
    # For transaction entries subject to availability/float and for which availability information is provided, the value date must not be used. In this case the availability component identifies the number of availability days.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ValueDateTime?;
    # Further details of the transaction. 
    # This is the transaction narrative, which is unstructured text.
    string TransactionInformation?;
    # Information that locates and identifies a specific address for a transaction entry, that is presented in free format text.
    string AddressLine?;
    # Amount of money in the cash transaction entry.
    Amount Amount;
    # Transaction charges to be paid by the charge bearer.
    Amount ChargeAmount?;
    # Set of elements used to provide details on the currency exchange.
    CurrencyExchange CurrencyExchange?;
    # Set of elements used to fully identify the type of underlying transaction resulting in an entry.
    BankTransactionCode BankTransactionCode?;
    # Set of elements to fully identify a proprietary bank transaction code.
    ProprietaryBankTransactionCode ProprietaryBankTransactionCode?;
    # Set of elements used to define the balance as a numerical representation of the net increases and decreases in an account after a transaction entry is applied to the account.
    BalanceAmount Balance?;
    # Details of the merchant involved in the transaction.
    MerchantDetails MerchantDetails?;
    # Financial institution servicing an account for the creditor.
    TransactionCreditorAgent CreditorAgent?;
    # Unambiguous identification of the account of the creditor, in the case of a debit transaction.
    CreditorAccount CreditorAccount?;
    # Financial institution servicing an account for the debtor.
    TransactionCreditorAgent DebtorAgent?;
    # Unambiguous identification of the account of the debtor, in the case of a crebit transaction.
    CreditorAccount DebtorAccount?;
    # Set of elements to describe the card instrument used in the transaction.
    CardInstrument CardInstrument?;
    # Additional information that can not be captured in the structured fields and/or any other specific block.
    Object SupplementaryData?;
};

# Set of elements used to provide details on the currency exchange.
public type CurrencyExchange record {
    # Currency from which an amount is to be converted in a currency conversion.
    string SourceCurrency;
    # Currency into which an amount is to be converted in a currency conversion.
    string TargetCurrency?;
    # Currency in which the rate of exchange is expressed in a currency exchange. In the example 1GBP = xxxCUR, the unit currency is GBP.
    string UnitCurrency?;
    # Factor used to convert an amount from one currency into another. This reflects the price at which one currency was bought with another currency.
    # Usage: ExchangeRate expresses the ratio between UnitCurrency and QuotedCurrency (ExchangeRate = UnitCurrency/QuotedCurrency).
    decimal ExchangeRate;
    # Unique identification to unambiguously identify the foreign exchange contract.
    string ContractIdentification?;
    # Date and time at which an exchange rate is quoted.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string QuotationDate?;
    # Amount of money to be moved between the debtor and creditor, before deduction of charges, expressed in the currency as ordered by the initiating party.
    Amount InstructedAmount?;
};

# Set of elements used to fully identify the type of underlying transaction resulting in an entry.
public type BankTransactionCode record {
    # Specifies the family within a domain.
    string Code;
    # Specifies the sub-product family within a specific family.
    string SubCode;
};

# Set of elements to fully identify a proprietary bank transaction code.
public type ProprietaryBankTransactionCode record {
    # Proprietary bank transaction code to identify the underlying transaction.
    string Code;
    # Identification of the issuer of the proprietary bank transaction code.
    string Issuer?;
};

# Details of the merchant involved in the transaction.
public type MerchantDetails record {
    # Name by which the merchant is known.
    string MerchantName?;
    # Category code conform to ISO 18245, related to the type of services or goods the merchant provides for the transaction.
    string MerchantCategoryCode?;
};

# Financial institution servicing an account for the creditor.
public type TransactionCreditorAgent record {
    # Name of the identification scheme, in a coded form as published in an external list.
    string SchemeName?;
    # Unique and unambiguous identification of a financial institution or a branch of a financial institution.
    string Identification?;
    # Name by which an agent is known and which is usually used to identify that agent.
    string Name?;
    # Information that locates and identifies a specific address, as defined by postal services.
    PostalAddress PostalAddress?;
};

# Set of elements to describe the card instrument used in the transaction.
public type CardInstrument record {
    # Name of the card scheme.
    string CardSchemeName;
    # The card authorisation type.
    string AuthorisationType?;
    # Name of the cardholder using the card instrument.
    string Name?;
    # Identification assigned by an institution to identify the card instrument used in the transaction. This identification is known by the account owner, and may be masked.
    string Identification?;
};
