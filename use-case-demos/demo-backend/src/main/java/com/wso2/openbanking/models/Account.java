package com.wso2.openbanking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private String id;
    private String name;
    private Double balance;
    private List<Transaction> transactions;
    private List<StandingOrder> standingOrders;
    private String bank;
    private String account;

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Account() {
    }

    public Account(String id, String name, Double balance, List<Transaction> transactions) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.transactions = transactions;
    }

    public Account(String id, String name, Double balance, List<Transaction> transactions, List<StandingOrder> standingOrders) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.transactions = transactions;
        this.standingOrders = standingOrders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<StandingOrder> getStandingOrders() {
        return standingOrders;
    }

    public void setStandingOrders(List<StandingOrder> standingOrders) {
        this.standingOrders = standingOrders;
    }
}
