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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Bank implementation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bank {

    private String name;
    private String image;
    private String color;
    private String border;
    private String currency;
    private List<Account> accounts;
    private Boolean flag;

    @JsonIgnore
    private final Map<String, String> accountToConsentMap = new HashMap<>();

    public Bank() {
    }

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
            justification = "addAccount is called intentionally in constructor; Bank is not designed for subclassing")
    public Bank(String name, String image, String color, String border, List<Account> accounts) {
        this.name = name;
        this.image = image;
        this.color = color;
        this.border = border;
        this.flag = name != null && name.equalsIgnoreCase(ConfigLoader.getMockBankName());
        if (accounts != null) {
            this.accounts = new ArrayList<>();
            accounts.forEach(this::addAccount);
        }
    }

    /**
     * Executes the addAccount operation and modify the payload if necessary.
     *
     * @param account         The account parameter
     */
    public void addAccount(Account account) {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
        if (account.getId() != null && account.getConsentId() != null) {
            accountToConsentMap.put(account.getId(), account.getConsentId());
        }
    }

    /**
     * Executes the getConsentIdForAccount operation and modify the payload if necessary.
     *
     * @param accountId       The accountId parameter
     */
    public String getConsentIdForAccount(String accountId) {
        return accountToConsentMap.get(accountId);
    }

    /**
     * Executes the getAccountIdsByConsentId operation and modify the payload if necessary.
     *
     * @param consentId       The consentId parameter
     */
    public List<String> getAccountIdsByConsentId(String consentId) {
        return accountToConsentMap.entrySet().stream()
                .filter(entry -> consentId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Executes the removeConsent operation and modify the payload if necessary.
     *
     * @param consentId       The consentId parameter
     */
    public void removeConsent(String consentId) {
        accountToConsentMap.entrySet().removeIf(entry -> consentId.equals(entry.getValue()));
    }

    /**
     * Executes the removeAccount operation and modify the payload if necessary.
     *
     * @param accountId       The accountId parameter
     */
    public boolean removeAccount(String accountId) {
        if (accounts == null) {
            return false;
        }
        return accounts.removeIf(a -> a.getId().equals(accountId));
    }

    /**
     * Executes the setAccounts operation and modify the payload if necessary.
     *
     * @param accounts        The accounts parameter
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = null;
        accountToConsentMap.clear();
        if (accounts != null) {
            accounts.forEach(this::addAccount);
        }
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
        this.flag = name != null && name.equalsIgnoreCase(ConfigLoader.getMockBankName());
    }

    /**
     * Executes the getImage operation and modify the payload if necessary.
     */
    public String getImage() {
        return image;
    }
    /**
     * Executes the getColor operation and modify the payload if necessary.
     */
    public String getColor() {
        return color;
    }
    /**
     * Executes the getBorder operation and modify the payload if necessary.
     */
    public String getBorder() {
        return border;
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
    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode();
    }

    /**
     * Executes the getAccounts operation and modify the payload if necessary.
     */
    public List<Account> getAccounts() {
        return accounts == null ? null : new ArrayList<>(accounts);
    }

    /**
     * Executes the getFlag operation and modify the payload if necessary.
     */
    public boolean getFlag() {
        return this.flag;
    }
}
