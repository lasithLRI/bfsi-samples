package com.wso2.openbanking.models;

import java.util.List;

public class ConfigResponse {
    private List<Bank> banks;
    private String name;
    private String image;
    private String background;
    private String route;
    private String applicationName;
    private CustomColors colors;
    private List<Payee> payees;
    private TransactionTableHeaderData transactionTableHeaderData;
    private StandingOrdersTableHeaderData standingOrdersTableHeaderData;
    private List<Transaction> transactions;
    private List<StandingOrder> standingOrders;

    public ConfigResponse() {
    }

    public ConfigResponse(List<Bank> banks, String name, String image, String background,
                          String route, String appName, CustomColors colors,
                          TransactionTableHeaderData transactionTableHeaderData,
                          StandingOrdersTableHeaderData standingOrdersTableHeaderData,
                          List<Payee> payees,
                          List<Transaction> transactions,
                          List<StandingOrder> standingOrders) {
        this.banks = banks;
        this.name = name;
        this.image = image;
        this.background = background;
        this.route = route;
        this.applicationName = appName;
        this.colors = colors;
        this.transactionTableHeaderData = transactionTableHeaderData;
        this.standingOrdersTableHeaderData = standingOrdersTableHeaderData;
        this.payees = payees;
        this.transactions = transactions;
        this.standingOrders = standingOrders;
    }

    // Getters and Setters
    public List<Bank> getBanks() {
        return banks;
    }

    public void setBanks(List<Bank> banks) {
        this.banks = banks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAppName() {
        return applicationName;
    }

    public void setAppName(String appName) {
        this.applicationName = appName;
    }

    public CustomColors getColors() {
        return colors;
    }

    public void setColors(CustomColors colors) {
        this.colors = colors;
    }

    public List<Payee> getPayees() {
        return payees;
    }

    public void setPayees(List<Payee> payees) {
        this.payees = payees;
    }

    public TransactionTableHeaderData getTransactionTableHeaderData() {
        return transactionTableHeaderData;
    }

    public void setTransactionTableHeaderData(TransactionTableHeaderData transactionTableHeaderData) {
        this.transactionTableHeaderData = transactionTableHeaderData;
    }

    public StandingOrdersTableHeaderData getStandingOrdersTableHeaderData() {
        return standingOrdersTableHeaderData;
    }

    public void setStandingOrdersTableHeaderData(StandingOrdersTableHeaderData standingOrdersTableHeaderData) {
        this.standingOrdersTableHeaderData = standingOrdersTableHeaderData;
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
