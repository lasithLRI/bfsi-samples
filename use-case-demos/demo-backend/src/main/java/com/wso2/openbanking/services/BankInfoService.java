package com.wso2.openbanking.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.models.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class BankInfoService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONFIG_FILE = "config.json";

    private List<Bank> banks;
    private String name;
    private String image;
    private String background;
    private String route;
    private String applicationName;
    private CustomColors colors;
    private TransactionTableHeaderData transactionTableHeaders;
    private List<Payee> payees;
    private final List<String> currencies = new ArrayList<>(Arrays.asList("USD", "EURO", "GBP"));
    private AddAccountBankInfo addAccountBankInfo;

    public BankInfoService() {
    }

    public void loadBanks() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + CONFIG_FILE);
            }
            if (this.banks == null) {
                JsonNode rootNode = objectMapper.readTree(inputStream);
                loadBanksFromJson(rootNode);
                loadUserInfo(rootNode);
                loadColors(rootNode);
                loadApplicationInfo(rootNode);
                loadPayees(rootNode);
                loadTransactionHeaders(rootNode);
                loadAddAccountInfo(rootNode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadBanksFromJson(JsonNode rootNode) {
        JsonNode banksNode = rootNode.get("banks");
        if (banksNode != null && banksNode.isArray()) {
            this.banks = objectMapper.convertValue(banksNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Bank.class));
        }
    }

    private void loadUserInfo(JsonNode rootNode) {
        JsonNode nameNode = rootNode.get("user");
        this.name = nameNode.get("username").asText();
        this.image = nameNode.get("profile").asText();
        this.background = nameNode.get("background").asText();
    }

    private void loadColors(JsonNode rootNode) {
        JsonNode colorsNode = rootNode.get("colors");
        if (colorsNode != null) {
            this.colors = objectMapper.convertValue(colorsNode, CustomColors.class);
        }
    }

    private void loadApplicationInfo(JsonNode rootNode) {
        JsonNode routeNode = rootNode.get("name");
        this.route = routeNode.get("route").asText();
        this.applicationName = routeNode.get("applicationName").asText();
    }

    private void loadPayees(JsonNode rootNode) {
        JsonNode payeesNode = rootNode.get("payees");
        if (payeesNode != null) {
            this.payees = objectMapper.convertValue(payeesNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Payee.class));
        }
    }

    private void loadTransactionHeaders(JsonNode rootNode) {
        JsonNode transactionHeadersNode = rootNode.get("transactionTableHeaderData");
        if (transactionHeadersNode != null) {
            this.transactionTableHeaders = objectMapper.convertValue(
                    transactionHeadersNode,
                    TransactionTableHeaderData.class
            );
        }
    }

    private void loadAddAccountInfo(JsonNode rootNode) {
        JsonNode addAccountInfoNode = rootNode.get("addAccountInfo");
        if (addAccountInfoNode != null) {
            this.addAccountBankInfo = objectMapper.convertValue(addAccountInfoNode, AddAccountBankInfo.class);
        }
    }

    public ConfigResponse getConfigurations() {
        return new ConfigResponse(this.banks, this.name, this.image, this.background,
                this.route, this.applicationName, this.colors, this.transactionTableHeaders, this.payees);
    }

    public LoadPaymentPageResponse getPaymentPageInfo() {
        List<BankInfoInPayments> bankInfoInPayments = this.banks.stream()
                .flatMap(bank -> bank.getAccounts().stream()
                        .map(account -> new BankInfoInPayments(
                                bank.getName(),
                                account.getId()
                        ))
                )
                .collect(Collectors.toList());

        return new LoadPaymentPageResponse(bankInfoInPayments, this.payees, this.currencies);
    }

    public List<AddAccountBankInfo> getAddAccountBanksInformation() {
        List<AddAccountBankInfo> banksList = this.banks.stream()
                .map(bank -> new AddAccountBankInfo(bank.getName(), bank.getImage()))
                .collect(Collectors.toList());
        banksList.add(this.addAccountBankInfo);
        return banksList;
    }

    // Package-private methods for other services to use
    List<Bank> getBanks() {
        return banks;
    }

    void addBank(Bank bank) {
        this.banks.add(bank);
    }

    boolean isBankExists(String bankName) {
        return this.banks.stream()
                .anyMatch(bank -> bankName.equals(bank.getName()));
    }

    Optional<Account> findAccount(String bankName, String accountNumber) {
        return this.banks.stream()
                .filter(bank -> bank.getName().equals(bankName))
                .flatMap(bank -> bank.getAccounts().stream())
                .filter(account -> account.getId().equals(accountNumber))
                .findFirst();
    }

    void addTransactionToAccount(Account account, Transaction transaction) {
        List<Transaction> transactions = account.getTransactions();
        if (transactions == null) {
            transactions = new ArrayList<>();
            account.setTransactions(transactions);
        }
        transactions.add(0, transaction);
    }
}
