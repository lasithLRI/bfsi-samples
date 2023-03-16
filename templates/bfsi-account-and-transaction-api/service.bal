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

//This import represents the backend service of your bank
import bfsi_account_and_transaction_api.'client;
import bfsi_account_and_transaction_api.interceptor;
import bfsi_account_and_transaction_api.model;

// Request interceptors handle HTTP requests globally 
final interceptor:RequestInterceptor requestInterceptor = new;
final interceptor:RequestErrorInterceptor requestErrorInterceptor = new;
final interceptor:ResponseErrorInterceptor ResponseErrorInterceptor = new;
final http:ListenerConfiguration config = {
    interceptors: [requestInterceptor, requestErrorInterceptor, ResponseErrorInterceptor]
};
listener http:Listener interceptorListener = new (9090, config);

isolated service / on interceptorListener {
    private final 'client:AccountClient accountClient = new ();

    # Get all accounts.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - account list or error
    isolated resource function get accounts(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:AccountsResponse {
        log:printInfo("Retrieving all accounts");
        return self.accountClient->/accounts;
    }

    # Get an account by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account list or error
    isolated resource function get accounts/[string accountId](@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:AccountsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving an account for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId];
    }

    # Get balances by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account balances list or error
    isolated resource function get accounts/[string accountId]/balances(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:BalanceResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving balances for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/balances;
    }

    # Get beneficiaries by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account beneficiaries list or error
    isolated resource function get accounts/[string accountId]/beneficiaries(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:BeneficiariesResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving beneficiaries for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/beneficiaries;
    }

    # Get direct debits by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account direct debits list or error
    isolated resource function get accounts/[string accountId]/'direct\-debits(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
            returns model:DirectDebitsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving direct debits for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/direct\-debits;
    }

    # Get offers by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account offers list or error
    isolated resource function get accounts/[string accountId]/offers(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:OffersResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving offers for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/offers;
    }

    # Get parties by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account parties list or error
    isolated resource function get accounts/[string accountId]/parties(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:PartiesResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving parties for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/parties;
    }

    # Get a party by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account parties list or error
    isolated resource function get accounts/[string accountId]/party(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:PartiesResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving a party for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/party;
    }

    # Get products by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account products list or error
    isolated resource function get accounts/[string accountId]/product(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:ProductsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving products for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/products;
    }

    # Get scheduled payments by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account scheduled payments list or error
    isolated resource function get accounts/[string accountId]/'scheduled\-payments(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
            returns model:ScheduledPaymentsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving scheduled payments for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/scheduled\-payments;
    }

    # Get standing orders by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account standing orders list or error
    isolated resource function get accounts/[string accountId]/'standing\-orders(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
            returns model:StandingOrdersResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving standing orders for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/standing\-orders;
    }

    # Get statements by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account statements list or error
    isolated resource function get accounts/[string accountId]/statements(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent, string? fromStatementDateTime, string? toStatementDateTime)
            returns model:StatementsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving statements for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/statements;
    }

    # Get statements by account ID and statement ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account statements list or error
    isolated resource function get accounts/[string accountId]/statements/[string statementId](
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
            returns model:StatementsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving statements for account Id and statement Id", accoundId = accountId,
            statementId = statementId);
        return self.accountClient->/accounts/[accountId]/statements/[statementId];
    }

    # Get the statement file by account ID and statement ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account statements list or error
    isolated resource function get accounts/[string accountId]/statements/[string statementId]/file(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
            returns http:Response|model:InvalidResourceIdError {
        log:printInfo("Retrieving statement file for account Id and statement Id", accoundId = accountId,
            statementId = statementId);
        return self.accountClient->/accounts/[accountId]/statements/[statementId]/file;
    }

    # Get transactions by account ID and statement ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account transactions list or error
    isolated resource function get accounts/[string accountId]/statements/[string statementId]/transactions(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
            returns model:TransactionsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving transactions for account Id and statement Id", accoundId = accountId,
            statementId = statementId);
        return self.accountClient->/accounts/[accountId]/statements/[statementId]/transactions;
    }

    # Get transactions by account ID.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account transactions list or error
    isolated resource function get accounts/[string accountId]/transactions(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent, string? fromBookingDateTime, string? toBookingDateTime)
            returns model:TransactionsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving transactions for account Id", accoundId = accountId);
        return self.accountClient->/accounts/[accountId]/transactions;
    }

    # Get all balances.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all balances or error
    isolated resource function get balances(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:BalanceResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all balances");
        return self.accountClient->/balances;
    }

    # Get all beneficiaries.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all beneficiaries or error
    isolated resource function get beneficiaries(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:BeneficiariesResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all beneficiaries");
        return self.accountClient->/beneficiaries;
    }

    # Get all direct debits.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all direct debits or error
    isolated resource function get 'direct\-debits(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:DirectDebitsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all direct debits");
        return self.accountClient->/direct\-debits;
    }

    # Get all offers.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all offers or error
    isolated resource function get offers(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:OffersResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all offers");
        return self.accountClient->/offers;
    }

    # Get all parties.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all parties or error
    isolated resource function get party(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent) returns model:PartiesResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all parties");
        return self.accountClient->/parties;
    }

    # Get all products.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all products or error
    isolated resource function get products(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:ProductsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all products");
        return self.accountClient->/products;
    }

    # Get all scheduled payments.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all scheduled payments or error
    isolated resource function get 'scheduled\-payments(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:ScheduledPaymentsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all scheduled payments");
        return self.accountClient->/scheduled\-payments;
    }

    # Get all standing orders.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all standing orders or error
    isolated resource function get 'standing\-orders(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
            returns model:StandingOrdersResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all standing orders");
        return self.accountClient->/standing\-orders;
    }

    # Get all statements.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all statements or error
    isolated resource function get statements(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            string? fromStatementDateTime, string? toStatementDateTime, @http:Header string? x\-customer\-user\-agent)
            returns model:StatementsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all statements");
        return self.accountClient->/statements;
    }

    # Get all transactions.
    #
    # + x\-fapi\-auth\-date - the date and time at which the TPP attempted to access the resource
    # + x\-fapi\-customer\-ip\-address - the IP address of the TPP
    # + x\-fapi\-interaction\-id - an unique identifier for the interaction between TPP and bank
    # + x\-customer\-user\-agent - the user agent of the TPP
    # + return - all transactions or error
    isolated resource function get transactions(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent, string? fromBookingDateTime, string? toBookingDateTime)
            returns model:TransactionsResponse|model:InvalidResourceIdError {
        log:printInfo("Retrieving all transactions");
        return self.accountClient->/transactions;
    }
}
