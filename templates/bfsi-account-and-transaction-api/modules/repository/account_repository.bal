// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import bfsi_account_and_transaction_api.model;

# Creates a `Accounts` table type in which each member is uniquely identified using its `AccountId` field.
public final table<model:Account> key(AccountId) & readonly accounts = table [
    {
        AccountId: "A001",
        Status: "Enabled",
        AccountType: "Personal",
        AccountSubType: "Current Account"
    },
    {
        AccountId: "A002",
        Status: "Enabled",
        AccountType: "Personal",
        AccountSubType: "Savings Account"
    }
            ,
    {
        AccountId: "A003",
        Status: "Disabled",
        AccountType: "Personal",
        AccountSubType: "Joint Account"
    }
];

# Creates a `Balances` table type in which each member is uniquely identified using its `AccountId` field.
public final table<model:Balance> key(AccountId, BalanceId) & readonly balances = table [
    {
        AccountId: "A001",
        BalanceId: "B001",
        CreditDebitIndicator: "Credit",
        Type: "InterimBooked",
        CreditLine: [{Included: true, Type: "Available"}],
        Amount: {}
    },
    {
        AccountId: "A002",
        BalanceId: "B002",
        CreditDebitIndicator: "Dedit",
        Type: "ClosingAvailable",
        CreditLine: [{Included: false, Type: "PreAgreed"}],
        Amount: {}
    },
    {
        AccountId: "A001",
        BalanceId: "B003",
        CreditDebitIndicator: "Dedit",
        Type:
            "InterimBooked",
        Amount: {}
    }
];

# Creates a `Beneficiaries` table type in which each member is uniquely identified using its `AccountId` and 
# `BeneficiaryId` fields.
public final table<model:Beneficiary> key(AccountId, BeneficiaryId) & readonly beneficiaries = table [
    {
        AccountId: "A001",
        BeneficiaryId: "B001",
        Reference: "Airbender Club",
        CreditorAccount: {SchemeName: "SortCodeAccountNumber", Name: "Aang"}
    },
    {AccountId: "A002", BeneficiaryId: "B002", Reference: "Waterbender Club"},
    {AccountId: "A001", BeneficiaryId: "B003", Reference: "Firebender Club"}
];

# Creates a `DirectDebits` table type in which each member is uniquely identified using its `AccountId` and 
# `DirectDebitId` fields.
public final table<model:DirectDebit> key(AccountId, DirectDebitId) & readonly directDebits = table [
    {
        AccountId: "A001",
        DirectDebitId: "DB001",
        DirectDebitStatusCode: "Active",
        Name: "Airbender Club",
        PreviousPaymentAmount: {}
    },
    {
        AccountId: "A002",
        DirectDebitId: "DB002",
        Name: "Waterbender Club",
        PreviousPaymentAmount: {}
    },
    {
        AccountId: "A001",
        DirectDebitId: "DB003",
        Name: "Firebender Club",
        PreviousPaymentAmount: {}
    }
];

# Creates a `Offers` table type in which each member is uniquely identified using its `AccountId` and 
# `OfferId` fields.
public final table<model:Offer> key(AccountId, OfferId) & readonly offers = table [
    {
        AccountId: "A001",
        OfferId: "O001",
        OfferType: "BalanceTransfer",
        Description: "Credit limit increase",
        Amount: {},
        Fee: {}
    },
    {AccountId: "A002", OfferId: "O002", OfferType: "BalanceTransfer", Amount: {}, Fee: {}},
    {AccountId: "A001", OfferId: "O003", OfferType: "LimitIncrease", Amount: {}, Fee: {}}
];

# Creates a `Parties` table type in which each member is uniquely identified using its `PartyId` field.
public final table<model:Party> key(PartyId) & readonly parties = table [
    {
        PartyId: "P001",
        PartyNumber: "01",
        PartyType: "Delegate",
        FullLegalName: "Airbender PVT LTD",
        LegalStructure: "Private Limited Company",
        BeneficialOwnership: true,
        Relationships:
                {Account: {Related: "/accounts/A001", Id: "A001"}},
        Address: [{Country: "US", AddressType: "Business"}]
    },
    {
        PartyId: "P002",
        PartyNumber: "02",
        PartyType: "Delegate",
        FullLegalName: "Waterbender PVT LTD",
        LegalStructure: "Limited Company",
        BeneficialOwnership: true,
        Relationships:
                {Account: {Related: "/accounts/A002", Id: "A002"}},
        Address: [{Country: "US", AddressType: "Personal"}]
    },
    {
        PartyId: "P003",
        PartyNumber: "03",
        PartyType: "Sole",
        FullLegalName: "Firebender PVT LTD",
        LegalStructure: "Limited Company",
        BeneficialOwnership: false,
        Relationships:
                {Account: {Related: "/accounts/A001", Id: "A001"}},
        Address: [{Country: "US", AddressType: "Personal"}]
    }
];

# Creates a `Product` table type in which each member is uniquely identified using its `AccountId` and 
# `ProductId` fields.
public final table<model:Product> key(AccountId, ProductId) & readonly products = table [
    {
        AccountId: "A001",
        ProductId: "P001",
        ProductType: "BusinessCurrentAccount",
        ProductName: "Wind sword"
    },
    {
        AccountId: "A002",
        ProductId: "P002",
        ProductType: "PersonalCurrentAccount",
        ProductName: "Wolf armor"
    },
    {
        AccountId: "A001",
        ProductId: "P003",
        ProductType: "PersonalSavingsAccount",
        ProductName: "Dual broadswords"
    }
];

# Creates a `ScheduledPayment` table type in which each member is uniquely identified using its `AccountId` and 
# `ScheduledPaymentId` fields.
public final table<model:ScheduledPayment> key(AccountId, ScheduledPaymentId) & readonly scheduledPayments =
    table [
    {
        AccountId: "A001",
        ScheduledPaymentId: "SP001",
        ScheduledType: "Arrival",
        InstructedAmount: {},
        CreditorAccount: {SchemeName: "Air Nomads", Name: "Aang"}
    },
    {
        AccountId: "A002",
        ScheduledPaymentId: "SP002",
        ScheduledType: "Arrival",
        InstructedAmount: {},
        CreditorAccount: {SchemeName: "Water Tribe", Name: "Korra"}
    },
    {
        AccountId: "A001",
        ScheduledPaymentId: "SP003",
        ScheduledType: "Execution",
        InstructedAmount: {},
        CreditorAccount: {SchemeName: "Fire Nation", Name: "Azula"}
    }
];

# Creates a `StandingOrder` table type in which each member is uniquely identified using its `AccountId` and 
# `StandingOrderId` fields.
public final table<model:StandingOrder> key(AccountId, StandingOrderId) & readonly standingOrders = table [
    {
        AccountId: "A001",
        StandingOrderId: "SO001",
        Frequency: "EveryWorkingDay",
        Reference: "Northern Air Temple",
        FirstPaymentAmount: {},
        NextPaymentAmount: {},
        FinalPaymentAmount: {},
        StandingOrderStatusCode: "Active",
        CreditorAccount: {SchemeName: "Air Nomads"}
    },
    {
        AccountId: "A002",
        StandingOrderId: "SO002",
        Frequency: "EveryMonday",
        Reference: "Foggy Swamp",
        FirstPaymentAmount: {},
        NextPaymentAmount: {},
        FinalPaymentAmount: {},
        StandingOrderStatusCode: "Active",
        CreditorAccount: {SchemeName: "Water Tribe"}
    },
    {
        AccountId: "A001",
        StandingOrderId: "SO003",
        Frequency: "EveryFriday",
        Reference: "Fire Fountain City",
        FirstPaymentAmount: {},
        NextPaymentAmount: {},
        FinalPaymentAmount: {},
        StandingOrderStatusCode: "Inactive",
        CreditorAccount: {SchemeName: "Fire Nation"}
    }
];

# Creates a `Statement` table type in which each member is uniquely identified using its `AccountId` and 
# `StatementId` fields.
public final table<model:Statement> key(AccountId, StatementId) & readonly statements = table [
    {
        AccountId: "A001",
        StatementId: "S001",
        Type: "RegularPeriodic",
        StatementAmount: [
            {Type: "ClosingBalance", Amount: {}, CreditDebitIndicator: "Credit"},
            {Type: "PreviousClosingBalance", Amount: {}, CreditDebitIndicator: "Credit"}
        ]
    },
    {
        AccountId: "A002",
        StatementId: "S002",
        Type: "RegularPeriodic",
        StatementAmount: [{Type: "PreviousClosingBalance", Amount: {}, CreditDebitIndicator: "Credit"}]
    },
    {
        AccountId: "A001",
        StatementId: "S003",
        Type: "AccountClosure",
        StatementAmount: [{Type: "PreviousClosingBalance", Amount: {}, CreditDebitIndicator: "Debit"}]
    }
];

# Creates a `Transaction` table type in which each member is uniquely identified using its `AccountId` and 
# `TransactionId` fields.
public final table<model:Transaction> key(AccountId, TransactionId) & readonly transactions = table [
    {
        AccountId: "A001",
        TransactionId: "T001",
        Status: "Booked",
        TransactionReference: "Airbender club payment",
        StatementReference: ["S001"],
        Amount: {},
        CreditDebitIndicator: "Credit",
        ChargeAmount: {},
        BankTransactionCode: {Code: "BT", SubCode: "001"},
        Balance: {CreditDebitIndicator: "Credit", Type: "InterimBooked", Amount: {}}
    },
    {
        AccountId: "A002",
        TransactionId: "T002",
        Status: "Booked",
        TransactionReference: "Waterbender club payment",
        StatementReference: ["S002"],
        Amount: {},
        CreditDebitIndicator: "Dedit",
        ChargeAmount: {},
        BankTransactionCode: {Code: "BT", SubCode: "002"}
    },
    {
        AccountId: "A001",
        TransactionId: "T003",
        Status: "Pending",
        TransactionReference: "Firebender club payment",
        StatementReference: ["S001", "S002"],
        Amount: {},
        CreditDebitIndicator: "Dedit",
        ChargeAmount: {},
        BankTransactionCode: {Code: "BT", SubCode: "003"},
        Balance: {CreditDebitIndicator: "Dedit", Type: "InterimBooked", Amount: {}}
    }
];
