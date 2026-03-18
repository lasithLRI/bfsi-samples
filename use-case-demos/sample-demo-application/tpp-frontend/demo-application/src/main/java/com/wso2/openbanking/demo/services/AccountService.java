package com.wso2.openbanking.demo.services;


import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.models.Account;
import com.wso2.openbanking.demo.models.Bank;
import com.wso2.openbanking.demo.models.Transaction;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles fetching and storing account and transaction data from the mock Open Banking API.
 * Uses mTLS via HttpTlsClient and OAuth tokens via OAuthTokenService to authenticate requests.
 */
public class AccountService {

    private final BankInfoService bankInfoService;
    private final HttpTlsClient client;
    private final OAuthTokenService oauthService;
    private String accessToken;

    private static final DateTimeFormatter ISO_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String FIELD_ACCOUNT = "Account";
    private static final String FIELD_AMOUNT = "Amount";
    private static final String ACCOUNTS_PATH = "/accounts/";

    public AccountService(BankInfoService bankInfoService, HttpTlsClient client) throws Exception {
        this.bankInfoService = bankInfoService;
        this.client = client;
        this.oauthService = new OAuthTokenService(client);
    }

    /** Sets the OAuth access token used to authenticate API requests. */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Fetches accounts from the mock bank API and adds any new ones to the in-memory store.
     * Accounts already present are skipped to avoid duplication.
     */
    public void addMockBankAccountsInformation() throws IOException, BankInfoLoadException {
        if (bankInfoService.getBanks() == null) {
            bankInfoService.loadBanks();
        }
        String bankName = ConfigLoader.getMockBankName();
        boolean bankExists = bankInfoService.isBankExists(bankName);
        List<String> fetchedAccountIds = fetchAccountIds();
        Set<String> existingAccountIds = getExistingAccountIds(bankName);
        List<String> newAccountIds = fetchedAccountIds.stream()
                .filter(id -> !existingAccountIds.contains(id))
                .collect(Collectors.toList());
        if (newAccountIds.isEmpty()) {
            return;
        }
        List<Account> newAccounts = fetchAccountsWithTransactions(newAccountIds, bankName);
        if (bankExists) {
            addAccountsToExistingBank(bankName, newAccounts);
        } else {
            addNewBank(bankName, newAccounts);
        }
    }

    /**
     * Initiates the account consent flow for the mock bank.
     * Returns the authorization URL the user must visit to grant consent.
     */
    public String processAddAccount(String bankName) throws Exception {
        if (!bankName.equals(ConfigLoader.getMockBankName())) {
            return null;
        }
        String addAccountUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents";
        String consentBody = createAccountConsentBody();
        String token = oauthService.getToken("accounts openid");

        String consentResponse = oauthService.initializeConsent(token, consentBody, addAccountUrl);

        String redirectUrl = oauthService.authorizeConsent(consentResponse, "accounts openid");

        System.out.println("========== REDIRECT URL ==========");
        System.out.println(redirectUrl);
        System.out.println("==================================");

        return redirectUrl;
    }

    /** Returns the set of account IDs already stored for the given bank. */
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

    /** Merges new accounts into an existing bank entry, skipping any that are already present. */
    private void addAccountsToExistingBank(String bankName, List<Account> newAccounts) {
        bankInfoService.getBanks().stream()
                .filter(bank -> bank.getName().equals(bankName))
                .findFirst()
                .ifPresent(bank -> {
                    List<Account> currentAccounts = bank.getAccounts();
                    if (currentAccounts == null) {
                        currentAccounts = new ArrayList<>();
                        bank.setAccounts(currentAccounts);
                    }
                    Set<String> currentIds = currentAccounts.stream()
                            .map(Account::getId)
                            .collect(Collectors.toSet());
                    List<Account> uniqueNewAccounts = newAccounts.stream()
                            .filter(account -> !currentIds.contains(account.getId()))
                            .collect(Collectors.toList());
                    currentAccounts.addAll(uniqueNewAccounts);
                });
    }

    /** Calls the accounts endpoint and returns all account IDs available under the current token. */
    private List<String> fetchAccountIds() throws IOException {
        String response = client.getWithAuth(
                ConfigLoader.getAccountBaseUrl() + "/accounts",
                this.accessToken
        );
        JSONArray accountsArray = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray(FIELD_ACCOUNT);
        List<String> accountIds = new ArrayList<>();
        for (int i = 0; i < accountsArray.length(); i++) {
            accountIds.add(accountsArray.getJSONObject(i).getString("AccountId"));
        }
        return accountIds;
    }

    /** Fetches full account details and transaction history for each of the given account IDs. */
    private List<Account> fetchAccountsWithTransactions(List<String> accountIds, String bankName) throws IOException {
        List<Account> accounts = new ArrayList<>();
        for (String accountId : accountIds) {
            String accountName = fetchAccountName(accountId);
            double balance = fetchAccountBalance(accountId);
            List<Transaction> transactions = fetchAccountTransactions(accountId, bankName);
            Account account = new Account(accountId, accountName, balance, transactions);
            account.setBank(bankName);
            accounts.add(account);
        }
        return accounts;
    }

    /**
     * Fetches the display name for an account. Falls back to "Open Banking Account"
     * or "Standard Account" if no name is present in the API response.
     */
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

    /** Fetches the current balance for an account from the balances endpoint. */
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

    /** Fetches and parses the full transaction list for an account. */
    private List<Transaction> fetchAccountTransactions(String accountId, String bankName) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + ACCOUNTS_PATH + accountId + "/transactions";
        String response = client.getWithAuth(url, this.accessToken);
        JSONArray transactionsArray = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Transaction");
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsArray.length(); i++) {
            transactions.add(parseTransaction(transactionsArray.getJSONObject(i), bankName, accountId));
        }
        return transactions;
    }

    /** Maps a raw transaction JSON object to a Transaction model. */
    private Transaction parseTransaction(JSONObject txn, String bankName, String accountId) {
        Transaction transaction = new Transaction();
        transaction.setId(txn.getString("TransactionId"));
        transaction.setDate(convertIsoDateTimeToDate(txn.getString("BookingDateTime")));
        transaction.setReference(txn.getString("TransactionInformation"));
        JSONObject amountObj = txn.getJSONObject(FIELD_AMOUNT);
        transaction.setAmount(amountObj.getString(FIELD_AMOUNT));
        transaction.setCurrency(amountObj.getString("Currency"));
        transaction.setCreditDebitStatus(txn.getString("CreditDebitIndicator"));
        transaction.setBank(bankName);
        transaction.setAccount(accountId);
        return transaction;
    }

    /**
     * Converts an ISO 8601 datetime string to a plain date string (yyyy-MM-dd).
     * Tries zoned, then local datetime parsing, then falls back to a substring slice.
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

    /** Creates a new Bank entry in the in-memory store using mock bank config properties. */
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

    /** Builds the consent request body for the account access consent endpoint. */
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
}
