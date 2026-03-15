package com.wso2.openbanking.demo.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the full configuration response returned to the frontend,
 * containing banks, accounts, transactions, standing orders, payees,
 * UI customization settings, and application metadata.
 */
public class ConfigResponse {
    private final List<Bank> banks;
    private final List<Payee> payees;
    private final List<Transaction> transactions;
    private final List<StandingOrder> standingOrders;

    /**
     * Constructs a ConfigResponse with all configuration fields.
     *
     * @param banks                        the list of banks available in the applicationthe application name

     * @param payees                       the list of available payees
     * @param transactions                 the list of transactions across all accounts
     * @param standingOrders               the list of standing orders across all accounts
     */
    public ConfigResponse(List<Bank> banks,
                          List<Payee> payees,
                          List<Transaction> transactions,
                          List<StandingOrder> standingOrders) {
        this.banks = banks == null ? null : new ArrayList<>(banks);
        this.payees = payees == null ? null : new ArrayList<>(payees);
        this.transactions = transactions == null ? null : new ArrayList<>(transactions);
        this.standingOrders = standingOrders == null ? null : new ArrayList<>(standingOrders);
    }

    /**
     * Returns the list of banks available in the application.
     *
     * @return the list of banks
     */
    public List<Bank> getBanks() {
        return banks == null ? null : new ArrayList<>(banks);
    }


    /**
     * Returns the list of available payees.
     *
     * @return the list of payees
     */
    public List<Payee> getPayees() {
        return payees == null ? null : new ArrayList<>(payees);
    }

    /**
     * Returns the list of transactions across all accounts.
     *
     * @return the list of transactions
     */
    public List<Transaction> getTransactions() {
        return transactions == null ? null : new ArrayList<>(transactions);
    }

    /**
     * Returns the list of standing orders across all accounts.
     *
     * @return the list of standing orders
     */
    public List<StandingOrder> getStandingOrders() {
        return standingOrders == null ? null : new ArrayList<>(standingOrders);
    }
}