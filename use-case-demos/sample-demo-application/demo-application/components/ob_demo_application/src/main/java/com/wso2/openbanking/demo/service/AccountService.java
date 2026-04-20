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

import com.wso2.openbanking.demo.constants.OpenBankingConstants;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.models.Account;
import com.wso2.openbanking.demo.models.Transaction;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.reflections.Reflections.log;

/** Handles account consent, data fetching, and consent revocation via the Open Banking API. */
public final class AccountService {

    private final HttpTlsClient client;
    private final OAuthTokenService oauthService;
    private String accessToken;
    private String currentConsentId;

    private static final DateTimeFormatter ISO_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Creates an AccountService with the given HTTP client and OAuth service.
     *
     * @param client       TLS HTTP client for making API calls
     * @param oauthService service for obtaining and managing OAuth tokens
     */
    private AccountService(HttpTlsClient client, OAuthTokenService oauthService) {
        this.client = client;
        this.oauthService = oauthService;
    }

    /**
     * Creates an AccountService instance using the given TLS client.
     *
     * @param client TLS HTTP client used for API calls
     * @return new AccountService instance
     * @throws BankInfoLoadException if OAuth or TLS service setup fails
     */
    public static AccountService create(HttpTlsClient client) throws BankInfoLoadException {
        try {
            OAuthTokenService oauthService = new OAuthTokenService(client);
            return new AccountService(client, oauthService);
        } catch (GeneralSecurityException | IOException e) {
            throw new BankInfoLoadException("OAuth token service initialization failed.", e);
        } catch (SSLContextCreationException e) {
            throw new BankInfoLoadException("TLS client initialization failed during OAuth service setup.", e);
        }
    }

    /**
     * Sets the OAuth access token for authenticated API requests.
     *
     * @param accessToken valid OAuth access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Fetches all accounts and their transactions for the current consent.
     *
     * @return list of accounts with transaction data
     * @throws IOException if any API call fails
     */
    public List<Account> createBankInContext() throws IOException {
        List<String> fetchedAccountIds = fetchAccountIds();
        return fetchAccountsWithTransactions(fetchedAccountIds);
    }

    /**
     * Creates an account consent and returns the OAuth authorization URL.
     *
     * @return authorization redirect URL for the account consent flow
     * @throws Exception if consent creation or authorization fails
     */
    public String processAddAccount() throws Exception {
        String addAccountUrl = ConfigLoader.getAccountBaseUrl() + OpenBankingConstants.PATH_ACCOUNT_CONSENTS;
        String consentBody = createAccountConsentBody();
        String token = oauthService.getToken(OpenBankingConstants.SCOPE_ACCOUNTS);
        String consentResponse = oauthService.initializeConsent(token, consentBody, addAccountUrl);
        currentConsentId = new JSONObject(consentResponse)
                .getJSONObject(OpenBankingConstants.FIELD_DATA)
                .getString(OpenBankingConstants.FIELD_CONSENT_ID);
        return oauthService.authorizeConsent(consentResponse, OpenBankingConstants.SCOPE_ACCOUNTS);
    }

    /**
     * Fetches the list of account IDs available under the current access token.
     *
     * @return list of account ID strings
     * @throws IOException if the API call fails
     */
    private List<String> fetchAccountIds() throws IOException {
        String response = client.getWithAuth(
                ConfigLoader.getAccountBaseUrl() + OpenBankingConstants.PATH_ACCOUNTS.stripTrailing(),
                this.accessToken);
        JSONArray accountsArray = new JSONObject(response)
                .getJSONObject(OpenBankingConstants.FIELD_DATA)
                .getJSONArray(OpenBankingConstants.FIELD_ACCOUNT);
        List<String> accountIds = new ArrayList<>();
        for (int i = 0; i < accountsArray.length(); i++) {
            accountIds.add(accountsArray.getJSONObject(i).getString(OpenBankingConstants.FIELD_ACCOUNT_ID));
        }
        return accountIds;
    }

    /**
     * Fetches full account details and transactions for each account ID.
     *
     * @param accountIds list of account IDs to fetch
     * @return list of Account objects with transactions
     * @throws IOException if any API call fails
     */
    private List<Account> fetchAccountsWithTransactions(List<String> accountIds) throws IOException {
        List<Account> accounts = new ArrayList<>();
        for (String accountId : accountIds) {
            String accountName = fetchAccountName(accountId);
            double balance = fetchAccountBalance(accountId);
            List<Transaction> transactions = fetchAccountTransactions(accountId);
            Account account = new Account(accountId, accountName, balance, transactions);
            account.setConsentId(currentConsentId);
            accounts.add(account);
        }
        currentConsentId = null;
        return accounts;
    }

    /**
     * Fetches the display name of an account by its ID.
     *
     * @param accountId account ID to look up
     * @return account name or a default value if not found
     * @throws IOException if the API call fails
     */
    private String fetchAccountName(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + OpenBankingConstants.PATH_ACCOUNTS + accountId;
        String response = client.getWithAuth(url, this.accessToken);
        JSONObject accountDataNode = new JSONObject(response)
                .getJSONObject(OpenBankingConstants.FIELD_DATA)
                .getJSONArray(OpenBankingConstants.FIELD_ACCOUNT)
                .getJSONObject(0);
        if (accountDataNode.has(OpenBankingConstants.FIELD_ACCOUNT)) {
            return accountDataNode.getJSONArray(OpenBankingConstants.FIELD_ACCOUNT)
                    .getJSONObject(0)
                    .optString(OpenBankingConstants.FIELD_NAME, OpenBankingConstants.DEFAULT_ACCOUNT_NAME);
        }
        return accountDataNode.optString(OpenBankingConstants.FIELD_NICKNAME, OpenBankingConstants.DEFAULT_STANDARD_ACCOUNT);
    }

    /**
     * Fetches the current balance of an account by its ID.
     *
     * @param accountId account ID to look up
     * @return account balance as a double
     * @throws IOException if the API call fails
     */
    private double fetchAccountBalance(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + OpenBankingConstants.PATH_ACCOUNTS
                + accountId + OpenBankingConstants.PATH_BALANCES;
        String response = client.getWithAuth(url, this.accessToken);
        String amount = new JSONObject(response)
                .getJSONObject(OpenBankingConstants.FIELD_DATA)
                .getJSONArray(OpenBankingConstants.FIELD_BALANCE)
                .getJSONObject(0)
                .getJSONObject(OpenBankingConstants.FIELD_AMOUNT)
                .getString(OpenBankingConstants.FIELD_AMOUNT);
        return Double.parseDouble(amount);
    }

    /**
     * Fetches the transaction list for an account by its ID.
     *
     * @param accountId account ID to fetch transactions for
     * @return list of Transaction objects, or empty list if none found
     * @throws IOException if the API call fails
     */
    private List<Transaction> fetchAccountTransactions(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + OpenBankingConstants.PATH_ACCOUNTS
                + accountId + OpenBankingConstants.PATH_TRANSACTIONS;
        String response = client.getWithAuth(url, this.accessToken);
        JSONObject root = new JSONObject(response);
        if (!root.has(OpenBankingConstants.FIELD_DATA)
                || !root.getJSONObject(OpenBankingConstants.FIELD_DATA)
                .has(OpenBankingConstants.FIELD_TRANSACTION)) {
            return new ArrayList<>();
        }
        JSONArray transactionsArray = root.getJSONObject(OpenBankingConstants.FIELD_DATA)
                .getJSONArray(OpenBankingConstants.FIELD_TRANSACTION);
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsArray.length(); i++) {
            transactions.add(parseTransaction(transactionsArray.getJSONObject(i), accountId));
        }
        return transactions;
    }

    /**
     * Parses a JSON transaction object into a Transaction model.
     *
     * @param txn       JSON object representing a single transaction
     * @param accountId account ID the transaction belongs to
     * @return populated Transaction object
     */
    private Transaction parseTransaction(JSONObject txn, String accountId) {
        Transaction transaction = new Transaction();
        transaction.setId(txn.getString(OpenBankingConstants.FIELD_TRANSACTION_ID));
        transaction.setDate(convertIsoDateTimeToDate(txn.getString(OpenBankingConstants.FIELD_BOOKING_DATE_TIME)));
        transaction.setReference(txn.getString(OpenBankingConstants.FIELD_TRANSACTION_INFORMATION));
        JSONObject amountObj = txn.getJSONObject(OpenBankingConstants.FIELD_AMOUNT);
        transaction.setAmount(amountObj.getString(OpenBankingConstants.FIELD_AMOUNT));
        transaction.setCurrency(amountObj.getString(OpenBankingConstants.FIELD_CURRENCY));
        transaction.setCreditDebitStatus(txn.getString(OpenBankingConstants.FIELD_CREDIT_DEBIT_INDICATOR));
        transaction.setAccount(accountId);
        return transaction;
    }

    /**
     * Converts an ISO datetime string to a plain date string (yyyy-MM-dd).
     *
     * @param isoDateTime ISO 8601 datetime string to convert
     * @return date string in yyyy-MM-dd format, or the original value on failure
     */
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
     * Builds the JSON request body for creating an account consent.
     *
     * @return account consent request body as a JSON string
     */
    private String createAccountConsentBody() {
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.of(OpenBankingConstants.TIMEZONE_OFFSET));
        JSONObject permissions = new JSONObject()
                .put("Permissions", new JSONArray()
                        .put(OpenBankingConstants.PERM_READ_ACCOUNTS_BASIC)
                        .put(OpenBankingConstants.PERM_READ_ACCOUNTS_DETAIL)
                        .put(OpenBankingConstants.PERM_READ_BALANCES)
                        .put(OpenBankingConstants.PERM_READ_TRANSACTIONS_DETAIL))
                .put("ExpirationDateTime", now.plusDays(90).format(ISO_DATETIME_FORMATTER))
                .put("TransactionFromDateTime", now.minusDays(30).format(ISO_DATETIME_FORMATTER))
                .put("TransactionToDateTime", now.format(ISO_DATETIME_FORMATTER));
        return new JSONObject()
                .put(OpenBankingConstants.FIELD_DATA, permissions)
                .put(OpenBankingConstants.FIELD_RISK, new JSONObject())
                .toString();
    }

    /**
     * Revokes the consent for a given account and returns the revocation result.
     *
     * @param accountId account ID whose consent is being revoked
     * @param bankName  bank name associated with the account
     * @param consentId consent ID to revoke
     * @return true if revocation succeeded, false otherwise
     * @throws Exception if the revocation request fails
     */
    public boolean revokeAccountConsent(String accountId, String bankName, String consentId) throws Exception {
        log.info("[DELETE] Attempting to revoke consent for accountId: {}, bankName: {}, {}",
                accountId, bankName, consentId);
        String tokenResponse = oauthService.getToken(OpenBankingConstants.SCOPE_ACCOUNTS);
        String token = new JSONObject(tokenResponse).getString("access_token");
        String revokeUrl = ConfigLoader.getAccountBaseUrl()
                + OpenBankingConstants.PATH_ACCOUNT_CONSENTS + "/" + consentId;
        log.info("[DELETE] Calling revoke URL: {}", revokeUrl);
        boolean success = client.deleteWithAuth(revokeUrl, token);
        log.info("[DELETE] OB backend revocation success: {}", success);
        return success;
    }
}
