package com.wso2.openbanking.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.models.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BankInfoService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    String fileName = "config.json";
    private List<Bank> banks;
    private String name;
    private String image;
    private String background;
    private String route;
    private String applicationName;
    private CustomColors colors;
    private TransactionTableHeaderData transactionTableHeaders;
    private List<Payee>  payees;
    private final List<String> currencies = new ArrayList<>(Arrays.asList("USD", "EURO", "GBP"));
    private AddAccountBankInfo addAccountBankInfo;
    HttpTlsClient client = new HttpTlsClient("/obtransport.pem", "/obtransport.key", "/client-truststore.jks", "123456");
    Payment payment;
    Transaction transaction;

    public BankInfoService() throws Exception {
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    private String accessToken;

    public void loadBanks(){
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)){
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + fileName);
            }
            if (this.banks == null){
                JsonNode rootNode = objectMapper.readTree(inputStream);
                JsonNode banksNode = rootNode.get("banks");
                if (banksNode != null && banksNode.isArray()) {
                    this.banks = objectMapper.convertValue(banksNode,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Bank.class));

                }
                JsonNode nameNode = rootNode.get("user");
                this.name = nameNode.get("username").asText();
                this.image = nameNode.get("profile").asText();
                this.background = nameNode.get("background").asText();
                JsonNode colorsNode = rootNode.get("colors");

                if (colorsNode != null) {
                    this.colors = objectMapper.convertValue(colorsNode, CustomColors.class);
                }

                JsonNode routeNode = rootNode.get("name");
                this.route = routeNode.get("route").asText();
                this.applicationName = routeNode.get("applicationName").asText();

                JsonNode payeesNode = rootNode.get("payees");
                if (payeesNode != null) {
                    this.payees=objectMapper.convertValue(payeesNode,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Payee.class));
                }



                JsonNode transactionHeadersNode = rootNode.get("transactionTableHeaderData");
                if (transactionHeadersNode != null) {
                    this.transactionTableHeaders = objectMapper.convertValue(
                            transactionHeadersNode,
                            TransactionTableHeaderData.class
                    );
                }


                JsonNode addAccountInfoNode = rootNode.get("addAccountInfo");
                if (addAccountInfoNode != null) {
                    this.addAccountBankInfo = objectMapper.convertValue(addAccountInfoNode,AddAccountBankInfo.class);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigResponse getConfigurations(){
        return new ConfigResponse(this.banks,this.name,this.image,this.background,this.route,this.applicationName,this.colors, this.transactionTableHeaders, this.payees);
    }

    public LoadPaymentPageResponse getPaymentPageInfo(){

        List<BankInfoInPayments> bankInfoInPayments = this.banks.stream()
                .flatMap(bank -> bank.getAccounts().stream()
                        .map(account -> new BankInfoInPayments(
                                bank.getName(),
                                account.getId()
                        ))
                )
                .collect(Collectors.toList());

        return new LoadPaymentPageResponse(bankInfoInPayments,this.payees,this.currencies);
    }

    public List<AddAccountBankInfo> addNewAccount(){
       return getAddAccountBanksInformation();
    }

    public List<AddAccountBankInfo> getAddAccountBanksInformation(){
        List<AddAccountBankInfo> banksList = this.banks.stream().map(bank -> new AddAccountBankInfo(bank.getName(),bank.getImage())).collect(Collectors.toList());
        banksList.add(this.addAccountBankInfo);
        return banksList;
    }

    public void addMockBankAccountsInformation() throws IOException {
        if (this.banks == null) {
            loadBanks();
        }
        String bankName = "SandBox Mock Bank";
        if (isBankAlreadyExists(bankName)) {
            System.out.println(bankName + " already exists, skipping...");
            return;
        }
        List<String> accountIds = fetchAccountIds();
        System.out.println("Account IDs: " + accountIds.toString());
        List<Account> accounts = fetchAccountsWithTransactions(accountIds);
        addNewBank(bankName, accounts);
        System.out.println("Added " + bankName + " with " + accounts.size() + " accounts and their transactions");
    }

    private boolean isBankAlreadyExists(String bankName) {
        return this.banks.stream()
                .anyMatch(bank -> bankName.equals(bank.getName()));
    }

    private List<String> fetchAccountIds() throws IOException {
        String responseAccounts = client.getAccountsRequest(
                "https://localhost:8243/open-banking/v3.1/aisp/accounts",
                this.accessToken
        );
        JSONObject accountsJson = new JSONObject(responseAccounts);
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
        String url = "https://localhost:8243/open-banking/v3.1/aisp/accounts/" + accountId;
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
        String url = "https://localhost:8243/open-banking/v3.1/aisp/accounts/"
                + accountId + "/balances";
        String response = client.getAccountFromId(url, this.accessToken);
        JSONObject balanceJson = new JSONObject(response);
        JSONArray balances = balanceJson.getJSONObject("Data").getJSONArray("Balance");
        JSONObject firstBalance = balances.getJSONObject(0);
        String amount = firstBalance.getJSONObject("Amount").getString("Amount");
        return Double.parseDouble(amount);
    }

    private List<Transaction> fetchAccountTransactions(String accountId) throws IOException {
        String url = "https://localhost:8243/open-banking/v3.1/aisp/accounts/"
                + accountId + "/transactions";
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

        return new Transaction(
                transactionId,
                date,
                reference,
                amount,
                currency,
                creditDebitStatus
        );
    }

    private void addNewBank(String bankName, List<Account> accounts) {
        Bank newBank = new Bank(
                bankName,
                "/resources/assets/images/logos/global_asset_trust_logo.png",
                "black",
                "black",
                accounts
        );

        this.banks.add(newBank);
    }

    public String getToken(String scope){
        try {
            String jti = new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
            AppContext context = new AppContext("onKy05vpqDjTenzZSRjfSOfb3ZMa","sCekNgSWIauQ34klRhDGqfwpjc4","PS256","JWT",jti);
            String body = "grant_type=client_credentials" +
                    "&scope="+scope +
                    "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                    "&client_id=" + context.getClientId() +
                    "&client_assertion="+ context.createClientAsserstion() +
                    "&redirect_uri=https://tpp.local.ob/ob_demo_backend_war/init/redirected";

            HttpTlsClient client = new HttpTlsClient("/obtransport.pem","/obtransport.key","/client-truststore.jks","123456");
            String response = client.postJwt("https://localhost:9446/oauth2/token", body);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String processAddAccount(String bankName) throws Exception {
        if(!bankName.equals("SandBox Mock Bank")){
            return null;
        }
        String addAccountUrl = "https://localhost:8243/open-banking/v3.1/aisp/account-access-consents";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZoneOffset offset = ZoneOffset.of("+05:30");
        ZonedDateTime now = ZonedDateTime.now(offset);
        ZonedDateTime expirationDateTime = now.plusDays(90);
        String expirationDate = expirationDateTime.format(formatter);
        String transactionToDate = now.format(formatter);
        ZonedDateTime transactionFromDateTime = now.minusDays(30);
        String transactionFromDate = transactionFromDateTime.format(formatter);
        String consentBody = "{\n" +
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
                "    \"Risk\": {\n" +
                "    }\n" +
                "}";

        String consent = consentInit(getToken("accounts openid"),consentBody,addAccountUrl);
        return consentAuth(consent,"accounts openid");
    }

    public String consentInit(String token, String consentBody, String url) throws Exception {

        JSONObject jsonObject = new JSONObject(token);
        token = jsonObject.get("access_token").toString();
        HttpTlsClient client = new HttpTlsClient("/obtransport.pem","/obtransport.key","/client-truststore.jks","123456");
        String response = client.postConsentInit(url,consentBody,token);
        return response;
    }

    public String consentAuth(String consentInit,String scope) throws Exception {

        JSONObject jsonObject = new JSONObject(consentInit);
        JSONObject data = jsonObject.getJSONObject("Data");
        String consentId = data.getString("ConsentId");
        String jti = new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
        AppContext context = new AppContext("onKy05vpqDjTenzZSRjfSOfb3ZMa","sCekNgSWIauQ34klRhDGqfwpjc4","PS256","JWT",jti);
        String requestObject = context.makeRequestObject(consentId);
        HttpTlsClient client = new HttpTlsClient("/obtransport.pem","/obtransport.key","/client-truststore.jks","123456");
        String url = client.postConsentAuthRequest(requestObject,"onKy05vpqDjTenzZSRjfSOfb3ZMa",scope);
        return url;
    }

    public String paymentRequest(Payment payment) throws Exception {

        this.payment = payment;
        String token = getToken("payments openid");
        System.out.println("token: " + token);
        String paymentUrl = "https://localhost:8243/open-banking/v3.1/pisp/payment-consents";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZoneOffset offset = ZoneOffset.of("+05:30");
        ZonedDateTime now = ZonedDateTime.now(offset);
        ZonedDateTime expirationDateTime = now.plusDays(90);
        String expirationDate = expirationDateTime.format(formatter);
        String transactionToDate = now.format(formatter);
        ZonedDateTime transactionFromDateTime = now.minusDays(30);
        String transactionFromDate = transactionFromDateTime.format(formatter);
        String consentBody ="{\n" +
                "    \"Data\": {\n" +
                "        \"Initiation\": {\n" +
                "            \"InstructionIdentification\": \"ACME412\",\n" +
                "            \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
                "            \"LocalInstrument\": \"OB.Paym\",\n" +
                "            \"InstructedAmount\": {\n" +
                "                \"Amount\": \"165.88\",\n" +
                "                \"Currency\": \"GBP\"\n" +
                "            },\n" +
                "            \"CreditorAccount\": {\n" +
                "                \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
                "                \"Identification\": \"08080021325698\",\n" +
                "                \"Name\": \"ACME Inc\",\n" +
                "                \"SecondaryIdentification\": \"0002\"\n" +
                "            },\n" +
                "            \"DebtorAccount\": {\n" +
                "                \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
                "                \"Identification\": \"08080025612489\",\n" +
                "                \"Name\": \"Jane Smith\",\n" +
                "                \"SecondaryIdentification\": \"080801562314789\"\n" +
                "            },\n" +
                "            \"SupplementaryData\": {\n" +
                "                \"additionalProp1\": {\n" +
                "\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"Risk\": {\n" +
                "\n" +
                "    }\n" +
                "}";

        String response = consentInit(token,consentBody,paymentUrl);
        String url = consentAuth(response,"payments openid");
        return url;

    }

    public void addPaymentToAccount() throws Exception {

        if (payment != null) {
            String account = this.payment.getUserAccount();
            String[] parts = account.split("-", 2);
            String bankName = parts[0];
            String accountNumber = parts.length > 1 ? parts[1] : "";
            double paymentAmount = Double.parseDouble(this.payment.getAmount());
            String transactionId = "TXN-" + UUID.randomUUID().toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            ZoneOffset offset = ZoneOffset.of("+05:30");
            ZonedDateTime now = ZonedDateTime.now(offset);
            String currentDate = now.format(formatter);
            this.transaction = new Transaction("jsdbvjefbv","","","100","","");
            findAndUpdateAccount(bankName,accountNumber,paymentAmount,this.transaction);
        }
    }

    private void findAndUpdateAccount(String bankName, String accountNumber,
                                      double amountToDeduct, Transaction transaction) {
        Optional<Account> accountOpt = findAccount(bankName, accountNumber);
        if (accountOpt.isPresent()) {
            Account acc = accountOpt.get();
            acc.setBalance(acc.getBalance() - amountToDeduct);
            addTransactionToAccount(acc, transaction);
            System.out.println("Account updated successfully: " + bankName + "-" + accountNumber);
        } else {
            System.out.println("Account not found: " + bankName + "-" + accountNumber);
        }
    }

    private Optional<Account> findAccount(String bankName, String accountNumber) {
        return this.banks.stream()
                .filter(bank -> bank.getName().equals(bankName))
                .flatMap(bank -> bank.getAccounts().stream())
                .filter(account -> account.getId().equals(accountNumber))
                .findFirst();
    }

    private void addTransactionToAccount(Account account, Transaction transaction) {
        List<Transaction> transactions = account.getTransactions();
        if (transactions == null) {
            transactions = new ArrayList<>();
            account.setTransactions(transactions);
        }
        transactions.add(0, transaction);
    }
}
