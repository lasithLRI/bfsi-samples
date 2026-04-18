/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Transaction implementation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private String id;
    private String date;
    private String reference;
    private String amount;
    private String currency;
    private String creditDebitStatus;

    private String account;

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

    public Transaction() {
    }

    /**
     * Executes the getId operation and modify the payload if necessary.
     */
    public String getId() {
        return id;
    }

    /**
     * Executes the setId operation and modify the payload if necessary.
     *
     * @param id              The id parameter
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Executes the getDate operation and modify the payload if necessary.
     */
    public String getDate() {
        return date;
    }

    /**
     * Executes the setDate operation and modify the payload if necessary.
     *
     * @param date            The date parameter
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Executes the getReference operation and modify the payload if necessary.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Executes the setReference operation and modify the payload if necessary.
     *
     * @param reference       The reference parameter
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Executes the getAmount operation and modify the payload if necessary.
     */
    public String getAmount() {
        return amount;
    }

    /**
     * Executes the setAmount operation and modify the payload if necessary.
     *
     * @param amount          The amount parameter
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * Executes the getCurrency operation and modify the payload if necessary.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Executes the setCurrency operation and modify the payload if necessary.
     *
     * @param currency        The currency parameter
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Executes the getCreditDebitStatus operation and modify the payload if necessary.
     */
    public String getCreditDebitStatus() {
        return creditDebitStatus;
    }

    /**
     * Executes the setCreditDebitStatus operation and modify the payload if necessary.
     *
     * @param creditDebitStatus The creditDebitStatus parameter
     */
    public void setCreditDebitStatus(String creditDebitStatus) {
        this.creditDebitStatus = creditDebitStatus;
    }


    /**
     * Executes the getAccount operation and modify the payload if necessary.
     */
    public String getAccount() {
        return account;
    }

    /**
     * Executes the setAccount operation and modify the payload if necessary.
     *
     * @param account         The account parameter
     */
    public void setAccount(String account) {
        this.account = account;
    }
}
