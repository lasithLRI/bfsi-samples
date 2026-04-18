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

/** StandingOrder implementation. */
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

    public StandingOrder() {
    }

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
     * Executes the getBank operation and modify the payload if necessary.
     */
    public String getBank() {
        return bank;
    }

    /**
     * Executes the setBank operation and modify the payload if necessary.
     *
     * @param bank            The bank parameter
     */
    public void setBank(String bank) {
        this.bank = bank;
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

    /**
     * Executes the getNextDate operation and modify the payload if necessary.
     */
    public String getNextDate() {
        return nextDate;
    }

    /**
     * Executes the setNextDate operation and modify the payload if necessary.
     *
     * @param nextDate        The nextDate parameter
     */
    public void setNextDate(String nextDate) {
        this.nextDate = nextDate;
    }

    /**
     * Executes the getStatus operation and modify the payload if necessary.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Executes the setStatus operation and modify the payload if necessary.
     *
     * @param status          The status parameter
     */
    public void setStatus(String status) {
        this.status = status;
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
}
