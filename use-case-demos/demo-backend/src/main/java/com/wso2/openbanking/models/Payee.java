package com.wso2.openbanking.models;

public class Payee {
    String name;
    String bank;
    String accountNumber;

    public Payee(String name, String bank, String accountNumber) {
        this.name = name;
        this.bank = bank;
        this.accountNumber = accountNumber;
    }

    public Payee() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    

}
