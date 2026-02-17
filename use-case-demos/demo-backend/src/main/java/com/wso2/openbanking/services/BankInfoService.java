package com.wso2.openbanking.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.models.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                loadStandingOrdersHeaders(rootNode);
                loadAddAccountInfo(rootNode);
                loadMockBankFromProperties();
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

            // Convert date offsets to actual dates
            convertDateOffsets();

            // Sort transactions by date (latest first)
            sortTransactionsByDate();
        }
    }

    /**
     * Converts integer day offsets to actual date strings.
     * - Transaction dates: days ago from today (past dates)
     * - Standing order nextDate: days from today (future dates)
     */
    private void convertDateOffsets() {
        LocalDate today = LocalDate.now();

        System.out.println("Converting date offsets. Today is: " + today.format(DATE_FORMATTER));

        for (Bank bank : this.banks) {
            if (bank.getAccounts() != null) {
                for (Account account : bank.getAccounts()) {

                    // Convert transaction dates (past dates - subtract days)
                    if (account.getTransactions() != null) {
                        for (Transaction transaction : account.getTransactions()) {
                            String dateValue = transaction.getDate();
                            if (dateValue != null) {
                                try {
                                    int daysAgo = Integer.parseInt(dateValue);
                                    LocalDate transactionDate = today.minusDays(daysAgo);
                                    String formattedDate = transactionDate.format(DATE_FORMATTER);
                                    transaction.setDate(formattedDate);

                                    System.out.println("Converted transaction date: " + daysAgo + " days ago -> " + formattedDate);
                                } catch (NumberFormatException e) {
                                    // Already a date string, skip conversion
                                    System.out.println("Transaction date already formatted: " + dateValue);
                                }
                            }
                        }
                    }

                    // Convert standing order dates (future dates - add days)
                    if (account.getStandingOrders() != null) {
                        for (StandingOrder order : account.getStandingOrders()) {
                            String nextDateValue = order.getNextDate();
                            if (nextDateValue != null) {
                                try {
                                    int daysFromNow = Integer.parseInt(nextDateValue);
                                    LocalDate futureDate = today.plusDays(daysFromNow);
                                    String formattedDate = futureDate.format(DATE_FORMATTER);
                                    order.setNextDate(formattedDate);

                                    System.out.println("Converted standing order date: " + daysFromNow + " days from now -> " + formattedDate);
                                } catch (NumberFormatException e) {
                                    // Already a date string, skip conversion
                                    System.out.println("Standing order date already formatted: " + nextDateValue);
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Date conversion completed.");
    }

    /**
     * Sorts transactions by date in descending order (latest first)
     * within each account
     */
    private void sortTransactionsByDate() {
        System.out.println("Sorting transactions by date (latest first)...");

        for (Bank bank : this.banks) {
            if (bank.getAccounts() != null) {
                for (Account account : bank.getAccounts()) {
                    if (account.getTransactions() != null && !account.getTransactions().isEmpty()) {
                        List<Transaction> transactions = account.getTransactions();

                        transactions.sort((t1, t2) -> {
                            try {
                                LocalDate date1 = LocalDate.parse(t1.getDate(), DATE_FORMATTER);
                                LocalDate date2 = LocalDate.parse(t2.getDate(), DATE_FORMATTER);
                                // Descending order (latest first)
                                return date2.compareTo(date1);
                            } catch (Exception e) {
                                // If date parsing fails, maintain current order
                                System.err.println("WARNING: Could not parse dates for sorting: " +
                                        t1.getDate() + " or " + t2.getDate());
                                return 0;
                            }
                        });

                        System.out.println("Sorted " + transactions.size() + " transactions for account " + account.getId());
                    }
                }
            }
        }

        System.out.println("Transaction sorting completed.");
    }

    /**
     * Sorts standing orders by next date in ascending order (earliest first)
     * within each account
     */
    private void sortStandingOrdersByDate() {
        System.out.println("Sorting standing orders by next date (earliest first)...");

        for (Bank bank : this.banks) {
            if (bank.getAccounts() != null) {
                for (Account account : bank.getAccounts()) {
                    if (account.getStandingOrders() != null && !account.getStandingOrders().isEmpty()) {
                        List<StandingOrder> orders = account.getStandingOrders();

                        orders.sort((o1, o2) -> {
                            try {
                                LocalDate date1 = LocalDate.parse(o1.getNextDate(), DATE_FORMATTER);
                                LocalDate date2 = LocalDate.parse(o2.getNextDate(), DATE_FORMATTER);
                                // Ascending order (earliest first)
                                return date1.compareTo(date2);
                            } catch (Exception e) {
                                System.err.println("WARNING: Could not parse dates for sorting: " +
                                        o1.getNextDate() + " or " + o2.getNextDate());
                                return 0;
                            }
                        });

                        System.out.println("Sorted " + orders.size() + " standing orders for account " + account.getId());
                    }
                }
            }
        }

        System.out.println("Standing order sorting completed.");
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
        if (colorsNode != null) {
            if (colorsNode.isArray()) {
                // Handle array format - merge all objects into one
                Map<String, Object> mergedColors = new HashMap<>();
                for (JsonNode colorObj : colorsNode) {
                    Iterator<Map.Entry<String, JsonNode>> fields = colorObj.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        mergedColors.put(field.getKey(), field.getValue().asText());
                    }
                }
                this.colors = objectMapper.convertValue(mergedColors, CustomColors.class);
            } else {
                // Handle object format
                this.colors = objectMapper.convertValue(colorsNode, CustomColors.class);
            }
        }
    }

    private void loadApplicationInfo(JsonNode rootNode) {
        JsonNode routeNode = rootNode.get("name");
        if (routeNode != null) {
            this.route = routeNode.get("route").asText();
            this.applicationName = routeNode.get("applicationName").asText();
        }
    }

    private void loadPayees(JsonNode rootNode) {
        JsonNode payeesNode = rootNode.get("payees");
        if (payeesNode != null && payeesNode.isArray()) {
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

    private void loadStandingOrdersHeaders(JsonNode rootNode) {
        JsonNode standingOrdersHeadersNode = rootNode.get("standingOrdersTableHeaderData");

        System.out.println("DEBUG: standingOrdersHeadersNode = " + standingOrdersHeadersNode);

        if (standingOrdersHeadersNode != null) {
            System.out.println("DEBUG: standingOrdersHeadersNode content = " + standingOrdersHeadersNode.toString());

            try {
                this.standingOrdersTableHeaders = objectMapper.convertValue(
                        standingOrdersHeadersNode,
                        StandingOrdersTableHeaderData.class
                );
                System.out.println("DEBUG: Successfully loaded standing orders headers");
                System.out.println("DEBUG: Header fields = " + this.standingOrdersTableHeaders.getHeaderFields());
            } catch (Exception e) {
                System.err.println("ERROR: Failed to convert standingOrdersTableHeaderData");
                e.printStackTrace();
            }
        } else {
            System.out.println("WARNING: standingOrdersTableHeaderData not found in JSON");
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

            if (mockBankName != null && !mockBankName.isEmpty() &&
                    mockBankLogo != null && !mockBankLogo.isEmpty()) {
                this.addAccountBankInfo = new AddAccountBankInfo(mockBankName, mockBankLogo);
                System.out.println("Loaded mock bank from properties: " + mockBankName);
            }
        } catch (RuntimeException e) {
            System.out.println("Mock bank properties not configured, skipping: " + e.getMessage());
        }
    }

    public ConfigResponse getConfigurations() {
        // Collect all transactions with parent bank and account info
        List<Transaction> allTransactions = new ArrayList<>();
        if (this.banks != null) {
            for (Bank bank : this.banks) {
                if (bank.getAccounts() != null) {
                    for (Account account : bank.getAccounts()) {
                        if (account.getTransactions() != null) {
                            for (Transaction transaction : account.getTransactions()) {
                                Transaction enrichedTransaction = new Transaction();
                                enrichedTransaction.setId(transaction.getId());
                                enrichedTransaction.setDate(transaction.getDate());
                                enrichedTransaction.setReference(transaction.getReference());
                                enrichedTransaction.setAmount(transaction.getAmount());
                                enrichedTransaction.setCurrency(transaction.getCurrency());
                                enrichedTransaction.setCreditDebitStatus(transaction.getCreditDebitStatus());

                                // Set the ACTUAL parent bank and account (this account's info)
                                enrichedTransaction.setBank(bank.getName());
                                enrichedTransaction.setAccount(account.getId());

                                allTransactions.add(enrichedTransaction);
                            }
                        }
                    }
                }
            }
        }

        // Sort all transactions by date (latest first) for the flat list
        allTransactions.sort((t1, t2) -> {
            try {
                LocalDate date1 = LocalDate.parse(t1.getDate(), DATE_FORMATTER);
                LocalDate date2 = LocalDate.parse(t2.getDate(), DATE_FORMATTER);
                // Descending order (latest first)
                return date2.compareTo(date1);
            } catch (Exception e) {
                return 0;
            }
        });

        // Similar for standing orders...
        List<StandingOrder> allStandingOrders = new ArrayList<>();
        if (this.banks != null) {
            for (Bank bank : this.banks) {
                if (bank.getAccounts() != null) {
                    for (Account account : bank.getAccounts()) {
                        if (account.getStandingOrders() != null) {
                            for (StandingOrder standingOrder : account.getStandingOrders()) {
                                StandingOrder enrichedOrder = new StandingOrder();
                                enrichedOrder.setId(standingOrder.getId());
                                enrichedOrder.setReference(standingOrder.getReference());
                                enrichedOrder.setNextDate(standingOrder.getNextDate());
                                enrichedOrder.setStatus(standingOrder.getStatus());
                                enrichedOrder.setAmount(standingOrder.getAmount());
                                enrichedOrder.setCurrency(standingOrder.getCurrency());

                                // Set the ACTUAL parent bank and account (this account's info)
                                enrichedOrder.setBank(bank.getName());
                                enrichedOrder.setAccount(account.getId());

                                allStandingOrders.add(enrichedOrder);
                            }
                        }
                    }
                }
            }
        }

        // Sort all standing orders by next date (earliest first) for the flat list
        allStandingOrders.sort((o1, o2) -> {
            try {
                LocalDate date1 = LocalDate.parse(o1.getNextDate(), DATE_FORMATTER);
                LocalDate date2 = LocalDate.parse(o2.getNextDate(), DATE_FORMATTER);
                // Ascending order (earliest first)
                return date1.compareTo(date2);
            } catch (Exception e) {
                return 0;
            }
        });

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

    /**
     * Returns a unique list of banks for the "Add Account" page.
     * Removes duplicate bank entries to ensure each bank appears only once.
     */
    public List<AddAccountBankInfo> getAddAccountBanksInformation() {
        if (this.banks == null) {
            throw new RuntimeException("Banks list is not loaded. Call loadBanks() first.");
        }

        // Use LinkedHashMap to preserve insertion order while ensuring uniqueness
        Map<String, AddAccountBankInfo> uniqueBanks = new LinkedHashMap<>();

        // Add banks from config, ensuring uniqueness by bank name
        this.banks.forEach(bank -> {
            if (!uniqueBanks.containsKey(bank.getName())) {
                uniqueBanks.put(bank.getName(), new AddAccountBankInfo(bank.getName(), bank.getImage()));
            }
        });

        // Add mock bank info if available and not already present
        if (this.addAccountBankInfo != null) {
            String mockBankName = this.addAccountBankInfo.getName();
            if (!uniqueBanks.containsKey(mockBankName)) {
                uniqueBanks.put(mockBankName, this.addAccountBankInfo);
            }
        }

        List<AddAccountBankInfo> banksList = new ArrayList<>(uniqueBanks.values());

        System.out.println("Returning " + banksList.size() + " unique banks for add account page");
        banksList.forEach(bank -> System.out.println("  - " + bank.getName()));

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
        // Add at index 0 (top of list) - will be sorted later if needed
        transactions.add(0, transaction);

        // Re-sort to ensure latest transactions are always on top
        transactions.sort((t1, t2) -> {
            try {
                LocalDate date1 = LocalDate.parse(t1.getDate(), DATE_FORMATTER);
                LocalDate date2 = LocalDate.parse(t2.getDate(), DATE_FORMATTER);
                return date2.compareTo(date1); // Descending
            } catch (Exception e) {
                return 0;
            }
        });
    }
}
