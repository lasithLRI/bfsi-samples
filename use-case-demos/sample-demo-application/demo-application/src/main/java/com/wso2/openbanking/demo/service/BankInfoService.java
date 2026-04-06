/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.models.Account;
import com.wso2.openbanking.demo.models.AddAccountBankInfo;
import com.wso2.openbanking.demo.models.Bank;
import com.wso2.openbanking.demo.models.BankInfoInPayments;
import com.wso2.openbanking.demo.models.ConfigResponse;
import com.wso2.openbanking.demo.models.LoadPaymentPageResponse;
import com.wso2.openbanking.demo.models.Payee;
import com.wso2.openbanking.demo.models.StandingOrder;
import com.wso2.openbanking.demo.models.Transaction;
import com.wso2.openbanking.demo.utils.ConfigLoader;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** BankInfoService implementation. */
public final class BankInfoService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONFIG_FILE = "config.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

//    private List<Bank> banks;
//
//    private List<Payee> payees;
//    private final List<String> currencies = new ArrayList<>(Arrays.asList("USD", "EURO", "GBP"));
//    private AddAccountBankInfo addAccountBankInfo;
//
//    /**
//     * Executes the loadBanks operation and modify the payload if necessary.
//     *
//     * @throws BankInfoLoadException When an error occurs during the operation
//     */
//    public void loadBanks() throws BankInfoLoadException {
//        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
//            if (inputStream == null) {
//                throw new BankInfoLoadException("Config file not found on classpath: " + CONFIG_FILE);
//            }
////            if (this.banks == null) {
////                JsonNode rootNode = objectMapper.readTree(inputStream);
//////                loadBanksFromJson(rootNode);
//////                loadPayees(rootNode);
//////                loadAddAccountInfo(rootNode);
//////                loadMockBankFromProperties();
////            }
//        } catch (IOException e) {
//            throw new BankInfoLoadException("Failed to read or parse config file: " + CONFIG_FILE, e);
//        }
//    }
//
//    /**
//     * Executes the loadPayees operation and modify the payload if necessary.
//     *
//     * @param rootNode        The rootNode parameter
//     */
////    private void loadPayees(JsonNode rootNode) {
////        JsonNode payeesNode = rootNode.get("payees");
////        if (payeesNode != null && payeesNode.isArray()) {
////            this.payees = objectMapper.convertValue(
////                    payeesNode,
////                    objectMapper.getTypeFactory().constructCollectionType(List.class, Payee.class)
////            );
////        }
////    }
//
//    /**
//     * Executes the loadAddAccountInfo operation and modify the payload if necessary.
//     *
//     * @param rootNode        The rootNode parameter
//     */
//    private void loadAddAccountInfo(JsonNode rootNode) {
//        JsonNode addAccountInfoNode = rootNode.get("addAccountInfo");
//        if (addAccountInfoNode != null) {
//            this.addAccountBankInfo = objectMapper.convertValue(addAccountInfoNode, AddAccountBankInfo.class);
//        }
//    }
//
//    /**
//     * Executes the loadMockBankFromProperties operation and modify the payload if necessary.
//     */
//    private void loadMockBankFromProperties() {
//        try {
//            String mockBankName = ConfigLoader.getMockBankName();
//            String mockBankLogo = ConfigLoader.getMockBankLogo();
//            if (!mockBankName.isEmpty() && !mockBankLogo.isEmpty()) {
//                this.addAccountBankInfo = new AddAccountBankInfo(mockBankName, mockBankLogo);
//            }
//        } catch (IllegalStateException e) {
//
//        }
//    }
//
//    /**
//     * Executes the byDateDescending operation and modify the payload if necessary.
//     */
//    private Comparator<Transaction> byDateDescending() {
//        return (t1, t2) -> {
//            try {
//                return LocalDate.parse(t2.getDate(), DATE_FORMATTER)
//                        .compareTo(LocalDate.parse(t1.getDate(), DATE_FORMATTER));
//            } catch (DateTimeParseException e) {
//
//                return 0;
//            }
//        };
//    }
//
//
//    /**
//     * Executes the getBanks operation and modify the payload if necessary.
//     */
//    public List<Bank> getBanks() {
//        return banks == null ? null : new ArrayList<>(banks);
//    }
//
//    public void addBank(Bank bank) {
//        this.banks.add(bank);
//    }
//
//    public Optional<Account> findAccount(String bankName, String accountId) {
//        return this.banks.stream()
//                .filter(bank -> bank.getName().equals(bankName))
//                .flatMap(bank -> bank.getAccounts().stream())
//                .filter(account -> account.getId().equals(accountId))
//                .findFirst();
//    }

//    public void addTransactionToAccount(Account account, Transaction transaction) {
//        List<Transaction> transactions = account.getTransactions();
//        if (transactions == null) {
//            transactions = new ArrayList<>();
//            account.setTransactions(transactions);
//        }
//        transactions.add(0, transaction);
//        transactions.sort(byDateDescending());
//        account.setTransactions(transactions);
//    }

//    public Map<String, Object> getConsentGroupForMockBank() {
//        if (this.banks == null) return null;
//
//        Bank mockBank = this.banks.stream()
//                .filter(bank -> bank.getName().equals(ConfigLoader.getMockBankName()))
//                .findFirst()
//                .orElse(null);
//
//        if (mockBank == null || mockBank.getAccounts() == null || mockBank.getAccounts().isEmpty()) {
//            return null;
//        }
//
//        String consentId = mockBank.getAccounts().stream()
//                .map(Account::getConsentId)
//                .filter(id -> id != null && !id.isEmpty())
//                .findFirst()
//                .orElse(null);
//
//        if (consentId == null) return null;
//
//        List<Map<String, String>> accountList = new ArrayList<>();
//        for (Account acc : mockBank.getAccounts()) {
//            Map<String, String> a = new LinkedHashMap<>();
//            a.put("id", acc.getId());
//            a.put("name", acc.getName());
//            accountList.add(a);
//        }
//
//        Map<String, Object> group = new LinkedHashMap<>();
//        group.put("consentId", consentId);
//        group.put("bankName",  mockBank.getName());
//        group.put("accounts",  accountList);
//        return group;
//    }
//
//    public void replaceAccountsForBank(String bankName, List<Account> newAccounts) {
//        if (this.banks == null) return;
//        this.banks.stream()
//                .filter(bank -> bank.getName().equals(bankName))
//                .findFirst()
//                .ifPresent(bank -> {
//                    List<Account> accounts = bank.getAccounts();
//                    if (accounts != null) {
//                        accounts.clear();
//                        accounts.addAll(newAccounts);
//                    }
//                });
//    }
}
