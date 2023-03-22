// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/http;
import ballerina/log;
import ballerina/mime;

import bfsi_account_and_transaction_api.model;
import bfsi_account_and_transaction_api.repository;
import bfsi_account_and_transaction_api.util;

# Represents accounts API service methods.
public isolated client class AccountClient {

    # Get all accounts.
    #
    # + return - account list
    resource isolated function get accounts() returns model:AccountsResponse => {
        Data: repository:accounts.toArray(),
        Links: self.getLinks("/accounts"),
        Meta: {TotalPages: 1}
    };

    # Get an account by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account list or error
    resource isolated function get accounts/[string accountId]()
            returns model:AccountsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Account? account = repository:accounts[accountId];
        if account == () {
            log:printDebug(util:INVALID_ACCOUNT_ID, accountId = accountId);
            return error(util:INVALID_ACCOUNT_ID, ErrorCode = util:CODE_INVALID_ACCOUNT_ID);
        }

        return {
            Data: account,
            Links: self.getLinks("/accounts", accountId),
            Meta: {TotalPages: 1}
        };
    }

    # Get all balances.
    #
    # + return - all balances or error
    resource isolated function get balances() returns model:BalanceResponse => {
        Data: repository:balances.toArray(),
        Links: self.getLinks("/balances"),
        Meta: {TotalPages: 1}
    };

    # Get balances by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account balances list or error
    resource isolated function get accounts/[string accountId]/balances()
            returns model:BalanceResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Balance[] balances = from model:Balance i in repository:balances
            where i.AccountId == accountId
            select i;

        return {
            Data: balances,
            Links: self.getLinks(string `/accounts/${accountId}/balances`),
            Meta: {TotalPages: 1}
        };
    }

    # Get all beneficiaries.
    #
    # + return - account beneficiaries list or error
    resource isolated function get beneficiaries() returns model:BeneficiariesResponse => {
        Data: repository:beneficiaries.toArray(),
        Links: self.getLinks("/beneficiaries"),
        Meta: {TotalPages: 1}
    };

    # Get beneficiaries by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account beneficiaries list or error
    resource isolated function get accounts/[string accountId]/beneficiaries()
            returns model:BeneficiariesResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Beneficiary[] beneficiaries = from model:Beneficiary i in repository:beneficiaries
            where i.AccountId == accountId
            select i;

        return {
            Data: beneficiaries,
            Links: self.getLinks(string `/accounts/${accountId}/beneficiaries`),
            Meta: {TotalPages: 1}
        };
    }

    # Get all direct debits.
    #
    # + return - account direct debits list or error
    resource isolated function get direct\-debits() returns model:DirectDebitsResponse => {
        Data: repository:directDebits.toArray(),
        Links: self.getLinks("/direct-debits"),
        Meta: {TotalPages: 1}
    };

    # Get direct debits by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account direct debits list or error
    resource isolated function get accounts/[string accountId]/direct\-debits()
            returns model:DirectDebitsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:DirectDebit[] directDebits = from model:DirectDebit i in repository:directDebits
            where i.AccountId == accountId
            select i;

        return {
            Data: directDebits,
            Links: self.getLinks(string `/accounts/${accountId}/direct-debits`),
            Meta: {TotalPages: 1}
        };
    }

    # Get all offers.
    #
    # + return - account offers list or error
    resource isolated function get offers() returns model:OffersResponse => {
        Data: repository:offers.toArray(),
        Links: self.getLinks("/offers"),
        Meta: {TotalPages: 1}
    };

    # Get offers by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account offers list or error
    resource isolated function get accounts/[string accountId]/offers()
            returns model:OffersResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Offer[] offers = from model:Offer i in repository:offers
            where i.AccountId == accountId
            select i;
        return {
            Data: offers,
            Links: self.getLinks(string `/accounts/${accountId}/offers`),
            Meta: {TotalPages: 1}
        };
    }

    # Get all parties.
    #
    # + return - account parties list or error
    resource isolated function get parties() returns model:PartiesResponse => {
        Data: repository:parties.toArray(),
        Links: self.getLinks("/parties"),
        Meta: {TotalPages: 1}
    };

    # Get parties by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account parties list or error
    resource isolated function get accounts/[string accountId]/parties()
            returns model:PartiesResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Party[] parties = from model:Party party in repository:parties
            where party.Relationships?.Account?.Id == accountId
            select party;

        return {
            Data: parties,
            Links: self.getLinks(string `/accounts/${accountId}/parties`),
            Meta: {TotalPages: 1}
        };
    }

    # Get a party by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account parties list or error
    resource isolated function get accounts/[string accountId]/party()
            returns model:PartiesResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Party[] parties = from model:Party party in repository:parties
            where party.Relationships?.Account?.Id == accountId
            limit 1
            select party;

        if parties.length() > 0 {
            return {
                Data: parties[0],
                Links: self.getLinks(string `/accounts/${accountId}/party`),
                Meta: {TotalPages: 1}
            };
        }
        return error(util:INVALID_ACCOUNT_ID, ErrorCode = util:CODE_INVALID_ACCOUNT_ID);
    }

    # Get all products.
    #
    # + return - account products list or error
    resource isolated function get products() returns model:ProductsResponse => {
        Data: repository:products.toArray(),
        Links: self.getLinks("/products"),
        Meta: {TotalPages: 1}
    };

    # Get products by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account products list or error
    resource isolated function get accounts/[string accountId]/products()
            returns model:ProductsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Product[] product = from model:Product i in repository:products
            where i.AccountId == accountId
            select i;
        return {
            Data: product,
            Links: self.getLinks(string `/accounts/${accountId}/products`),
            Meta: {TotalPages: 1}
        };
    }

    # Get all scheduled payments.
    #
    # + return - account scheduled payments list or error
    resource isolated function get scheduled\-payments() returns model:ScheduledPaymentsResponse => {
        Data: repository:scheduledPayments.toArray(),
        Links: self.getLinks("/scheduled-payments"),
        Meta: {TotalPages: 1}
    };

    # Get scheduled payments by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account scheduled payments list or error
    resource isolated function get accounts/[string accountId]/scheduled\-payments()
            returns model:ScheduledPaymentsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:ScheduledPayment[] scheduledPayments = from model:ScheduledPayment i in repository:scheduledPayments
            where i.AccountId == accountId
            select i;
        return {
            Data: scheduledPayments,
            Links: self.getLinks(string `/accounts/${accountId}/scheduled-payments`),
            Meta: {TotalPages: 1}
        };
    }

    # Get all standing orders.
    #
    # + return - account standing orders list or error
    resource isolated function get standing\-orders() returns model:StandingOrdersResponse => {
        Data: repository:standingOrders.toArray(),
        Links: self.getLinks("/standing-orders"),
        Meta: {TotalPages: 1}
    };

    # Get standing orders by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account standing orders list or error
    resource isolated function get accounts/[string accountId]/standing\-orders()
            returns model:StandingOrdersResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:StandingOrder[] standingOrder = from model:StandingOrder i in repository:standingOrders
            where i.AccountId == accountId
            select i;
        return {
            Data: standingOrder,
            Links: self.getLinks(string `/accounts/${accountId}/standing-orders`),
            Meta: {TotalPages: 1}
        };
    }

    # Get all statements.
    #
    # + return - account statements list or error
    resource isolated function get statements() returns model:StatementsResponse => {
        Data: repository:statements.toArray(),
        Links: self.getLinks("/statements"),
        Meta: {TotalPages: 1}
    };

    # Get statements by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account statements list or error
    resource isolated function get accounts/[string accountId]/statements()
            returns model:StatementsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Statement[] statement = from model:Statement i in repository:statements
            where i.AccountId == accountId
            select i;

        return {
            Data: statement,
            Links: self.getLinks(string `/accounts/${accountId}/statement`),
            Meta: {TotalPages: 1}
        };
    }

    # Get statements by account ID and statement ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account statements list or error
    resource isolated function get accounts/[string accountId]/statements/[string statementId]()
            returns model:StatementsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);
        check self.validateStatementId(statementId);

        model:Statement? statement = repository:statements[accountId, statementId];
        if statement == () {
            log:printDebug(util:INVALID_STATEMENT_ID, statementId = statementId);
            return error(util:INVALID_STATEMENT_ID, ErrorCode = util:CODE_INVALID_STATEMENT_ID);
        }
        return {
            Data: statement,
            Links: self.getLinks(string `/accounts/${accountId}/statement`, statementId),
            Meta: {TotalPages: 1}
        };
    }

    # Get the statement file by account ID and statement ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account statements list or error
    resource isolated function get accounts/[string accountId]/statements/[string statementId]/file()
            returns http:Response|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);
        check self.validateStatementId(statementId);

        model:Statement? statement = repository:statements[accountId, statementId];
        if statement == () {
            log:printDebug(util:INVALID_STATEMENT_ID, statementId = statementId);
            return error(util:INVALID_STATEMENT_ID, ErrorCode = util:CODE_INVALID_STATEMENT_ID);
        } else {
            return self.readStatementFile(statement);
        }
    }

    private isolated function readStatementFile(model:Statement statement) returns http:Response {
        // Creates an enclosing entity.
        mime:Entity entity = new;
        http:Response outResponse = new;
        // todo: file read from resources directory is not possible yet. 
        // @see https://github.com/ballerina-platform/ballerina-spec/issues/1100
        // This file path is relative to where the Ballerina is running.
        // entity.setFileAsEntityBody(util:PATH_STATEMENT_FILE, contentType = mime:APPLICATION_PDF);

        // Creates an array to hold the parent part and set it to the response.
        outResponse.setBodyParts([entity], contentType = mime:MULTIPART_FORM_DATA);
        outResponse.setHeader("Content-Disposition",
            "attachment; filename=statement_" + statement.StatementId + ".pdf");
        return outResponse;
    }

    # Get all transactions.
    #
    # + return - account transactions list or error
    resource isolated function get transactions() returns model:TransactionsResponse => {
        Data: repository:transactions.toArray(),
        Links: self.getLinks("/transactions"),
        Meta: {TotalPages: 1}
    };

    # Get transactions by account ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account transactions list or error
    resource isolated function get accounts/[string accountId]/transactions()
            returns model:TransactionsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);

        model:Transaction[] transactions = from model:Transaction i in repository:transactions
            where i.AccountId == accountId
            select i;

        return {
            Data: transactions,
            Links: self.getLinks(string `/accounts/${accountId}/transactions`),
            Meta: {TotalPages: 1}
        };
    }

    # Get transactions by account ID and statement ID.
    #
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account transactions list or error
    resource isolated function get accounts/[string accountId]/statements/[string statementId]/transactions()
            returns model:TransactionsResponse|model:InvalidResourceIdError {
        check self.validateAccountId(accountId);
        check self.validateStatementId(statementId);

        model:Transaction[] accountTransactions = from model:Transaction i in repository:transactions
            where i.AccountId == accountId
            select i;

        return {
            Data: self.filterTransactionByStatementId(accountTransactions, statementId),
            Links: self.getLinks(string `/accounts/${accountId}/statement/${statementId}/transactions`),
            Meta: {TotalPages: 1}
        };
    }

    # Filter the transactions by statement ID.
    #
    # + accountTransactions - Account transactions
    # + statementId - Statement ID
    # + return - Filtered transactions
    private isolated function filterTransactionByStatementId(model:Transaction[] accountTransactions,
            string statementId) returns model:Transaction[] =>
        from model:Transaction tran in accountTransactions
            let string[]? statementReference = tran.StatementReference
            where statementReference !is () && statementReference.indexOf(statementId, 0) != ()
            select tran;

    # Get the Link matching to the path and consent id
    #
    # + path - Path of the request
    # + id - Consent Id of the request
    # + return - Link
    private isolated function getLinks(string path, string id = "") returns model:Links => {
        Self: string `https://api.alphabank.com/open-banking/v3.1/aisp${path}/${id}`
    };

    # Validates the account ID.
    #
    # + accountId - Account ID to be validated
    # + return - `InvalidResourceIdError` if the account ID is invalid
    private isolated function validateAccountId(string accountId) returns model:InvalidResourceIdError? {
        if util:isEmpty(accountId) {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode = util:CODE_EMPTY_ACCOUNT_ID);
        }
    }

    # Validates the statement ID.
    #
    # + statementId - Statement ID to be validated
    # + return - `InvalidResourceIdError` if the statement ID is invalid
    private isolated function validateStatementId(string statementId) returns model:InvalidResourceIdError? {
        if util:isEmpty(statementId) {
            log:printDebug(util:EMPTY_STATEMENT_ID);
            return error(util:EMPTY_STATEMENT_ID, ErrorCode = util:CODE_EMPTY_STATEMENT_ID);
        }
    }
}
