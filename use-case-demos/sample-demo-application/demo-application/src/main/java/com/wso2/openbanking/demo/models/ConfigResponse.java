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

import java.util.ArrayList;
import java.util.List;

/** ConfigResponse implementation. */
public class ConfigResponse {
    private final List<Bank> banks;
    private final List<Payee> payees;
    private final List<Transaction> transactions;
    private final List<StandingOrder> standingOrders;

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
     * Executes the getBanks operation and modify the payload if necessary.
     */
    public List<Bank> getBanks() {
        return banks == null ? null : new ArrayList<>(banks);
    }

    /**
     * Executes the getPayees operation and modify the payload if necessary.
     */
    public List<Payee> getPayees() {
        return payees == null ? null : new ArrayList<>(payees);
    }

    /**
     * Executes the getTransactions operation and modify the payload if necessary.
     */
    public List<Transaction> getTransactions() {
        return transactions == null ? null : new ArrayList<>(transactions);
    }

    /**
     * Executes the getStandingOrders operation and modify the payload if necessary.
     */
    public List<StandingOrder> getStandingOrders() {
        return standingOrders == null ? null : new ArrayList<>(standingOrders);
    }
}
