package com.wso2.openbanking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    String id;
    String date;
    String reference;
    String amount;
    String currency;
    String creditDebitStatus;

    public Transaction(String id, String date, String reference, String amount, String currency, String creditDebitStatus) {
        this.id = id;
        this.date = date;
        this.reference = reference;
        this.amount = amount;
        this.currency = currency;
        this.creditDebitStatus = creditDebitStatus;
    }

    public Transaction() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    public String getCreditDebitStatus() {
        return creditDebitStatus;
    }

    public void setCreditDebitStatus(String creditDebitStatus) {
        this.creditDebitStatus = creditDebitStatus;
    }
}
