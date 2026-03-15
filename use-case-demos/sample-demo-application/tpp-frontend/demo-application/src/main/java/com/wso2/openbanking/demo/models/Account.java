package com.wso2.openbanking.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account with its associated details, transactions, and standing orders.
 * Unknown JSON properties are ignored during deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private String id;
    private String name;
    private Double balance;
    private List<Transaction> transactions;
    private List<StandingOrder> standingOrders;
    private String bank;
    private String account;



    /**
     * Returns the name of the bank associated with this account.
     *
     * @return the bank name
     */
    public String getBank() {
        return bank;
    }

    /**
     * Sets the name of the bank associated with this account.
     *
     * @param bank the bank name to set
     */
    public void setBank(String bank) {
        this.bank = bank;
    }

    /**
     * Returns the account identifier string.
     *
     * @return the account identifier
     */
    public String getAccount() {
        return account;
    }

    /**
     * Sets the account identifier string.
     *
     * @param account the account identifier to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * Constructs an empty Account instance.
     */
    public Account() {
    }

    /**
     * Constructs an Account with basic details and a list of transactions.
     *
     * @param id           the unique identifier of the account
     * @param name         the account holder's name
     * @param balance      the current balance of the account
     * @param transactions the list of transactions associated with the account
     */
    public Account(String id, String name, Double balance, List<Transaction> transactions) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.transactions = transactions == null ? null : new ArrayList<>(transactions);
    }

    /**
     * Returns the unique identifier of the account.
     *
     * @return the account ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the account.
     *
     * @param id the account ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the account holder's name.
     *
     * @return the account name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the account holder's name.
     *
     * @param name the account name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the current balance of the account.
     *
     * @return the account balance
     */
    public Double getBalance() {
        return balance;
    }

    /**
     * Sets the current balance of the account.
     *
     * @param balance the balance to set
     */
    public void setBalance(Double balance) {
        this.balance = balance;
    }

    /**
     * Returns the list of transactions associated with the account.
     *
     * @return the list of transactions
     */
    public List<Transaction> getTransactions() {
        return transactions == null ? null : new ArrayList<>(transactions);
    }

    public List<StandingOrder> getStandingOrders() {
        return standingOrders == null ? null : new ArrayList<>(standingOrders);
    }

    public void setStandingOrders(List<StandingOrder> standingOrders) {
        this.standingOrders = standingOrders == null ? null : new ArrayList<>(standingOrders);
    }

    public void addTransaction(Transaction transaction) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        transactions.add(transaction);
    }
}
