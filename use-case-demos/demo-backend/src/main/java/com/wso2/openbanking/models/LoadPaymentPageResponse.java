package com.wso2.openbanking.models;

import java.util.List;

public class LoadPaymentPageResponse {
    List<BankInfoInPayments> banks;
    List<Payee> payees;
    List<String> currencies;

    public LoadPaymentPageResponse(List<BankInfoInPayments> banks, List<Payee> payees, List<String> currencies) {
        this.banks = banks;
        this.payees = payees;
        this.currencies = currencies;
    }

    public List<BankInfoInPayments> getBanks() {
        return banks;
    }

    public void setBanks(List<BankInfoInPayments> banks) {
        this.banks = banks;
    }

    public List<Payee> getPayees() {
        return payees;
    }

    public void setPayees(List<Payee> payees) {
        this.payees = payees;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<String> currencies) {
        this.currencies = currencies;
    }
}
