package com.wso2.openbanking.models;

public class Payment {
    private String userAccount;
    private String payeeAccount;
    private String currency;
    private String amount;
    private String reference;

    public Payment(String userAccount, String payeeAccount, String currency, String amount, String reference) {
        this.userAccount = userAccount;
        this.payeeAccount = payeeAccount;
        this.currency = currency;
        this.amount = amount;
        this.reference = reference;
    }

    public Payment() {
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(String payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "userAccount='" + userAccount + '\'' +
                ", payeeAccount='" + payeeAccount + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}
