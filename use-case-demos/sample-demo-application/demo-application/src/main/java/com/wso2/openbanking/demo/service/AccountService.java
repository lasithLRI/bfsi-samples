/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.reflections.Reflections.log;

/** AccountService implementation. */
public final class AccountService {

    private final BankInfoService bankInfoService;
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

    /**
     * Private constructor — never throws, so EI_EXPOSE_REP2 is not triggered.
     * All fields assigned cleanly with no exception risk after assignment.
     */
    private AccountService(BankInfoService bankInfoService,
                           HttpTlsClient client,
                           OAuthTokenService oauthService) {
        this.bankInfoService = bankInfoService;
        this.client = client;
        this.oauthService = oauthService;
    }

    /**
     * Static factory method — handles all throwing logic before construction.
     * Use this instead of new AccountService(...).
     */
    public static AccountService create(BankInfoService bankInfoService,
                                        HttpTlsClient client)
            throws BankInfoLoadException {
        try {
            OAuthTokenService oauthService = new OAuthTokenService(client);
            return new AccountService(bankInfoService, client, oauthService);
        } catch (GeneralSecurityException | IOException e) {
            throw new BankInfoLoadException(
                    "Failed to initialize OAuth token service: " + e.getMessage(), e);
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<Account> addMockBankAccountsInformation() throws IOException, BankInfoLoadException {
        List<String> fetchedAccountIds = fetchAccountIds();
        System.out.println("=== Fetched account IDs: " + fetchedAccountIds);
        List<Account> accounts = fetchAccountsWithTransactions(fetchedAccountIds);
        System.out.println("=== Built accounts: " + accounts.size());
        for (Account a : accounts) {
            System.out.println("  -> Account: " + a.getId()
                    + " | Transactions: " + (a.getTransactions() != null ? a.getTransactions().size() : "null"));
        }
        return accounts;
    }

    public String processAddAccount(String bankName) throws Exception {
//        if (!bankName.equals(ConfigLoader.getMockBankName())) {
//            return null;
//        }
        String addAccountUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents";
        String consentBody = createAccountConsentBody();
        String token = oauthService.getToken("accounts openid");
        String consentResponse = oauthService.initializeConsent(token, consentBody, addAccountUrl);
        currentConsentId = new JSONObject(consentResponse).getJSONObject("Data").getString("ConsentId");

        return oauthService.authorizeConsent(consentResponse, "accounts openid");
    }

    private Set<String> getExistingAccountIds(String bankName) {
        Set<String> existingIds = new HashSet<>();
        if (bankInfoService.getBanks() == null) {
            return existingIds;
        }
        bankInfoService.getBanks().stream()
                .filter(bank -> bank.getName().equals(bankName))
                .flatMap(bank -> bank.getAccounts().stream())
                .forEach(account -> existingIds.add(account.getId()));
        return existingIds;
    }

    private void addAccountsToExistingBank(String bankName, List<Account> newAccounts) {
        bankInfoService.getBanks().stream()
                .filter(bank -> bank.getName().equals(bankName))
                .findFirst()
                .ifPresent(bank -> {
                    List<Account> currentAccounts = bank.getAccounts();
                    if (currentAccounts == null) {
                        currentAccounts = new ArrayList<>();
                    }
                    Set<String> currentIds = currentAccounts.stream()
                            .map(Account::getId)
                            .collect(Collectors.toSet());
                    newAccounts.stream()
                            .filter(account -> !currentIds.contains(account.getId()))
                            .forEach(bank::addAccount);
                });
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

        bankInfoService.replaceAccountsForBank(ConfigLoader.getMockBankName(), accounts);

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

        System.out.println("=== TRANSACTIONS RESPONSE for " + accountId + " ===");
        System.out.println(response);
        System.out.println("=====================================================");

        JSONObject root = new JSONObject(response);
        if (!root.has("Data") || !root.getJSONObject("Data").has("Transaction")) {
            System.out.println("WARNING: No Transaction array found in response for account: " + accountId);
            return new ArrayList<>();
        }

        JSONArray transactionsArray = root.getJSONObject("Data").getJSONArray("Transaction");
        System.out.println("Transaction count for " + accountId + ": " + transactionsArray.length());

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

    private void addNewBank(String bankName, List<Account> accounts) {
        Bank newBank = new Bank(
                bankName,
                ConfigLoader.getMockBankLogo(),
                ConfigLoader.getMockBankPrimaryColor(),
                ConfigLoader.getMockBankSecondaryColor(),
                accounts
        );
        bankInfoService.addBank(newBank);
    }

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

    public boolean revokeAccountConsent(String accountId, String bankName) throws Exception {
        log.info("[DELETE] Attempting to revoke consent for accountId: {}, bankName: {}",
                accountId, bankName);
        Bank bank = bankInfoService.getBanks().stream()
                .filter(b -> b.getName().equals(bankName))
                .findFirst()
                .orElse(null);
        if (bank == null) {
            log.warn("[DELETE] Bank not found: {}", bankName);
            return false;
        }
        String consentId = bank.getConsentIdForAccount(accountId);
        if (consentId == null) {
            log.warn("[DELETE] No consentId found for accountId: {}", accountId);
            return false;
        }
        log.info("[DELETE] Resolved consentId: {}", consentId);
        String tokenResponse = oauthService.getToken("accounts openid");
        String token = new JSONObject(tokenResponse).getString("access_token");
        String revokeUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents/" + consentId;
        log.info("[DELETE] Calling revoke URL: {}", revokeUrl);
        boolean success = client.deleteWithAuth(revokeUrl, token);
        log.info("[DELETE] OB backend revocation success: {}", success);
        if (success) {
            List<String> allAccountIds = bank.getAccountIdsByConsentId(consentId);
            log.info("[DELETE] Removing accounts: {}", allAccountIds);
            allAccountIds.forEach(bank::removeAccount);
            bank.removeConsent(consentId);
        }
        return success;
    }
}
