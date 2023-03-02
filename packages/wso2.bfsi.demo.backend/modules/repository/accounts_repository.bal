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
import wso2.bfsi.demo.backend.model;

#Works as the account service repository.
public class AccountsRepository {

    #Creates a `Accounts` table type in which each member is uniquely identified using its `AccountId` field.
    private table<model:Account> key(AccountId) accounts = table[];
    #Creates a `Balances` table type in which each member is uniquely identified using its `AccountId` field.
    private table<model:Balance> key(AccountId, BalanceId) balances = table[];
    #Creates a `Beneficiaries` table type in which each member is uniquely identified using its `AccountId` and `BeneficiaryId` fields.
    private table<model:Beneficiary> key(AccountId, BeneficiaryId) beneficiaries = table[];
    #Creates a `DirectDebits` table type in which each member is uniquely identified using its `AccountId` and `DirectDebitId` fields.
    private table<model:DirectDebit> key(AccountId, DirectDebitId) directDebits = table[];
    #Creates a `Offers` table type in which each member is uniquely identified using its `AccountId` and `OfferId` fields.
    private table<model:Offer> key(AccountId, OfferId) offers = table[];
    #Creates a `Parties` table type in which each member is uniquely identified using its `PartyId` field.
    private table<model:Party> key(PartyId) parties = table[];
    #Creates a `Product` table type in which each member is uniquely identified using its `AccountId` and `ProductId` fields.
    private table<model:Product> key(AccountId, ProductId) products = table[];
    #Creates a `ScheduledPayment` table type in which each member is uniquely identified using its `AccountId` and `ScheduledPaymentId` fields.
    private table<model:ScheduledPayment> key(AccountId, ScheduledPaymentId) scheduledPayments = table[];
    #Creates a `StandingOrder` table type in which each member is uniquely identified using its `AccountId` and `StandingOrderId` fields.
    private table<model:StandingOrder> key(AccountId, StandingOrderId) standingOrders = table[];
    #Creates a `Statement` table type in which each member is uniquely identified using its `AccountId` and `StatementId` fields.
    private table<model:Statement> key(AccountId, StatementId) statements = table[];
    #Creates a `Transaction` table type in which each member is uniquely identified using its `AccountId` and `TransactionId` fields.
    private table<model:Transaction> key(AccountId, TransactionId) transactions = table[];

    // The `init` method initializes the object.
    public isolated function init() {
        log:printDebug("Initiating a database");

        // Initiating accounts
        log:printDebug("Initiating accounts table");
        self.accounts.put({AccountId: "A001", Status: "Enabled", AccountType: "Personal", AccountSubType: "Current Account"});
        self.accounts.put({AccountId: "A002", Status: "Enabled", AccountType: "Personal", AccountSubType: "Savings Account"});
        self.accounts.put({AccountId: "A003", Status: "Disabled", AccountType: "Personal", AccountSubType: "Joint Account"});

        // Initiating balances
        log:printDebug("Initiating balances table");
        self.balances.put({AccountId: "A001", BalanceId: "B001", CreditDebitIndicator: "Credit", Type: "InterimBooked", CreditLine: [{Included: true, Type: "Available" }], Amount: {}});
        self.balances.put({AccountId: "A002", BalanceId: "B002", CreditDebitIndicator: "Dedit", Type: "ClosingAvailable", CreditLine: [{Included: false, Type: "PreAgreed" }], Amount: {}});
        self.balances.put({AccountId: "A001", BalanceId: "B003", CreditDebitIndicator: "Dedit", Type: "InterimBooked", Amount: {}});

        // Initiating beneficiaries
        log:printDebug("Initiating beneficiaries table");
        self.beneficiaries.put({AccountId: "A001", BeneficiaryId: "B001", Reference: "Airbender Club", CreditorAccount: {SchemeName: "SortCodeAccountNumber", Name: "Aang"}});
        self.beneficiaries.put({AccountId: "A002", BeneficiaryId: "B002", Reference: "Waterbender Club"});
        self.beneficiaries.put({AccountId: "A001", BeneficiaryId: "B003", Reference: "Firebender Club"});

        // Initiating directdebits
        log:printDebug("Initiating directDebits table");
        self.directDebits.put({AccountId: "A001", DirectDebitId: "DB001", DirectDebitStatusCode: "Active", Name: "Airbender Club", PreviousPaymentAmount: {}});
        self.directDebits.put({AccountId: "A002", DirectDebitId: "DB002", Name: "Waterbender Club", PreviousPaymentAmount: {}});
        self.directDebits.put({AccountId: "A001", DirectDebitId: "DB003", Name: "Firebender Club", PreviousPaymentAmount: {}});

        // Initiating offers
        log:printDebug("Initiating offers table");
        self.offers.put({AccountId: "A001", OfferId: "O001", OfferType: "BalanceTransfer", Description: "Credit limit increase", Amount: {}, Fee: {}});
        self.offers.put({AccountId: "A002", OfferId: "O002", OfferType: "BalanceTransfer", Amount: {}, Fee: {}});
        self.offers.put({AccountId: "A001", OfferId: "O003", OfferType: "LimitIncrease", Amount: {}, Fee: {}});

        // Initiating parties
        log:printDebug("Initiating parties table");
        self.parties.put({PartyId: "P001", PartyNumber: "01", PartyType: "Delegate", FullLegalName: "Airbender PVT LTD", 
            LegalStructure: "Private Limited Company", BeneficialOwnership: true, Relationships: 
            {Account: {Related: "/accounts/A001", Id: "A001"}}, Address: [{Country: "US", AddressType: "Business"}]});
        self.parties.put({PartyId: "P002", PartyNumber: "02", PartyType: "Delegate", FullLegalName: "Waterbender PVT LTD", 
            LegalStructure: "Limited Company", BeneficialOwnership: true, Relationships: 
            {Account: {Related: "/accounts/A002", Id: "A002"}}, Address: [{Country: "US", AddressType: "Personal"}]});
        self.parties.put({PartyId: "P003", PartyNumber: "03", PartyType: "Sole", FullLegalName: "Firebender PVT LTD", 
            LegalStructure: "Limited Company", BeneficialOwnership: false, Relationships: 
            {Account: {Related: "/accounts/A001", Id: "A001"}}, Address: [{Country: "US", AddressType: "Personal"}]});
        
        // Initiating products
        log:printDebug("Initiating products table");
        self.products.put({AccountId: "A001", ProductId: "P001", ProductType: "BusinessCurrentAccount", ProductName: "Wind sword"});
        self.products.put({AccountId: "A002", ProductId: "P002", ProductType: "PersonalCurrentAccount", ProductName: "Wolf armor"});
        self.products.put({AccountId: "A001", ProductId: "P003", ProductType: "PersonalSavingsAccount", ProductName: "Dual broadswords"});

        // Initiating scheduledPayments
        log:printDebug("Initiating scheduledPayments table");
        self.scheduledPayments.put({AccountId: "A001", ScheduledPaymentId: "SP001", ScheduledType: "Arrival", 
            InstructedAmount: {}, CreditorAccount: {SchemeName: "Air Nomads", Name: "Aang"}});
        self.scheduledPayments.put({AccountId: "A002", ScheduledPaymentId: "SP002", ScheduledType: "Arrival", 
            InstructedAmount: {}, CreditorAccount: {SchemeName: "Water Tribe", Name: "Korra"}});
        self.scheduledPayments.put({AccountId: "A001", ScheduledPaymentId: "SP003", ScheduledType: "Execution", 
            InstructedAmount: {}, CreditorAccount: {SchemeName: "Fire Nation", Name: "Azula"}});

        // Initiating standingOrders
        log:printDebug("Initiating standingOrders table");
        self.standingOrders.put({AccountId: "A001", StandingOrderId: "SO001", Frequency: "EveryWorkingDay", Reference: "Northern Air Temple", 
            FirstPaymentAmount: {}, NextPaymentAmount: {}, FinalPaymentAmount: {}, StandingOrderStatusCode: "Active", CreditorAccount: {SchemeName: "Air Nomads"}});
        self.standingOrders.put({AccountId: "A002", StandingOrderId: "SO002", Frequency: "EveryMonday", Reference: "Foggy Swamp", 
            FirstPaymentAmount: {}, NextPaymentAmount: {}, FinalPaymentAmount: {}, StandingOrderStatusCode: "Active", CreditorAccount: {SchemeName: "Water Tribe"}});
        self.standingOrders.put({AccountId: "A001", StandingOrderId: "SO003", Frequency: "EveryFriday", Reference: "Fire Fountain City", 
            FirstPaymentAmount: {}, NextPaymentAmount: {}, FinalPaymentAmount: {}, StandingOrderStatusCode: "Inactive", CreditorAccount: {SchemeName: "Fire Nation"}});

        // Initiating statements
        log:printDebug("Initiating statements table");
        self.statements.put({AccountId: "A001", StatementId: "S001", Type: "RegularPeriodic", 
            StatementAmount: [{Type: "ClosingBalance", Amount: {}, CreditDebitIndicator: "Credit"}, 
            {Type: "PreviousClosingBalance", Amount: {}, CreditDebitIndicator: "Credit"}]});
        self.statements.put({AccountId: "A002", StatementId: "S002", Type: "RegularPeriodic", 
            StatementAmount: [{Type: "PreviousClosingBalance", Amount: {}, CreditDebitIndicator: "Credit"}]});
        self.statements.put({AccountId: "A001", StatementId: "S003", Type: "AccountClosure", 
            StatementAmount: [{Type: "PreviousClosingBalance", Amount: {}, CreditDebitIndicator: "Debit"}]});
        
        // Initiating transactions
        log:printDebug("Initiating transactions table");
        self.transactions.put({AccountId: "A001", TransactionId: "T001", Status: "Booked", TransactionReference: "Airbender club payment", 
            StatementReference: ["S001"], Amount: {}, CreditDebitIndicator: "Credit", ChargeAmount: {},
            BankTransactionCode: {Code: "BT", SubCode: "001"}, Balance: {CreditDebitIndicator: "Credit", Type: "InterimBooked", Amount: {}}});
        self.transactions.put({AccountId: "A002", TransactionId: "T002", Status: "Booked", TransactionReference: "Waterbender club payment", 
            StatementReference: ["S002"], Amount: {}, CreditDebitIndicator: "Dedit", ChargeAmount: {},
            BankTransactionCode: {Code: "BT", SubCode: "002"}});
        self.transactions.put({AccountId: "A001", TransactionId: "T003", Status: "Pending", TransactionReference: "Firebender club payment", 
            StatementReference: ["S001", "S002"], Amount: {}, CreditDebitIndicator: "Dedit", ChargeAmount: {},
            BankTransactionCode: {Code: "BT", SubCode: "003"}, Balance: {CreditDebitIndicator: "Dedit", Type: "InterimBooked", Amount: {}}});
    }

    public isolated function getAllAccounts() returns table<model:Account> key(AccountId) {
        return self.accounts;
    }

    public isolated function getAllBalances() returns table<model:Balance> key(AccountId, BalanceId) {
        return self.balances;
    }

    public isolated function getAllBeneficiaries() returns table<model:Beneficiary> key(AccountId, BeneficiaryId) {
        return self.beneficiaries;
    }

    public isolated function getAllDirectDebits() returns table<model:DirectDebit> key(AccountId, DirectDebitId) {
        return self.directDebits;
    }

    public isolated function getAllOffers() returns table<model:Offer> key(AccountId, OfferId) {
        return self.offers;
    }

    public isolated function getAllParties() returns table<model:Party> key(PartyId) {
        return self.parties;
    }

    public isolated function getAllProducts() returns table<model:Product> key(AccountId, ProductId) {
        return self.products;
    }

    public isolated function getAllScheduledPayments() returns table<model:ScheduledPayment> key(AccountId, ScheduledPaymentId) {
        return self.scheduledPayments;
    }

    public isolated function getAllStandingOrders() returns table<model:StandingOrder> key(AccountId, StandingOrderId) {
        return self.standingOrders;
    }

    public isolated function getAllStatements() returns table<model:Statement> key(AccountId, StatementId) {
        return self.statements;
    }

    public isolated function getAllTransactions() returns table<model:Transaction> key(AccountId, TransactionId) {
        return self.transactions;
    }
}
