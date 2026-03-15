package com.wso2.openbanking.demo.models;

/**
 * Represents a payee available for selection during payment processing,
 * including their name, associated bank, and account number.
 */
public class Payee {
    String name;
    String bank;
    String accountNumber;

    /**
     * Constructs an empty Payee instance.
     */
    public Payee() {
    }

    /**
     * Returns the name of the payee.
     *
     * @return the payee name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the payee.
     *
     * @param name the payee name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the bank associated with the payee.
     *
     * @return the bank name
     */
    public String getBank() {
        return bank;
    }

    /**
     * Sets the name of the bank associated with the payee.
     *
     * @param bank the bank name to set
     */
    public void setBank(String bank) {
        this.bank = bank;
    }

    /**
     * Returns the account number of the payee.
     *
     * @return the account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
