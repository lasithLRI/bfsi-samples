package com.wso2.openbanking.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.models.*;
import com.wso2.openbanking.demo.utils.ConfigLoader;

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
    private List<Payee> payees;
    private final List<String> currencies = new ArrayList<>(Arrays.asList("USD", "EURO", "GBP"));
    private AddAccountBankInfo addAccountBankInfo;

    public BankInfoService() {
        // No initialization required: all fields are populated lazily via loadBanks()
    }

    /**
     * Reads config.json and populates all service state.
     * Subsequent calls are no-ops if banks are already loaded.
     */
    public void loadBanks() throws BankInfoLoadException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new BankInfoLoadException("Config file not found on classpath: " + CONFIG_FILE);
            }
            if (this.banks == null) {
                JsonNode rootNode = objectMapper.readTree(inputStream);
                loadBanksFromJson(rootNode);
                loadUserInfo(rootNode);
                loadApplicationInfo(rootNode);
                loadPayees(rootNode);
                loadAddAccountInfo(rootNode);
                loadMockBankFromProperties();
            }
        } catch (IOException e) {
            throw new BankInfoLoadException("Failed to read or parse config file: " + CONFIG_FILE, e);
        }
    }

    /** Deserializes the banks array, then triggers date conversion and sorting. */
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

    /** Converts relative day offsets in transaction and standing order dates to absolute date strings. */
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

    /** Converts transaction dates that are stored as integer day offsets (e.g. "30" → 30 days ago). */
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

    /** Converts standing order next-dates that are stored as integer day offsets (e.g. "7" → 7 days from now). */
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

    /** Sorts transactions across all accounts by date, most recent first. */
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

    /** Loads user profile fields — name, avatar image, and background. */
    private void loadUserInfo(JsonNode rootNode) {
        JsonNode userNode = rootNode.get("user");
        if (userNode != null) {
            this.name = userNode.get("name").asText();
            this.image = userNode.get("image").asText();
            this.background = userNode.get("background").asText();
        }
    }

    /** Loads the application route and display name. */
    private void loadApplicationInfo(JsonNode rootNode) {
        JsonNode nameNode = rootNode.get("name");
        if (nameNode != null) {
            this.route = nameNode.get("route").asText();
            this.applicationName = nameNode.get("applicationName").asText();
        }
    }

    /** Loads the list of known payees available for payments. */
    private void loadPayees(JsonNode rootNode) {
        JsonNode payeesNode = rootNode.get("payees");
        if (payeesNode != null && payeesNode.isArray()) {
            this.payees = objectMapper.convertValue(
                    payeesNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Payee.class)
            );
        }
    }

    /** Loads the bank entry shown in the add-account flow from config. */
    private void loadAddAccountInfo(JsonNode rootNode) {
        JsonNode addAccountInfoNode = rootNode.get("addAccountInfo");
        if (addAccountInfoNode != null) {
            this.addAccountBankInfo = objectMapper.convertValue(addAccountInfoNode, AddAccountBankInfo.class);
        }
    }

    /**
     * Overrides addAccountBankInfo with the mock bank defined in application.properties.
     * Skipped silently if the properties are not configured.
     */
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

    /**
     * Builds the full configuration response for the frontend, including all
     * transactions and standing orders sorted and enriched with bank and account info.
     */
    public ConfigResponse getConfigurations() {
        List<Transaction> allTransactions = collectEnrichedTransactions();
        allTransactions.sort(byDateDescending());

        List<StandingOrder> allStandingOrders = collectEnrichedStandingOrders();
        allStandingOrders.sort(byDateAscending());

        return new ConfigResponse(
                this.banks,
                this.payees,
                allTransactions,
                allStandingOrders
        );
    }

    /**
     * Flattens transactions from all banks and accounts into a single list,
     * stamping each entry with its bank name and account ID.
     */
    private List<Transaction> collectEnrichedTransactions() {
        List<Transaction> result = new ArrayList<>();
        if (this.banks == null) return result;
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) continue;
            for (Account account : bank.getAccounts()) {
                if (account.getTransactions() == null) continue;
                for (Transaction transaction : account.getTransactions()) {
                    Transaction enriched = getTransaction(bank, account, transaction);
                    result.add(enriched);
                }
            }
        }
        return result;
    }

    /** Creates an enriched copy of a transaction stamped with its bank name and account ID. */
    private static Transaction getTransaction(Bank bank, Account account, Transaction transaction) {
        Transaction enriched = new Transaction();
        enriched.setId(transaction.getId());
        enriched.setDate(transaction.getDate());
        enriched.setReference(transaction.getReference());
        enriched.setAmount(transaction.getAmount());
        enriched.setCurrency(transaction.getCurrency());
        enriched.setCreditDebitStatus(transaction.getCreditDebitStatus());
        enriched.setBank(bank.getName());
        enriched.setAccount(account.getId());
        return enriched;
    }

    /**
     * Flattens standing orders from all banks and accounts into a single list,
     * stamping each entry with its bank name and account ID.
     */
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

    /** Builds the data needed to populate the payment page — accounts, payees, and currencies. */
    public LoadPaymentPageResponse getPaymentPageInfo() {
        List<BankInfoInPayments> bankInfoInPayments = this.banks.stream()
                .flatMap(bank -> bank.getAccounts().stream()
                        .map(account -> new BankInfoInPayments(bank.getName(), account.getId())))
                .collect(Collectors.toList());
        return new LoadPaymentPageResponse(bankInfoInPayments, this.payees, this.currencies);
    }

    /**
     * Returns the list of banks available for the add-account flow.
     * Deduplicates by bank name, with the mock bank appended if configured.
     */
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

    /** Comparator that orders transactions newest first. Unparseable dates are treated as equal. */
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

    /** Comparator that orders standing orders by next due date, earliest first. */
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

    public List<Bank> getBanks() { return banks; }
    void addBank(Bank bank) { this.banks.add(bank); }

    /** Returns true if a bank with the given name already exists in the loaded data. */
    boolean isBankExists(String bankName) {
        return this.banks.stream().anyMatch(bank -> bankName.equals(bank.getName()));
    }

    /** Finds a specific account by bank name and account ID. */
    Optional<Account> findAccount(String bankName, String accountId) {
        return this.banks.stream()
                .filter(bank -> bank.getName().equals(bankName))
                .flatMap(bank -> bank.getAccounts().stream())
                .filter(account -> account.getId().equals(accountId))
                .findFirst();
    }

    /** Prepends a new transaction to the account's list and re-sorts by date. */
    void addTransactionToAccount(Account account, Transaction transaction) {
        List<Transaction> transactions = account.getTransactions();
        if (transactions == null) {
            transactions = new ArrayList<>();
            account.setTransactions(transactions);
        }
        transactions.add(0, transaction);
        transactions.sort(byDateDescending());
    }

    /** Groups all accounts by ConsentId to render in the UI for deletion. */
    public List<Map<String, Object>> getAccountsGroupedByConsent() {
        if (this.banks == null) return Collections.emptyList();
        Map<String, Map<String, Object>> consentGroupMap = new LinkedHashMap<>();
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) continue;
            for (Account account : bank.getAccounts()) {
                String consentId = account.getConsentId();
                if (consentId == null || consentId.isEmpty()) continue;
                consentGroupMap.putIfAbsent(consentId, new HashMap<>());
                Map<String, Object> group = consentGroupMap.get(consentId);
                group.putIfAbsent("consentId", consentId);
                group.putIfAbsent("bankName", bank.getName());
                
                @SuppressWarnings("unchecked")
                List<Map<String, String>> accountsList = (List<Map<String, String>>) group.computeIfAbsent("accounts", k -> new ArrayList<>());
                Map<String, String> accountInfo = new HashMap<>();
                accountInfo.put("id", account.getId());
                accountInfo.put("name", account.getName());
                accountsList.add(accountInfo);
            }
        }
        return new ArrayList<>(consentGroupMap.values());
    }

    /** Retrieves the access token associated with a consentId, useful for revocation. */
    public String getAccessTokenForConsent(String consentId) {
        if (this.banks == null) return null;
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) continue;
            for (Account account : bank.getAccounts()) {
                if (consentId.equals(account.getConsentId())) {
                    return account.getAccessToken();
                }
            }
        }
        return null;
    }

    /** Removes all accounts tied to the specified consent ID. */
    public void deleteAccountsByConsentId(String consentId) {
        if (this.banks == null) return;
        for (Bank bank : this.banks) {
            if (bank.getAccounts() != null) {
                bank.getAccounts().removeIf(account -> consentId.equals(account.getConsentId()));
            }
        }
    }
}
