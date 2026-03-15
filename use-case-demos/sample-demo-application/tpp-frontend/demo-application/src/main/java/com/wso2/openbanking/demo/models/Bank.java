package com.wso2.openbanking.demo.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wso2.openbanking.demo.utils.ConfigLoader;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Bank {

    private String name;
    private String image;
    private String color;
    private String border;
    private String currency;
    private List<Account> accounts;
    private Boolean flag;

    /**
     * Maps a ConsentId to a list of AccountIds for every active AISP consent issued against this bank.
     * A single consent may grant access to multiple accounts, so each key maps to a list.
     * Scoped to the bank that issued the consent and excluded from JSON serialization.
     */
    @JsonIgnore
    private final Map<String, List<String>> consentToAccountMap = new HashMap<>();

    /**
     * Constructs an empty Bank instance.
     */
    public Bank() {
    }

    /**
     * Constructs a Bank with the specified display properties and accounts.
     *
     * @param name     the display name of the bank
     * @param image    the image URL or path representing the bank's logo
     * @param color    the primary color associated with the bank's branding
     * @param border   the border style associated with the bank's branding
     * @param accounts the list of accounts held at this bank
     */
    public Bank(String name, String image, String color, String border, List<Account> accounts) {
        this.name = name;
        this.image = image;
        this.color = color;
        this.border = border;
        this.accounts = accounts == null ? null : new ArrayList<>(accounts);
        this.flag = name != null && name.equalsIgnoreCase(ConfigLoader.getMockBankName()); // ← add this

    }

    /**
     * Registers a ConsentId to AccountId mapping for this bank.
     * If the ConsentId is already present, the AccountId is appended to its list.
     * Called after the AISP consent flow completes and the account is stored.
     *
     * @param consentId the ConsentId returned by the bank during consent initiation
     * @param accountId the AccountId the consent grants access to
     */
    public void registerConsent(String consentId, String accountId) {
        consentToAccountMap.computeIfAbsent(consentId, k -> new ArrayList<>()).add(accountId);
    }

    /**
     * Forward-lookup: returns the list of AccountIds associated with the given ConsentId.
     *
     * @param consentId the ConsentId to look up
     * @return an Optional containing the list of AccountIds, or empty if not found
     */
    public Optional<List<String>> getAccountIdsForConsent(String consentId) {
        return Optional.ofNullable(consentToAccountMap.get(consentId));
    }

    /**
     * Reverse-lookup: finds the ConsentId registered for a given AccountId.
     * Used when the frontend supplies an AccountId and the backend needs to
     * resolve which consent to revoke.
     *
     * @param accountId the account to look up
     * @return the ConsentId, or null if no mapping exists for this account
     */
    public String findConsentIdByAccountId(String accountId) {
        return consentToAccountMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(accountId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes the ConsentId mapping and returns all AccountIds that were registered under it.
     * Called after the bank confirms consent revocation so the mapping does not
     * linger in memory after the accounts are removed.
     *
     * @param consentId the ConsentId to remove
     * @return the list of AccountIds that were mapped, or null if no mapping existed
     */
    public List<String> removeConsent(String consentId) {
        return consentToAccountMap.remove(consentId);
    }

    /**
     * Returns the display name of the bank.
     *
     * @return the bank name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the bank.
     *
     * @param name the bank name to set
     */
//    public void setName(String name) {
//        this.name = name;
//    }

    /**
     * Returns the image URL or path representing the bank's logo.
     *
     * @return the bank image
     */
    public String getImage() {
        return image;
    }

    /**
     * Returns the primary color associated with the bank's branding.
     *
     * @return the bank color
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the border style associated with the bank's branding.
     *
     * @return the bank border style
     */
    public String getBorder() {
        return border;
    }

    /**
     * Returns the currency used by this bank.
     *
     * @return the bank currency
     */
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode();
    }

    /**
     * Returns the list of accounts held at this bank.
     *
     * @return the list of accounts
     */
    public List<Account> getAccounts() {
        return accounts == null ? null : new ArrayList<>(accounts);
    }

    /**
     * Sets the list of accounts held at this bank.
     *
     * @param accounts the list of accounts to set
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts == null ? null : new ArrayList<>(accounts);
    }

    public boolean removeAccount(String accountId) {
        if (accounts == null) {
            return false;
        }
        return accounts.removeIf(a -> a.getId().equals(accountId));
    }

    public void addAccount(Account account) {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
    }

    public boolean getFlag(){
        return this.flag;
    }

    public void setName(String name) {
        this.name = name;
        this.flag = name != null && name.equalsIgnoreCase(ConfigLoader.getMockBankName()); // ← set flag here
    }
}
