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

import wso2.bfsi.demo.backend.model;
import wso2.bfsi.demo.backend.repository;
import wso2.bfsi.demo.backend.util;

#Represents accounts API service methods.
public class AccountService {
    
    private final string STATEMENT_FILE_PATH = "./resources/statement.pdf";

    private repository:AccountsRepository repository = new();
    
    # Get all accounts.
    # 
    # + return - account list
    public isolated function getAccounts() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllAccounts().toArray(), "/accounts/");
    }

    # Get an account by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account list or error
    public isolated function getAccount(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        } 
        model:Account? account = self.repository.getAllAccounts()[accountId];
        if account == () {
            log:printDebug(util:INVALID_ACCOUNT_ID, accountId = accountId);
            return error(util:INVALID_ACCOUNT_ID, ErrorCode=util:CODE_INVALID_ACCOUNT_ID);
        } 
        return self.generateResponse(account, "/accounts/", accountId);
    }

    # Get all balances.
    # 
    # + return - all balances or error
    public isolated function getBalances() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllBalances().toArray(), "/balances/");
    }

    # Get balances by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account balances list or error
    public isolated function getAccountBalances(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Balance[] balances = from model:Balance i in self.repository.getAllBalances() 
                                    where i.AccountId == accountId 
                                    select i;
        return self.generateResponse(balances, "/accounts/" + accountId + "/balances/");
    }

    # Get all beneficiaries.
    # 
    # + return - account beneficiaries list or error
    public isolated function getBeneficiaries() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllBeneficiaries().toArray(), "/beneficiaries/");
    }

    # Get beneficiaries by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account beneficiaries list or error
    public isolated function getAccountBeneficiaries(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Beneficiary[] beneficiaries = from model:Beneficiary i in self.repository.getAllBeneficiaries() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(beneficiaries, "/accounts/" + accountId + "/beneficiaries/");
    }

    # Get all direct debits.
    # 
    # + return - account direct debits list or error
    public isolated function getDirectDebits() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllDirectDebits().toArray(), "/direct-debits/");
    }

    # Get direct debits by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account direct debits list or error
    public isolated function getAccountDirectDebits(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:DirectDebit[] directDebits = from model:DirectDebit i in self.repository.getAllDirectDebits() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(directDebits, "/accounts/" + accountId + "/direct-debits/");
    }

    # Get all offers.
    # 
    # + return - account offers list or error
    public isolated function getOffers() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllOffers().toArray(), "/offers/");
    }

    # Get offers by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account offers list or error
    public isolated function getAccountOffers(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Offer[] offers = from model:Offer i in self.repository.getAllOffers() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(offers, "/accounts/" + accountId + "/offers/");
    }

    # Get all parties.
    # 
    # + return - account parties list or error
    public isolated function getParties() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllParties().toArray(), "/parties/");
    }

    # Get parties by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account parties list or error
    public isolated function getAccountParties(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Party[] parties = [];
        foreach model:Party party in self.repository.getAllParties().toArray() {
            model:PartyRelationship pr = <model:PartyRelationship> party.Relationships;
            model:PartyRelationshipAccount pra = <model:PartyRelationshipAccount> pr.Account;
            if (pra.Id == accountId) {
                parties.push(party);
            }
        }

        return self.generateResponse(parties, "/accounts/" + accountId + "/parties/");
    }

    # Get a party by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account parties list or error
    public isolated function getAccountParty(string accountId) returns model:HateoasResponse|error {
        
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        foreach model:Party party in self.repository.getAllParties().toArray() {
            model:PartyRelationship pr = <model:PartyRelationship> party.Relationships;
            model:PartyRelationshipAccount pra = <model:PartyRelationshipAccount> pr.Account;
            if (pra.Id == accountId) {
                return self.generateResponse(party, "/accounts/" + accountId + "/party/");
            }
        }
        return error(util:INVALID_ACCOUNT_ID, ErrorCode=util:CODE_INVALID_ACCOUNT_ID);
    }

    # Get all products.
    # 
    # + return - account products list or error
    public isolated function getProducts() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllProducts().toArray(), "/products/");
    }

    # Get products by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account products list or error
    public isolated function getAccountProducts(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Product[] products = from model:Product i in self.repository.getAllProducts() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(products, "/accounts/" + accountId + "/products/");
    }

    # Get all scheduled payments.
    # 
    # + return - account scheduled payments list or error
    public isolated function getScheduledPayments() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllScheduledPayments().toArray(), "/scheduled-payments/");
    }

    # Get scheduled payments by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account scheduled payments list or error
    public isolated function getAccountScheduledPayments(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:ScheduledPayment[] scheduledPayments = from model:ScheduledPayment i in self.repository.getAllScheduledPayments() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(scheduledPayments, "/accounts/" + accountId + "/scheduled-payments/");
    }

    # Get all standing orders.
    # 
    # + return - account standing orders list or error
    public isolated function getStandingOrders() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllStandingOrders().toArray(), "/standing-orders/");
    }

    # Get standing orders by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account standing orders list or error
    public isolated function getAccountStandingOrders(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:StandingOrder[] standingOrders = from model:StandingOrder i in self.repository.getAllStandingOrders() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(standingOrders, "/accounts/" + accountId + "/standing-orders/");
    }

    # Get all statements.
    # 
    # + return - account statements list or error
    public isolated function getStatements() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllStatements().toArray(), "/statements/");
    }

    # Get statements by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account statements list or error
    public isolated function getAccountStatements(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Statement[] statements = from model:Statement i in self.repository.getAllStatements() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(statements, "/accounts/" + accountId + "/statements/");
    }

    # Get statements by account ID and statement ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account statements list or error
    public isolated function getAccountStatement(string accountId, string statementId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        } else if statementId == "" {
            log:printDebug(util:EMPTY_STATEMENT_ID);
            return error(util:EMPTY_STATEMENT_ID, ErrorCode=util:CODE_EMPTY_STATEMENT_ID);
        }
        model:Statement? statement = self.repository.getAllStatements()[accountId, statementId];
        if statement == () {
            log:printDebug(util:INVALID_STATEMENT_ID, statementId=statementId);
                return error(util:INVALID_STATEMENT_ID, ErrorCode=util:CODE_INVALID_STATEMENT_ID);
        } 
        return self.generateResponse(statement, "/accounts/" + accountId + "/statements/", statementId);
    }


    # Get the statement file by account ID and statement ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account statements list or error
    public isolated function getAccountStatementFile(string accountId, string statementId) returns http:Response|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        } else if statementId == "" {
            log:printDebug(util:EMPTY_STATEMENT_ID);
            return error(util:EMPTY_STATEMENT_ID, ErrorCode=util:CODE_EMPTY_STATEMENT_ID);
        }
        model:Statement? statement = self.repository.getAllStatements()[accountId, statementId];
        if statement == () {
            log:printDebug(util:INVALID_STATEMENT_ID, statementId=statementId);
                return error(util:INVALID_STATEMENT_ID, ErrorCode=util:CODE_INVALID_STATEMENT_ID);
        } else {
            return self.readStatementFile(statement);
        } 
    }

    private isolated function readStatementFile(model:Statement statement) returns http:Response|error {
        do {
	        // Checks whether the file exists on the provided path.
	        //boolean fileExists = check file:test(self.STATEMENT_FILE_PATH, file:EXISTS);
            //if (fileExists) {
                // Creates an enclosing entity.
                mime:Entity entity = new;
                http:Response outResponse = new;
                // todo: file read from resources directory is not possible yet. @see https://github.com/ballerina-platform/ballerina-spec/issues/1100
                // This file path is relative to where the Ballerina is running.
                // entity.setFileAsEntityBody(self.STATEMENT_FILE_PATH, contentType = mime:APPLICATION_PDF);
                
                // Creates an array to hold the parent part and set it to the response.
                outResponse.setBodyParts([entity], contentType = mime:MULTIPART_FORM_DATA);
                outResponse.setHeader("Content-Disposition", "attachment; filename=statement_" + statement.StatementId + ".pdf");
                return outResponse;
            //}
            //return {ErrorCode: util:CODE_STATEMENT_FILE_MISSING, Message: "File generation failed"};
        } on fail var e {
        	log:printError("failed to read statements file. Caused by, ", e);
            return error("File generation failed. Try again!", ErrorCode=util:CODE_FILE_GENERATION_FAILED);
        }
    }

    # Get all transactions.
    # 
    # + return - account transactions list or error
    public isolated function getTransactions() returns model:HateoasResponse {
        return self.generateResponse(self.repository.getAllTransactions().toArray(), "/transactions/");
    }

    # Get transactions by account ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + return - account transactions list or error
    public isolated function getAccountTransactions(string accountId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Transaction[] transactions = from model:Transaction i in self.repository.getAllTransactions() 
                                        where i.AccountId == accountId 
                                        select i;
        return self.generateResponse(transactions, "/accounts/" + accountId + "/transactions/");
    }

    # Get transactions by account ID and statement ID.
    # 
    # + accountId - the account ID path parameter, Try A001 as the default value
    # + statementId - the statement ID path parameter, Try S001 as the default value
    # + return - account transactions list or error
    public isolated function getAccountAndStatementTransactions(string accountId, string statementId) returns model:HateoasResponse|error {
        if accountId == "" {
            log:printDebug(util:EMPTY_ACCOUNT_ID);
            return error(util:EMPTY_ACCOUNT_ID, ErrorCode=util:CODE_EMPTY_ACCOUNT_ID);
        }
        model:Transaction[] AccountTransactions = from model:Transaction i in self.repository.getAllTransactions() 
                                        where i.AccountId == accountId 
                                        select i;
        model:Transaction[] transactions = AccountTransactions
            .filter(tran => !(tran.StatementReference is ()) && ((<string[]>tran?.StatementReference).indexOf(statementId, 0) != ()));
        
        return self.generateResponse(transactions, "/accounts/" + accountId + "/statements/" + statementId + "/transactions/");
    }

    private isolated function generateResponse(anydata data, string path, string id="") returns model:HateoasResponse {
        model:HateoasResponse response = {Data:  data};
        if id != "" {
            response.Links = { Self: "https://api.alphabank.com/open-banking/v3.1/aisp" + path + id };
            response.Meta = { TotalPages: 1 };
        }

        return response; 
    }
}
