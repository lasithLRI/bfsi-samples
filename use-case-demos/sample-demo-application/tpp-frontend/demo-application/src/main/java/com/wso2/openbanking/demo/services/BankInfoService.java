package com.wso2.openbanking.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.models.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class BankInfoService {

    private static final String CONFIG_FILE = "config.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    private List<Bank> banks;
    private List<Payee> payees;
    private AddAccountBankInfo addAccountBankInfo;



    public void loadBanks() throws BankInfoLoadException, IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new BankInfoLoadException("Config file not found on classpath: " + CONFIG_FILE);
            }
            if (this.banks == null) {
                JsonNode rootNode = objectMapper.readTree(inputStream);
                loadBanksFromJson(rootNode);
                loadPayees(rootNode);
                loadAddAccountInfo(rootNode);
            }
        } catch (IOException e) {
            throw new BankInfoLoadException("Failed to read or parse config file: " + CONFIG_FILE, e);
        }
    }

    private void loadAddAccountInfo(JsonNode rootNode) {
        JsonNode addAccountInfoNode = rootNode.get("addAccountInfo");
        if (addAccountInfoNode != null) {
            this.addAccountBankInfo = objectMapper
                    .convertValue(addAccountInfoNode, AddAccountBankInfo.class);

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

    private void sortTransactionsByDate() {
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) {
                continue;
            }
            for (Account account : bank.getAccounts()) {
                if (account.getTransactions() != null
                        && !account.getTransactions()
                        .isEmpty()) {
                    account.getTransactions()
                            .sort(byDateDescending());
                }
            }
        }
    }

    

    private void convertDateOffsets() throws BankInfoLoadException {
        LocalDate today = LocalDate.now();
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) {
                continue;
            }
            for (Account account : bank.getAccounts()) {
                convertTransactionDates(account, today);
                convertStandingOrderDates(account, today);
            }
        }
    }

    private void convertStandingOrderDates(Account account, LocalDate today) throws BankInfoLoadException {
        if (account.getStandingOrders() == null) {
            return;
        }
        for (StandingOrder order : account.getStandingOrders()) {
            String nextDateValue = order.getNextDate();
            if (nextDateValue == null) {
                continue;
            }
            try {
                int daysFromNow = Integer.parseInt(nextDateValue);
                order.setNextDate(today.plusDays(daysFromNow).format(DATE_FORMATTER));
            } catch (NumberFormatException e) {
                throw new BankInfoLoadException("Invalid next date value '"
                        + nextDateValue + "' for standing order in account "
                        + account.getId());
            }
        }
    }

    private void convertTransactionDates(Account account, LocalDate today) throws BankInfoLoadException {
        if (account.getTransactions() == null) {
            return;
        }
        for (Transaction transaction : account.getTransactions()) {
            String dateValue = transaction.getDate();
            if (dateValue == null) {
                continue;
            }
            try {
                int daysAgo = Integer.parseInt(dateValue);
                transaction.setDate(today.minusDays(daysAgo).format(DATE_FORMATTER));
            } catch (NumberFormatException e) {
                throw new BankInfoLoadException("Invalid date value '"
                        + dateValue + "' for transaction in account "
                        + account.getId());
            }
        }
    }

    public ConfigResponse getConfigurations() {
        List<Transaction> allTransactions = collectTransactions();
        allTransactions.sort(byDateDescending());

        List<StandingOrder> allStandingOrders = collectStandingOrders();
        allStandingOrders.sort(byStandingOrderDateDescending());

        return new ConfigResponse(
                this.banks,
                this.payees,
                allTransactions,
                allStandingOrders
        );
    }

    private Comparator<StandingOrder> byStandingOrderDateDescending() {
        return (o1, o2) -> {
            try {
                return LocalDate.parse(o2.getNextDate(), DATE_FORMATTER)
                        .compareTo(LocalDate.parse(o1.getNextDate(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                return 0;
            }
        };
    }

    private List<StandingOrder> collectStandingOrders() {
        List<StandingOrder> result = new ArrayList<>();
        if (this.banks == null) {
            return result;
        }
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) {
                continue;
            }
            for (Account account : bank.getAccounts()) {
                if (account.getStandingOrders() == null) {
                    continue;
                }
                for (StandingOrder order : account.getStandingOrders()) {
                    result.add(copyWithContext(bank, account, order));
                }
            }
        }
        return result;

    }

    private List<Transaction> collectTransactions () {
        List<Transaction> result = new ArrayList<>();
        if (this.banks == null) {
            return result;
        }
        for (Bank bank : this.banks) {
            if (bank.getAccounts() == null) {
                continue;
            }
            for (Account account : bank.getAccounts()) {
                if (account.getTransactions() == null) {
                    continue;
                }
                for (Transaction transaction : account.getTransactions()) {
                    result.add(copyWithContext(bank, account, transaction));
                }
            }
        }
        return result;
    }

    private static Transaction copyWithContext (Bank bank,
            Account account,
            Transaction transaction){
        Transaction copy = new Transaction();
        copy.setId(transaction.getId());
        copy.setDate(transaction.getDate());
        copy.setReference(transaction.getReference());
        copy.setAmount(transaction.getAmount());
        copy.setCurrency(transaction.getCurrency());
        copy.setCreditDebitStatus(transaction.getCreditDebitStatus());
        copy.setBank(bank.getName());
        copy.setAccount(account.getId());
        return copy;
    }

    private Comparator<Transaction> byDateDescending () {
        return (t1, t2) -> {
            try {
                return LocalDate.parse(t2.getDate(), DATE_FORMATTER)
                        .compareTo(LocalDate.parse(t1.getDate(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                return 0;
            }
        };
    }


    private static StandingOrder copyWithContext (Bank bank, Account account, StandingOrder order){
        StandingOrder copy = new StandingOrder();
        copy.setId(order.getId());
        copy.setReference(order.getReference());
        copy.setNextDate(order.getNextDate());
        copy.setStatus(order.getStatus());
        copy.setAmount(order.getAmount());
        copy.setCurrency(order.getCurrency());
        copy.setBank(bank.getName());
        copy.setAccount(account.getId());
        return copy;
    }


    public void registerConsentForAccount(String consentId, String accountId) {
        if (banks == null) {
            return;
        }
        for (Bank bank : banks) {
            if (bank.getAccounts() != null &&
                    bank.getAccounts().stream()
                            .anyMatch(a -> a.getId().equals(accountId))) {
                bank.registerConsent(consentId, accountId);
                return;
            }
        }

    }

    public boolean revokeConsentAndRemoveAccount(String consentId) {
        if (banks == null) {
            return false;
        }

        for (Bank bank : banks) {
            List<String> accountIds = bank.removeConsent(consentId);

            if (accountIds != null && !accountIds.isEmpty()) {
                return processRemovedAccounts(bank, consentId, accountIds);
            }
        }
        return false;
    }

    private boolean processRemovedAccounts(Bank bank, String consentId, List<String> accountIds) {
        List<String> removed = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        categorizeAccountRemovals(bank, accountIds, removed, missing);

        return !removed.isEmpty();
    }

    private void categorizeAccountRemovals(Bank bank, List<String> accountIds,
                                           List<String> removed, List<String> missing) {
        for (String accountId : accountIds) {
            boolean wasRemoved = bank.removeAccount(accountId);
            (wasRemoved ? removed : missing).add(accountId);
        }
    }

    public List<Bank> getBanks() {
        if (banks == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(banks);
    }

    boolean isBankExists(String bankName) {
        return this.banks.stream().anyMatch(bank -> bankName.equals(bank.getName()));
    }

    void addBank(Bank bank) {
        this.banks.add(bank);
    }


}


