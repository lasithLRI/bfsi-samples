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
import java.util.*;
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

    public void addMockBankAccountsInformation() throws IOException {
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

    public String processAddAccount(String bankName) throws Exception {
        if (!bankName.equals(ConfigLoader.getMockBankName())) {
            return null;
        }

        String addAccountUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents";
        String consentBody = createAccountConsentBody();
        String token = oauthService.getToken("accounts openid");
        String consentResponse = oauthService.initializeConsent(token, consentBody, addAccountUrl);

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

    private List<String> fetchAccountIds() throws IOException {
        String response = client.getAccountsRequest(
                ConfigLoader.getAccountBaseUrl() + "/accounts",
                this.accessToken
        );

        JSONArray accountsArray = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Account");

        List<String> accountIds = new ArrayList<>();
        for (int i = 0; i < accountsArray.length(); i++) {
            accountIds.add(accountsArray.getJSONObject(i).getString("AccountId"));
        }
        return accountIds;
    }

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

    private String fetchAccountName(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + "/accounts/" + accountId;
        String response = client.getAccountFromId(url, this.accessToken);

        JSONObject accountDataNode = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Account")
                .getJSONObject(0);

        if (accountDataNode.has("Account")) {
            return accountDataNode.getJSONArray("Account")
                    .getJSONObject(0)
                    .optString("Name", "Open Banking Account");
        }
        return accountDataNode.optString("Nickname", "Standard Account");
    }

    private double fetchAccountBalance(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + "/accounts/" + accountId + "/balances";
        String response = client.getAccountFromId(url, this.accessToken);

        String amount = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Balance")
                .getJSONObject(0)
                .getJSONObject("Amount")
                .getString("Amount");

        return Double.parseDouble(amount);
    }

    private List<Transaction> fetchAccountTransactions(String accountId, String bankName) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + "/accounts/" + accountId + "/transactions";
        String response = client.getAccountFromId(url, this.accessToken);

        JSONArray transactionsArray = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Transaction");

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsArray.length(); i++) {
            transactions.add(parseTransaction(transactionsArray.getJSONObject(i), bankName, accountId));
        }
        return transactions;
    }

    private Transaction parseTransaction(JSONObject txn, String bankName, String accountId) {
        Transaction transaction = new Transaction();
        transaction.setId(txn.getString("TransactionId"));
        transaction.setDate(convertIsoDateTimeToDate(txn.getString("BookingDateTime")));
        transaction.setReference(txn.getString("TransactionInformation"));

        JSONObject amountObj = txn.getJSONObject("Amount");
        transaction.setAmount(amountObj.getString("Amount"));
        transaction.setCurrency(amountObj.getString("Currency"));
        transaction.setCreditDebitStatus(txn.getString("CreditDebitIndicator"));
        transaction.setBank(bankName);
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
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.of("+05:30"));

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
