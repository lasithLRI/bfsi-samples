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

/** BankInfoInPayments implementation. */
public class BankInfoInPayments {
    String name;
    String accountNumber;

    public BankInfoInPayments(String name, String accountNumber) {
        this.name = name;
        this.accountNumber = accountNumber;
    }

    /**
     * Executes the getName operation and modify the payload if necessary.
     */
    public String getName() {
        return name;
    }

    /**
     * Executes the setName operation and modify the payload if necessary.
     *
     * @param name            The name parameter
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Executes the getAccountNumber operation and modify the payload if necessary.
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Executes the setAccountNumber operation and modify the payload if necessary.
     *
     * @param accountNumber   The accountNumber parameter
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
