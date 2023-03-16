// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/log;

import bfsi_account_and_transaction_api.model;

# Works as the account service repository.
public isolated class AccountsRepository {

    # Creates a `Accounts` table type in which each member is uniquely identified using its `AccountId` field.
    private final table<model:Account> key(AccountId) accounts = loadInitialAccounts();
    # Creates a `Balances` table type in which each member is uniquely identified using its `AccountId` field.
    private table<model:Balance> key(AccountId, BalanceId) balances = loadInitialBalances();
    # Creates a `Beneficiaries` table type in which each member is uniquely identified using its `AccountId` and 
    # `BeneficiaryId` fields.
    private table<model:Beneficiary> key(AccountId, BeneficiaryId) beneficiaries = loadInitialBeneficiaries();
    # Creates a `DirectDebits` table type in which each member is uniquely identified using its `AccountId` and 
    # `DirectDebitId` fields.
    private table<model:DirectDebit> key(AccountId, DirectDebitId) directDebits = loadInitialDirectDebits();
    # Creates a `Offers` table type in which each member is uniquely identified using its `AccountId` and 
    # `OfferId` fields.
    private table<model:Offer> key(AccountId, OfferId) offers = loadInitialOffers();
    # Creates a `Parties` table type in which each member is uniquely identified using its `PartyId` field.
    private table<model:Party> key(PartyId) parties = loadInitialParties();
    # Creates a `Product` table type in which each member is uniquely identified using its `AccountId` and 
    # `ProductId` fields.
    private table<model:Product> key(AccountId, ProductId) products = loadInitialProducts();
    # Creates a `ScheduledPayment` table type in which each member is uniquely identified using its `AccountId` and 
    # `ScheduledPaymentId` fields.
    private table<model:ScheduledPayment> key(AccountId, ScheduledPaymentId) scheduledPayments =
        loadInitialScheduledPayments();
    # Creates a `StandingOrder` table type in which each member is uniquely identified using its `AccountId` and 
    # `StandingOrderId` fields.
    private table<model:StandingOrder> key(AccountId, StandingOrderId) standingOrders = loadInitialStandingOrders();
    # Creates a `Statement` table type in which each member is uniquely identified using its `AccountId` and 
    # `StatementId` fields.
    private table<model:Statement> key(AccountId, StatementId) statements = loadInitialStatements();
    # Creates a `Transaction` table type in which each member is uniquely identified using its `AccountId` and 
    # `TransactionId` fields.
    private table<model:Transaction> key(AccountId, TransactionId) transactions = loadInitialTransactions();

    public isolated function getAllAccounts() returns table<model:Account> key(AccountId) {
        lock {
            return self.accounts.clone();
        }
    }

    public isolated function getAllBalances() returns table<model:Balance> key(AccountId, BalanceId) {
        lock {
            return self.balances.clone();
        }
    }

    public isolated function getAllBeneficiaries() returns table<model:Beneficiary> key(AccountId, BeneficiaryId) {
        lock {
            return self.beneficiaries.clone();
        }
    }

    public isolated function getAllDirectDebits() returns table<model:DirectDebit> key(AccountId, DirectDebitId) {
        lock {
            return self.directDebits.clone();
        }
    }

    public isolated function getAllOffers() returns table<model:Offer> key(AccountId, OfferId) {
        lock {
            return self.offers.clone();
        }
    }

    public isolated function getAllParties() returns table<model:Party> key(PartyId) {
        lock {
            return self.parties.clone();
        }
    }

    public isolated function getAllProducts() returns table<model:Product> key(AccountId, ProductId) {
        lock {
            return self.products.clone();
        }
    }

    public isolated function getAllScheduledPayments()
            returns table<model:ScheduledPayment> key(AccountId, ScheduledPaymentId) {
        lock {
            return self.scheduledPayments.clone();
        }
    }

    public isolated function getAllStandingOrders() returns table<model:StandingOrder> key(AccountId, StandingOrderId) {
        lock {
            return self.standingOrders.clone();
        }
    }

    public isolated function getAllStatements() returns table<model:Statement> key(AccountId, StatementId) {
        lock {
            return self.statements.clone();
        }
    }

    public isolated function getAllTransactions() returns table<model:Transaction> key(AccountId, TransactionId) {
        lock {
            return self.transactions.clone();
        }
    }
}

isolated function loadInitialAccounts() returns table<model:Account> key(AccountId) {
    log:printDebug("Initiating accounts table");
    return table [
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
}

isolated function loadInitialBalances() returns table<model:Balance> key(AccountId, BalanceId) {
    log:printDebug("Initiating balances table");
    return table [
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
}

isolated function loadInitialBeneficiaries() returns table<model:Beneficiary> key(AccountId, BeneficiaryId) {
    log:printDebug("Initiating beneficiaries table");
    return table [
        {
            AccountId: "A001",
            BeneficiaryId: "B001",
            Reference: "Airbender Club",
            CreditorAccount: {SchemeName: "SortCodeAccountNumber", Name: "Aang"}
        },
        {AccountId: "A002", BeneficiaryId: "B002", Reference: "Waterbender Club"},
        {AccountId: "A001", BeneficiaryId: "B003", Reference: "Firebender Club"}
    ];
}

isolated function loadInitialDirectDebits() returns table<model:DirectDebit> key(AccountId, DirectDebitId) {
    log:printDebug("Initiating directDebits table");
    return table [
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
}

isolated function loadInitialOffers() returns table<model:Offer> key(AccountId, OfferId) {
    log:printDebug("Initiating offers table");
    return table [
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
}

isolated function loadInitialParties() returns table<model:Party> key(PartyId) {
    log:printDebug("Initiating parties table");
    return table [
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
}

isolated function loadInitialProducts() returns table<model:Product> key(AccountId, ProductId) {
    log:printDebug("Initiating products table");
    return table [
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
}

isolated function loadInitialScheduledPayments()
        returns table<model:ScheduledPayment> key(AccountId, ScheduledPaymentId) {
    log:printDebug("Initiating scheduledPayments table");
    return table [
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
}

isolated function loadInitialStandingOrders() returns table<model:StandingOrder> key(AccountId, StandingOrderId) {
    log:printDebug("Initiating standingOrders table");
    return table [
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
}

isolated function loadInitialStatements() returns table<model:Statement> key(AccountId, StatementId) {
    log:printDebug("Initiating statements table");
    return table [
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
}

isolated function loadInitialTransactions() returns table<model:Transaction> key(AccountId, TransactionId) {
    log:printDebug("Initiating transactions table");
    return table [
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
}
