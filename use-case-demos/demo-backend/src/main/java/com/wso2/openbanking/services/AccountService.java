package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.models.Account;
import com.wso2.openbanking.models.Bank;
import com.wso2.openbanking.models.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountService {

    private final BankInfoService bankInfoService;
    private final HttpTlsClient client;
    private final OAuthTokenService oauthService;
    private String accessToken;

    private static final DateTimeFormatter ISO_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AccountService(BankInfoService bankInfoService, HttpTlsClient client) throws Exception {
        this.bankInfoService = bankInfoService;
        this.client = client;
        this.oauthService = new OAuthTokenService(client);
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Adds mock bank accounts information, ensuring no duplicate accounts are added.
     * Checks if accounts already exist in the bank before adding them.
     */
    public void addMockBankAccountsInformation() throws IOException {
        if (bankInfoService.getBanks() == null) {
            bankInfoService.loadBanks();
        }

        String bankName = ConfigLoader.getMockBankName();

        // Check if bank exists
        boolean bankExists = bankInfoService.isBankExists(bankName);

        if (bankExists) {
            System.out.println(bankName + " already exists, checking for new accounts...");
        }

        List<String> accountIds = fetchAccountIds();
        System.out.println("Fetched Account IDs: " + accountIds.toString());

        // Get existing account IDs for this bank
        Set<String> existingAccountIds = getExistingAccountIds(bankName);
        System.out.println("Existing Account IDs: " + existingAccountIds.toString());

        // Filter out accounts that already exist
        List<String> newAccountIds = accountIds.stream()
                .filter(id -> !existingAccountIds.contains(id))
                .collect(Collectors.toList());

        if (newAccountIds.isEmpty()) {
            System.out.println("No new accounts to add. All accounts already exist.");
            return;
        }

        System.out.println("New Account IDs to add: " + newAccountIds.toString());

        // Fetch only new accounts with their transactions
        List<Account> newAccounts = fetchAccountsWithTransactions(newAccountIds, bankName);

        if (bankExists) {
            // Add accounts to existing bank
            addAccountsToExistingBank(bankName, newAccounts);
            System.out.println("Added " + newAccounts.size() + " new accounts to " + bankName);
        } else {
            // Create new bank with accounts
            addNewBank(bankName, newAccounts);
            System.out.println("Added " + bankName + " with " + newAccounts.size() + " accounts");
        }
    }

    /**
     * Process add account request - ensures only new accounts are linked
     */
    public String processAddAccount(String bankName) throws Exception {
        if (!bankName.equals(ConfigLoader.getMockBankName())) {
            return null;
        }

        System.out.println("Processing add account...");

        String addAccountUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents";
        String consentBody = createAccountConsentBody();
        System.out.println(consentBody);
        String token = oauthService.getToken("accounts openid");
        System.out.println(token);
        String consentResponse = oauthService.initializeConsent(token, consentBody, addAccountUrl);
        System.out.println(consentResponse+"=_+++++++++++++++++");
        return oauthService.authorizeConsent(consentResponse, "accounts openid");
    }

    /**
     * Gets all existing account IDs for a given bank
     */
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

    /**
     * Adds new accounts to an existing bank, avoiding duplicates
     */
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

                    // Filter out any duplicates just to be safe
                    Set<String> currentIds = currentAccounts.stream()
                            .map(Account::getId)
                            .collect(Collectors.toSet());

                    List<Account> uniqueNewAccounts = newAccounts.stream()
                            .filter(account -> !currentIds.contains(account.getId()))
                            .collect(Collectors.toList());

                    currentAccounts.addAll(uniqueNewAccounts);

                    System.out.println("Added " + uniqueNewAccounts.size() + " unique accounts to bank");
                });
    }

    /**
     * Fetches account IDs from the API
     */
    private List<String> fetchAccountIds() throws IOException {
        String response = client.getAccountsRequest(
                ConfigLoader.getAccountBaseUrl() + "/accounts",
                this.accessToken
        );
        JSONObject accountsJson = new JSONObject(response);
        JSONArray accountsArray = accountsJson.getJSONObject("Data").getJSONArray("Account");

        List<String> accountIds = new ArrayList<>();
        for (int i = 0; i < accountsArray.length(); i++) {
            String accountId = accountsArray.getJSONObject(i).getString("AccountId");
            accountIds.add(accountId);
        }
        return accountIds;
    }

    /**
     * Fetches account details and transactions for given account IDs
     * Sets bank and account fields on transactions
     */
    private List<Account> fetchAccountsWithTransactions(List<String> accountIds, String bankName) throws IOException {
        List<Account> accounts = new ArrayList<>();
        for (String accountId : accountIds) {
            String accountName = fetchAccountName(accountId);
            double balance = fetchAccountBalance(accountId);
            List<Transaction> transactions = fetchAccountTransactions(accountId, bankName);

            Account account = new Account(accountId, accountName, balance, transactions);
            account.setBank(bankName);  // Set bank name on account
            accounts.add(account);
        }
        return accounts;
    }

    private String fetchAccountName(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + "/accounts/" + accountId;
        String response = client.getAccountFromId(url, this.accessToken);

        JSONObject accountJson = new JSONObject(response);
        JSONObject jsonAccount = accountJson.getJSONObject("Data")
                .getJSONArray("Account")
                .getJSONObject(0);

        if (jsonAccount.has("Account")) {
            return jsonAccount.getJSONArray("Account")
                    .getJSONObject(0)
                    .optString("Name", "Open Banking Account");
        } else {
            return jsonAccount.optString("Nickname", "Standard Account");
        }
    }

    private double fetchAccountBalance(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + "/accounts/" + accountId + "/balances";
        String response = client.getAccountFromId(url, this.accessToken);

        JSONObject balanceJson = new JSONObject(response);
        JSONArray balances = balanceJson.getJSONObject("Data").getJSONArray("Balance");
        JSONObject firstBalance = balances.getJSONObject(0);
        String amount = firstBalance.getJSONObject("Amount").getString("Amount");

        return Double.parseDouble(amount);
    }

    /**
     * Fetches transactions for an account and sets bank/account fields
     * Converts ISO datetime to simple date format (yyyy-MM-dd)
     */
    private List<Transaction> fetchAccountTransactions(String accountId, String bankName) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + "/accounts/" + accountId + "/transactions";
        String response = client.getAccountFromId(url, this.accessToken);

        JSONObject transactionsJson = new JSONObject(response);
        JSONArray transactionsArray = transactionsJson.getJSONObject("Data")
                .getJSONArray("Transaction");

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsArray.length(); i++) {
            JSONObject txn = transactionsArray.getJSONObject(i);
            Transaction transaction = parseTransaction(txn, bankName, accountId);
            transactions.add(transaction);
        }
        return transactions;
    }

    /**
     * Parses transaction JSON and sets bank/account fields
     * Converts ISO datetime string to simple date format (yyyy-MM-dd)
     */
    private Transaction parseTransaction(JSONObject txn, String bankName, String accountId) {
        Transaction transaction = new Transaction();

        transaction.setId(txn.getString("TransactionId"));

        // Convert ISO datetime to simple date format
        String bookingDateTime = txn.getString("BookingDateTime");
        String dateOnly = convertIsoDateTimeToDate(bookingDateTime);
        transaction.setDate(dateOnly);

        transaction.setReference(txn.getString("TransactionInformation"));

        JSONObject amountObj = txn.getJSONObject("Amount");
        transaction.setAmount(amountObj.getString("Amount"));
        transaction.setCurrency(amountObj.getString("Currency"));
        transaction.setCreditDebitStatus(txn.getString("CreditDebitIndicator"));

        // CRITICAL: Set bank and account fields
        transaction.setBank(bankName);
        transaction.setAccount(accountId);

        return transaction;
    }

    /**
     * Converts ISO datetime string to simple date format
     * Example: "2024-02-15T10:30:45.123+00:00" -> "2024-02-15"
     */
    private String convertIsoDateTimeToDate(String isoDateTime) {
        try {
            // Try parsing as ISO datetime with zone
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoDateTime, ISO_DATETIME_FORMATTER);
            return zonedDateTime.format(DATE_FORMATTER);
        } catch (Exception e1) {
            try {
                // Try parsing as LocalDateTime
                LocalDateTime localDateTime = LocalDateTime.parse(isoDateTime,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return localDateTime.format(DATE_FORMATTER);
            } catch (Exception e2) {
                // If all parsing fails, try to extract just the date part
                if (isoDateTime.length() >= 10) {
                    return isoDateTime.substring(0, 10);
                }
                // Last resort: return as-is
                System.err.println("WARNING: Could not parse datetime: " + isoDateTime);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZoneOffset offset = ZoneOffset.of("+05:30");
        ZonedDateTime now = ZonedDateTime.now(offset);

        String expirationDate = now.plusDays(90).format(formatter);
        String transactionToDate = now.format(formatter);
        String transactionFromDate = now.minusDays(30).format(formatter);

        return "{\n" +
                "    \"Data\": {\n" +
                "        \"Permissions\": [\n" +
                "            \"ReadAccountsBasic\",\n" +
                "            \"ReadAccountsDetail\",\n" +
                "            \"ReadBalances\",\n" +
                "            \"ReadTransactionsDetail\"\n" +
                "        ],\n" +
                "        \"ExpirationDateTime\": \"" + expirationDate + "\",\n" +
                "        \"TransactionFromDateTime\": \"" + transactionFromDate + "\",\n" +
                "        \"TransactionToDateTime\": \"" + transactionToDate + "\"\n" +
                "    },\n" +
                "    \"Risk\": {}\n" +
                "}";
    }
}
