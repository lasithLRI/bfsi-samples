/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

/** Payment implementation. */
public class Payment {
    private String userAccount;
    private String payeeAccount;
    private String currency;
    private String amount;
    private String reference;

    public Payment(Payment other) {
        this.userAccount = other.userAccount;
        this.payeeAccount = other.payeeAccount;
        this.amount = other.amount;
        this.currency = other.currency;
        this.reference = other.reference;
    }

    public Payment() {
    }

    /**
     * Executes the getUserAccount operation and modify the payload if necessary.
     */
    public String getUserAccount() {
        return userAccount;
    }

    /**
     * Executes the setUserAccount operation and modify the payload if necessary.
     *
     * @param userAccount     The userAccount parameter
     */
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    /**
     * Executes the getPayeeAccount operation and modify the payload if necessary.
     */
    public String getPayeeAccount() {
        return payeeAccount;
    }

    /**
     * Executes the setPayeeAccount operation and modify the payload if necessary.
     *
     * @param payeeAccount    The payeeAccount parameter
     */
    public void setPayeeAccount(String payeeAccount) {
        this.payeeAccount = payeeAccount;
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
     * Executes the toString operation and modify the payload if necessary.
     */
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
