package com.wso2.openbanking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingOrder {
    private String id;
    private String reference;
    private String bank;
    private String account;  // ADD THIS FIELD
    private String nextDate;
    private String status;
    private String amount;
    private String currency;

    public StandingOrder() {
    }

    public StandingOrder(String id, String reference, String bank, String account, String nextDate, String status, String amount, String currency) {
        this.id = id;
        this.reference = reference;
        this.bank = bank;
        this.account = account;  // ADD THIS
        this.nextDate = nextDate;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    // ADD THESE GETTER AND SETTER
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNextDate() {
        return nextDate;
    }

    public void setNextDate(String nextDate) {
        this.nextDate = nextDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
