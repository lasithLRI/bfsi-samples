package com.wso2.openbanking.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a bank account transaction, including its date, reference,
 * amount, currency, and credit/debit status.
 * Unknown JSON properties are ignored during deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private String id;
    private String date;
    private String reference;
    private String amount;
    private String currency;
    private String creditDebitStatus;
    private String bank;
    private String account;

    /**
     * Constructs a Transaction with the core transaction details.
     *
     * @param id               the unique identifier of the transaction
     * @param date             the date the transaction occurred
     * @param reference        the payment reference or description
     * @param amount           the transaction amount
     * @param currency         the currency code for the transaction
     * @param creditDebitStatus indicates whether the transaction is a credit or debit
     */
    public Transaction(String id, String date,
                       String reference, String amount,
                       String currency, String creditDebitStatus) {
        this.id = id;
        this.date = date;
        this.reference = reference;
        this.amount = amount;
        this.currency = currency;
        this.creditDebitStatus = creditDebitStatus;
    }

    /**
     * Constructs an empty Transaction instance.
     */
    public Transaction() {
    }

    /**
     * Returns the unique identifier of the transaction.
     *
     * @return the transaction ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the transaction.
     *
     * @param id the transaction ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the date the transaction occurred.
     *
     * @return the transaction date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date the transaction occurred.
     *
     * @param date the transaction date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Returns the payment reference or description for the transaction.
     *
     * @return the payment reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the payment reference or description for the transaction.
     *
     * @param reference the payment reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Returns the transaction amount.
     *
     * @return the transaction amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     * Sets the transaction amount.
     *
     * @param amount the transaction amount to set
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * Returns the currency code for the transaction.
     *
     * @return the currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency code for the transaction.
     *
     * @param currency the currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns whether the transaction is a credit or debit.
     *
     * @return the credit/debit status
     */
    public String getCreditDebitStatus() {
        return creditDebitStatus;
    }

    /**
     * Sets whether the transaction is a credit or debit.
     *
     * @param creditDebitStatus the credit/debit status to set
     */
    public void setCreditDebitStatus(String creditDebitStatus) {
        this.creditDebitStatus = creditDebitStatus;
    }

    /**
     * Returns the name of the bank associated with the transaction.
     *
     * @return the bank name
     */
    public String getBank() {
        return bank;
    }

    /**
     * Sets the name of the bank associated with the transaction.
     *
     * @param bank the bank name to set
     */
    public void setBank(String bank) {
        this.bank = bank;
    }

    /**
     * Returns the account number the transaction belongs to.
     *
     * @return the account number
     */
    public String getAccount() {
        return account;
    }

    /**
     * Sets the account number the transaction belongs to.
     *
     * @param account the account number to set
     */
    public void setAccount(String account) {
        this.account = account;
    }
}
