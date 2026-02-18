package com.wso2.openbanking.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.exception.BankInfoLoadException;
import com.wso2.openbanking.models.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class BankInfoService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONFIG_FILE = "config.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private List<Bank> banks;
    private String name;
    private String image;
    private String background;
    private String route;
    private String applicationName;
    private CustomColors colors;
    private TransactionTableHeaderData transactionTableHeaders;
    private StandingOrdersTableHeaderData standingOrdersTableHeaders;
    private List<Payee> payees;
    private final List<String> currencies = new ArrayList<>(Arrays.asList("USD", "EURO", "GBP"));
    private AddAccountBankInfo addAccountBankInfo;

    public BankInfoService() {
        // No initialization required: all fields are populated lazily via loadBanks()
    }

    public void loadBanks() throws BankInfoLoadException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new BankInfoLoadException("Config file not found on classpath: " + CONFIG_FILE);
            }
            if (this.banks == null) {
                JsonNode rootNode = objectMapper.readTree(inputStream);
                loadBanksFromJson(rootNode);
                loadUserInfo(rootNode);
                loadColors(rootNode);
                loadApplicationInfo(rootNode);
                loadPayees(rootNode);
                loadTransactionHeaders(rootNode);
                loadStandingOrdersHeaders(rootNode);
                loadAddAccountInfo(rootNode);
                loadMockBankFromProperties();
            }
        } catch (IOException e) {
            throw new BankInfoLoadException("Failed to read or parse config file: " + CONFIG_FILE, e);
        }
    }

    private void loadBanksFromJson(JsonNode rootNode) throws BankInfoLoadException {
        JsonNode banksNode = rootNode.get("banks");
        if (banksNode != null && banksNode.isArray()) {
            try {
                this.banks = objectMapper.convertValue(
                        banksNode,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Bank.class)
                );
            } catch (IllegalArgumentException e) {
                throw new BankInfoLoadException("Failed to deserialize banks array from config", e);
            }
            convertDateOffsets();
            sortTransactionsByDate();
        }
    }

    private void convertDateOffsets() {
        LocalDate today = LocalDate.now();
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) continue;
            for (Account account : bank.getAccounts()) {
                convertTransactionDates(account, today);
                convertStandingOrderDates(account, today);
            }
        }
    }

    private void convertTransactionDates(Account account, LocalDate today) {
        if (account.getTransactions() == null) return;
        for (Transaction transaction : account.getTransactions()) {
            String dateValue = transaction.getDate();
            if (dateValue == null) continue;
            try {
                int daysAgo = Integer.parseInt(dateValue);
                transaction.setDate(today.minusDays(daysAgo).format(DATE_FORMATTER));
            } catch (NumberFormatException e) {
                // dateValue is already a formatted date string (e.g. "2024-03-15") — no conversion needed
            }
        }
    }

    private void convertStandingOrderDates(Account account, LocalDate today) {
        if (account.getStandingOrders() == null) return;
        for (StandingOrder order : account.getStandingOrders()) {
            String nextDateValue = order.getNextDate();
            if (nextDateValue == null) continue;
            try {
                int daysFromNow = Integer.parseInt(nextDateValue);
                order.setNextDate(today.plusDays(daysFromNow).format(DATE_FORMATTER));
            } catch (NumberFormatException e) {
                // nextDateValue is already a formatted date string (e.g. "2024-03-15") — no conversion needed
            }
        }
    }

    private void sortTransactionsByDate() {
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) continue;
            for (Account account : bank.getAccounts()) {
                if (account.getTransactions() != null && !account.getTransactions().isEmpty()) {
                    account.getTransactions().sort(byDateDescending());
                }
            }
        }
    }

    private void loadUserInfo(JsonNode rootNode) {
        JsonNode userNode = rootNode.get("user");
        if (userNode != null) {
            this.name = userNode.get("name").asText();
            this.image = userNode.get("image").asText();
            this.background = userNode.get("background").asText();
        }
    }

    private void loadColors(JsonNode rootNode) {
        JsonNode colorsNode = rootNode.get("colors");
        if (colorsNode == null) return;
        if (colorsNode.isArray()) {
            Map<String, Object> mergedColors = new HashMap<>();
            for (JsonNode colorObj : colorsNode) {
                colorObj.fields().forEachRemaining(
                        field -> mergedColors.put(field.getKey(), field.getValue().asText())
                );
            }
            this.colors = objectMapper.convertValue(mergedColors, CustomColors.class);
        } else {
            this.colors = objectMapper.convertValue(colorsNode, CustomColors.class);
        }
    }

    private void loadApplicationInfo(JsonNode rootNode) {
        JsonNode nameNode = rootNode.get("name");
        if (nameNode != null) {
            this.route = nameNode.get("route").asText();
            this.applicationName = nameNode.get("applicationName").asText();
        }
    }

    private void loadPayees(JsonNode rootNode) {
        JsonNode payeesNode = rootNode.get("payees");
        if (payeesNode != null && payeesNode.isArray()) {
            this.payees = objectMapper.convertValue(
                    payeesNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Payee.class)
            );
        }
    }

    private void loadTransactionHeaders(JsonNode rootNode) {
        JsonNode headersNode = rootNode.get("transactionTableHeaderData");
        if (headersNode != null) {
            this.transactionTableHeaders = objectMapper.convertValue(headersNode, TransactionTableHeaderData.class);
        }
    }

    private void loadStandingOrdersHeaders(JsonNode rootNode) throws BankInfoLoadException {
        JsonNode headersNode = rootNode.get("standingOrdersTableHeaderData");
        if (headersNode != null) {
            try {
                this.standingOrdersTableHeaders = objectMapper.convertValue(
                        headersNode, StandingOrdersTableHeaderData.class);
            } catch (IllegalArgumentException e) {
                throw new BankInfoLoadException("Failed to parse standingOrdersTableHeaderData", e);
            }
        }
    }

    private void loadAddAccountInfo(JsonNode rootNode) {
        JsonNode addAccountInfoNode = rootNode.get("addAccountInfo");
        if (addAccountInfoNode != null) {
            this.addAccountBankInfo = objectMapper.convertValue(addAccountInfoNode, AddAccountBankInfo.class);
        }
    }

    private void loadMockBankFromProperties() {
        try {
            String mockBankName = ConfigLoader.getMockBankName();
            String mockBankLogo = ConfigLoader.getMockBankLogo();
            if (!mockBankName.isEmpty() && !mockBankLogo.isEmpty()) {
                this.addAccountBankInfo = new AddAccountBankInfo(mockBankName, mockBankLogo);
            }
        } catch (IllegalStateException e) {
            // Mock bank properties are optional — skip silently if not configured
        }
    }

    public ConfigResponse getConfigurations() {
        List<Transaction> allTransactions = collectEnrichedTransactions();
        allTransactions.sort(byDateDescending());

        List<StandingOrder> allStandingOrders = collectEnrichedStandingOrders();
        allStandingOrders.sort(byDateAscending());

        return new ConfigResponse(
                this.banks,
                this.name,
                this.image,
                this.background,
                this.route,
                this.applicationName,
                this.colors,
                this.transactionTableHeaders,
                this.standingOrdersTableHeaders,
                this.payees,
                allTransactions,
                allStandingOrders
        );
    }

    private List<Transaction> collectEnrichedTransactions() {
        List<Transaction> result = new ArrayList<>();
        if (this.banks == null) return result;
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) continue;
            for (Account account : bank.getAccounts()) {
                if (account.getTransactions() == null) continue;
                for (Transaction transaction : account.getTransactions()) {
                    Transaction enriched = new Transaction();
                    enriched.setId(transaction.getId());
                    enriched.setDate(transaction.getDate());
                    enriched.setReference(transaction.getReference());
                    enriched.setAmount(transaction.getAmount());
                    enriched.setCurrency(transaction.getCurrency());
                    enriched.setCreditDebitStatus(transaction.getCreditDebitStatus());
                    enriched.setBank(bank.getName());
                    enriched.setAccount(account.getId());
                    result.add(enriched);
                }
            }
        }
        return result;
    }

    private List<StandingOrder> collectEnrichedStandingOrders() {
        List<StandingOrder> result = new ArrayList<>();
        if (this.banks == null) return result;
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) continue;
            for (Account account : bank.getAccounts()) {
                if (account.getStandingOrders() == null) continue;
                for (StandingOrder order : account.getStandingOrders()) {
                    StandingOrder enriched = new StandingOrder();
                    enriched.setId(order.getId());
                    enriched.setReference(order.getReference());
                    enriched.setNextDate(order.getNextDate());
                    enriched.setStatus(order.getStatus());
                    enriched.setAmount(order.getAmount());
                    enriched.setCurrency(order.getCurrency());
                    enriched.setBank(bank.getName());
                    enriched.setAccount(account.getId());
                    result.add(enriched);
                }
            }
        }
        return result;
    }

    public LoadPaymentPageResponse getPaymentPageInfo() {
        List<BankInfoInPayments> bankInfoInPayments = this.banks.stream()
                .flatMap(bank -> bank.getAccounts().stream()
                        .map(account -> new BankInfoInPayments(bank.getName(), account.getId())))
                .collect(Collectors.toList());
        return new LoadPaymentPageResponse(bankInfoInPayments, this.payees, this.currencies);
    }

    public List<AddAccountBankInfo> getAddAccountBanksInformation() throws BankInfoLoadException {
        if (this.banks == null) {
            throw new BankInfoLoadException("Banks not loaded. Call loadBanks() first.");
        }
        Map<String, AddAccountBankInfo> uniqueBanks = new LinkedHashMap<>();
        this.banks.forEach(bank -> uniqueBanks.putIfAbsent(
                bank.getName(),
                new AddAccountBankInfo(bank.getName(), bank.getImage())
        ));
        if (this.addAccountBankInfo != null) {
            uniqueBanks.putIfAbsent(this.addAccountBankInfo.getName(), this.addAccountBankInfo);
        }
        return new ArrayList<>(uniqueBanks.values());
    }

    private Comparator<Transaction> byDateDescending() {
        return (t1, t2) -> {
            try {
                return LocalDate.parse(t2.getDate(), DATE_FORMATTER)
                        .compareTo(LocalDate.parse(t1.getDate(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                // If either date is unparseable, treat the two entries as equal in sort order
                return 0;
            }
        };
    }

    private Comparator<StandingOrder> byDateAscending() {
        return (o1, o2) -> {
            try {
                return LocalDate.parse(o1.getNextDate(), DATE_FORMATTER)
                        .compareTo(LocalDate.parse(o2.getNextDate(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                // If either date is unparseable, treat the two entries as equal in sort order
                return 0;
            }
        };
    }

    List<Bank> getBanks() { return banks; }
    void addBank(Bank bank) { this.banks.add(bank); }
    boolean isBankExists(String bankName) {
        return this.banks.stream().anyMatch(bank -> bankName.equals(bank.getName()));
    }

    Optional<Account> findAccount(String bankName, String accountId) {
        return this.banks.stream()
                .filter(bank -> bank.getName().equals(bankName))
                .flatMap(bank -> bank.getAccounts().stream())
                .filter(account -> account.getId().equals(accountId))
                .findFirst();
    }

    void addTransactionToAccount(Account account, Transaction transaction) {
        List<Transaction> transactions = account.getTransactions();
        if (transactions == null) {
            transactions = new ArrayList<>();
            account.setTransactions(transactions);
        }
        transactions.add(0, transaction);
        transactions.sort(byDateDescending());
    }
}
