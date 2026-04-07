/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.demo.service;

import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.models.Account;
import com.wso2.openbanking.demo.models.Bank;
import com.wso2.openbanking.demo.models.Transaction;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.reflections.Reflections.log;

/** AccountService implementation. */
public final class AccountService {

    private final HttpTlsClient client;
    private final OAuthTokenService oauthService;
    private String accessToken;
    private String currentConsentId;

    private static final DateTimeFormatter ISO_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String FIELD_ACCOUNT = "Account";
    private static final String FIELD_AMOUNT = "Amount";
    private static final String ACCOUNTS_PATH = "/accounts/";


    private AccountService(HttpTlsClient client,
                           OAuthTokenService oauthService) {
        this.client = client;
        this.oauthService = oauthService;
    }

    public static AccountService create(HttpTlsClient client)
            throws BankInfoLoadException {
        try {
            OAuthTokenService oauthService = new OAuthTokenService(client);
            return new AccountService(client, oauthService);
        } catch (GeneralSecurityException | IOException e) {
            throw new BankInfoLoadException(
                    "Failed to initialize OAuth token service: " + e.getMessage(), e);
        } catch (SSLContextCreationException e) {
            throw new BankInfoLoadException(
                    "Failed to create TLS client copy during OAuth service initialization: " + e.getMessage(), e);
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<Account> createBankInContext() throws IOException {
        List<String> fetchedAccountIds = fetchAccountIds();
        return fetchAccountsWithTransactions(fetchedAccountIds);
    }

    /**
     * Initiates the account access consent flow and returns an authorization URL.
     *
     * @return the OAuth authorization URL for the user to grant account access consent
     * @throws Exception if token retrieval, consent initialization, or authorization fails
     */
    public String processAddAccount() throws Exception {
        String addAccountUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents";
        String consentBody = createAccountConsentBody();
        String token = oauthService.getToken("accounts openid");
        String consentResponse = oauthService.initializeConsent(token, consentBody, addAccountUrl);
        currentConsentId = new JSONObject(consentResponse).getJSONObject("Data").getString("ConsentId");
        return oauthService.authorizeConsent(consentResponse, "accounts openid");
    }

    private List<String> fetchAccountIds() throws IOException {
        String response = client.getWithAuth(
                ConfigLoader.getAccountBaseUrl() + "/accounts", this.accessToken);
        JSONArray accountsArray = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray(FIELD_ACCOUNT);
        List<String> accountIds = new ArrayList<>();
        for (int i = 0; i < accountsArray.length(); i++) {
            accountIds.add(accountsArray.getJSONObject(i).getString("AccountId"));
        }
        return accountIds;
    }

    /**
     * Fetches account details, balance, and transactions for each of the given account IDs.
     *
     * @param accountIds the list of account IDs to fetch data for
     * @return a list of {@link Account} objects populated with name, balance, transaction, bank name, and consent ID
     * @throws IOException if any API call to fetch account details fails
     */
    private List<Account> fetchAccountsWithTransactions(List<String> accountIds) throws IOException {
        List<Account> accounts = new ArrayList<>();
        for (String accountId : accountIds) {
            String accountName = fetchAccountName(accountId);
            double balance = fetchAccountBalance(accountId);
            List<Transaction> transactions = fetchAccountTransactions(accountId);
            Account account = new Account(accountId, accountName, balance, transactions);
            account.setBank(ConfigLoader.getMockBankName());
            account.setConsentId(currentConsentId);
            accounts.add(account);
        }
        currentConsentId = null;
        return accounts;
    }

    private String fetchAccountName(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + ACCOUNTS_PATH + accountId;
        String response = client.getWithAuth(url, this.accessToken);
        JSONObject accountDataNode = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray(FIELD_ACCOUNT)
                .getJSONObject(0);
        if (accountDataNode.has(FIELD_ACCOUNT)) {
            return accountDataNode.getJSONArray(FIELD_ACCOUNT)
                    .getJSONObject(0)
                    .optString("Name", "Open Banking Account");
        }
        return accountDataNode.optString("Nickname", "Standard Account");
    }

    private double fetchAccountBalance(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + ACCOUNTS_PATH + accountId + "/balances";
        String response = client.getWithAuth(url, this.accessToken);
        String amount = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Balance")
                .getJSONObject(0)
                .getJSONObject(FIELD_AMOUNT)
                .getString(FIELD_AMOUNT);
        return Double.parseDouble(amount);
    }

    private List<Transaction> fetchAccountTransactions(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + ACCOUNTS_PATH + accountId + "/transactions";
        String response = client.getWithAuth(url, this.accessToken);
        JSONObject root = new JSONObject(response);
        if (!root.has("Data") || !root.getJSONObject("Data").has("Transaction")) {
            return new ArrayList<>();
        }
        JSONArray transactionsArray = root.getJSONObject("Data").getJSONArray("Transaction");
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsArray.length(); i++) {
            transactions.add(parseTransaction(transactionsArray.getJSONObject(i), accountId));
        }
        return transactions;
    }

    private Transaction parseTransaction(JSONObject txn, String accountId) {
        Transaction transaction = new Transaction();
        transaction.setId(txn.getString("TransactionId"));
        transaction.setDate(convertIsoDateTimeToDate(txn.getString("BookingDateTime")));
        transaction.setReference(txn.getString("TransactionInformation"));
        JSONObject amountObj = txn.getJSONObject(FIELD_AMOUNT);
        transaction.setAmount(amountObj.getString(FIELD_AMOUNT));
        transaction.setCurrency(amountObj.getString("Currency"));
        transaction.setCreditDebitStatus(txn.getString("CreditDebitIndicator"));
        transaction.setAccount(accountId);
        return transaction;
    }

    private String convertIsoDateTimeToDate(String isoDateTime) {
        try {
            return ZonedDateTime.parse(isoDateTime, ISO_DATETIME_FORMATTER).format(DATE_FORMATTER);
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .format(DATE_FORMATTER);
            } catch (Exception e2) {
                if (isoDateTime.length() >= 10) {
                    return isoDateTime.substring(0, 10);
                }
                return isoDateTime;
            }
        }
    }

    /**
     * Creates the JSON request body for an account access consent with predefined permissions and date ranges.
     *
     * @return a JSON string representing the account consent request body
     */
    private String createAccountConsentBody() {
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.of("+05:30"));
        JSONObject permissions = new JSONObject()
                .put("Permissions", new JSONArray()
                        .put("ReadAccountsBasic")
                        .put("ReadAccountsDetail")
                        .put("ReadBalances")
                        .put("ReadTransactionsDetail"))
                .put("ExpirationDateTime", now.plusDays(90).format(ISO_DATETIME_FORMATTER))
                .put("TransactionFromDateTime", now.minusDays(30).format(ISO_DATETIME_FORMATTER))
                .put("TransactionToDateTime", now.format(ISO_DATETIME_FORMATTER));
        return new JSONObject()
                .put("Data", permissions)
                .put("Risk", new JSONObject())
                .toString();
    }

    /**
     * Revokes the account access consent for the given account by calling the consent revocation endpoint.
     *
     * @param accountId the unique identifier of the account whose consent is to be revoked
     * @param bankName  the name of the bank associated with the account
     * @param consentId the unique identifier of the consent to be revoked
     * @return {@code true} if revocation was successful, {@code false} otherwise
     * @throws Exception if token retrieval or the revocation API call fails
     */
    public boolean revokeAccountConsent(String accountId, String bankName, String consentId) throws Exception {
        log.info("[DELETE] Attempting to revoke consent for accountId: {}, bankName: {}, {}",
                accountId, bankName, consentId);
        String tokenResponse = oauthService.getToken("accounts openid");
        String token = new JSONObject(tokenResponse).getString("access_token");
        String revokeUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents/" + consentId;
        log.info("[DELETE] Calling revoke URL: {}", revokeUrl);
        boolean success = client.deleteWithAuth(revokeUrl, token);
        log.info("[DELETE] OB backend revocation success: {}", success);
        return success;
    }
}
