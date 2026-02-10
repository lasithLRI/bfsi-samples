package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.models.Account;
import com.wso2.openbanking.models.Bank;
import com.wso2.openbanking.models.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccountService {

    private final BankInfoService bankInfoService;
    private final HttpTlsClient client;
    private final OAuthTokenService oauthService;
    private String accessToken;

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

        if (bankInfoService.isBankExists(bankName)) {
            System.out.println(bankName + " already exists, skipping...");
            return;
        }

        List<String> accountIds = fetchAccountIds();
        System.out.println("Account IDs: " + accountIds.toString());

        List<Account> accounts = fetchAccountsWithTransactions(accountIds);
        addNewBank(bankName, accounts);

        System.out.println("Added " + bankName + " with " + accounts.size() +
                " accounts and their transactions");
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

    private List<Account> fetchAccountsWithTransactions(List<String> accountIds) throws IOException {
        List<Account> accounts = new ArrayList<>();
        for (String accountId : accountIds) {
            String accountName = fetchAccountName(accountId);
            double balance = fetchAccountBalance(accountId);
            List<Transaction> transactions = fetchAccountTransactions(accountId);

            Account account = new Account(accountId, accountName, balance, transactions);
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

    private List<Transaction> fetchAccountTransactions(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + "/accounts/" + accountId + "/transactions";
        String response = client.getAccountFromId(url, this.accessToken);

        JSONObject transactionsJson = new JSONObject(response);
        JSONArray transactionsArray = transactionsJson.getJSONObject("Data")
                .getJSONArray("Transaction");

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsArray.length(); i++) {
            JSONObject txn = transactionsArray.getJSONObject(i);
            Transaction transaction = parseTransaction(txn);
            transactions.add(transaction);
        }
        return transactions;
    }

    private Transaction parseTransaction(JSONObject txn) {
        String transactionId = txn.getString("TransactionId");
        String date = txn.getString("BookingDateTime");
        String reference = txn.getString("TransactionInformation");

        JSONObject amountObj = txn.getJSONObject("Amount");
        String amount = amountObj.getString("Amount");
        String currency = amountObj.getString("Currency");
        String creditDebitStatus = txn.getString("CreditDebitIndicator");

        return new Transaction(transactionId, date, reference, amount, currency, creditDebitStatus);
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
