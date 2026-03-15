package com.wso2.openbanking.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a standing order associated with a bank account,
 * including its schedule, amount, currency, and current status.
 * Unknown JSON properties are ignored during deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingOrder {
    private String id;
    private String reference;
    private String bank;
    private String account;
    private String nextDate;
    private String status;
    private String amount;
    private String currency;

    /**
     * Constructs an empty StandingOrder instance.
     */
    public StandingOrder() {
    }

    /**
     * Constructs a StandingOrder with all required details.
     *
     * @param id        the unique identifier of the standing order
     * @param reference the payment reference or description for the standing order
     * @param bank      the name of the bank associated with the standing order
     * @param account   the account number the standing order is linked to
     * @param nextDate  the date of the next scheduled payment
     * @param status    the current status of the standing order
     * @param amount    the amount to be transferred on each execution
     * @param currency  the currency code for the standing order
     */
    public StandingOrder(String id, String reference,
                         String bank, String account,
                         String nextDate, String status,
                         String amount, String currency) {
        this.id = id;
        this.reference = reference;
        this.bank = bank;
        this.account = account;
        this.nextDate = nextDate;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * Returns the unique identifier of the standing order.
     *
     * @return the standing order ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the standing order.
     *
     * @param id the standing order ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the payment reference or description for the standing order.
     *
     * @return the payment reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the payment reference or description for the standing order.
     *
     * @param reference the payment reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Returns the name of the bank associated with the standing order.
     *
     * @return the bank name
     */
    public String getBank() {
        return bank;
    }

    /**
     * Sets the name of the bank associated with the standing order.
     *
     * @param bank the bank name to set
     */
    public void setBank(String bank) {
        this.bank = bank;
    }

    /**
     * Returns the account number the standing order is linked to.
     *
     * @return the account number
     */
    public String getAccount() {
        return account;
    }

    /**
     * Sets the account number the standing order is linked to.
     *
     * @param account the account number to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * Returns the date of the next scheduled payment.
     *
     * @return the next payment date
     */
    public String getNextDate() {
        return nextDate;
    }

    /**
     * Sets the date of the next scheduled payment.
     *
     * @param nextDate the next payment date to set
     */
    public void setNextDate(String nextDate) {
        this.nextDate = nextDate;
    }

    /**
     * Returns the current status of the standing order.
     *
     * @return the standing order status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the standing order.
     *
     * @param status the standing order status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the amount to be transferred on each execution.
     *
     * @return the payment amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     * Sets the amount to be transferred on each execution.
     *
     * @param amount the payment amount to set
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * Returns the currency code for the standing order.
     *
     * @return the currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency code for the standing order.
     *
     * @param currency the currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
