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

    private AccountService(HttpTlsClient client, OAuthTokenService oauthService) {
        this.client = client;
        this.oauthService = oauthService;
    }

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

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<Account> createBankInContext() throws IOException {
        List<String> fetchedAccountIds = fetchAccountIds();
        return fetchAccountsWithTransactions(fetchedAccountIds);
    }

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
