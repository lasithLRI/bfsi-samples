package com.wso2.openbanking.models;

import java.util.List;
import java.util.Map;

public class ConfigResponse {
    private List<Bank> banks;
    private String name;
    private String image;
    private String background;
    private String route;
    private String applicationName;
    public CustomColors colors;
    private List<Payee> payees;

    private TransactionTableHeaderData transactionTableHeaderData;

    public void setColors(CustomColors colors) {
        this.colors = colors;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public TransactionTableHeaderData getTransactionTableHeaderData() {
        return transactionTableHeaderData;
    }

    public void setTransactionTableHeaderData(TransactionTableHeaderData transactionTableHeaderData) {
        this.transactionTableHeaderData = transactionTableHeaderData;
    }

    public ConfigResponse(List<Bank> banks, String name, String image, String background, String route, String appName, CustomColors colors, TransactionTableHeaderData transactionTableHeaderData, List<Payee> payees) {
        this.banks = banks;
        this.name = name;
        this.image = image;
        this.background = background;
        this.route = route;
        this.applicationName = appName;
        this.colors = colors;
        this.payees= payees;
        this.transactionTableHeaderData = transactionTableHeaderData;
    }


    public ConfigResponse() {
    }

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

    public String getAppName() {
        return applicationName;
    }

    public void setAppName(String appName) {
        this.applicationName = appName;
    }

    public CustomColors getColors() {
        return colors;
    }

    public List<Payee> getPayees() {
        return payees;
    }
}
