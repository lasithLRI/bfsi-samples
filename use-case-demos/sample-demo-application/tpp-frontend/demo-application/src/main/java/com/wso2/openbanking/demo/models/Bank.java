package com.wso2.openbanking.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wso2.openbanking.demo.utils.ConfigLoader;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void addAccount(Account account) {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
        if (account.getId() != null && account.getConsentId() != null) {
            accountToConsentMap.put(account.getId(), account.getConsentId());
        }
    }

    public String getConsentIdForAccount(String accountId) {
        return accountToConsentMap.get(accountId);
    }

    public List<String> getAccountIdsByConsentId(String consentId) {
        return accountToConsentMap.entrySet().stream()
                .filter(entry -> consentId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void removeConsent(String consentId) {
        accountToConsentMap.entrySet().removeIf(entry -> consentId.equals(entry.getValue()));
    }

    public boolean removeAccount(String accountId) {
        if (accounts == null) return false;
        return accounts.removeIf(a -> a.getId().equals(accountId));
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = null;
        accountToConsentMap.clear();
        if (accounts != null) {
            accounts.forEach(this::addAccount);
        }
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
        this.flag = name != null && name.equalsIgnoreCase(ConfigLoader.getMockBankName());
    }

    public String getImage() { return image; }
    public String getColor() { return color; }
    public String getBorder() { return border; }
    public String getCurrency() { return currency; }

    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode();
    }

    public List<Account> getAccounts() {
        return accounts == null ? null : new ArrayList<>(accounts);
    }

    public boolean getFlag() { return this.flag; }
}