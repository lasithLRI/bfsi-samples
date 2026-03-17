package com.wso2.openbanking.demo.services;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.models.Account;
import com.wso2.openbanking.demo.models.Bank;
import com.wso2.openbanking.demo.models.Transaction;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles fetching and storing account and transaction data from the mock Open Banking API.
 * Uses mTLS via HttpTlsClient and OAuth tokens via OAuthTokenService to authenticate requests.
 *
 * When accounts are successfully added, each account's ConsentId is registered in
 * BankInfoService so it can be looked up for later revocation via the delete endpoint.
 */
public final class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final BankInfoService bankInfoService;
    private final HttpTlsClient client;
    private final OAuthTokenService oauthService;
    private String accessToken;

    /**
     * Holds the ConsentId returned during the account flow consent initiation phase so it can
     * be registered against accounts once they are fetched and stored.
     */
    private String currentConsentId;

    private static final DateTimeFormatter ISO_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String FIELD_ACCOUNT = "Account";
    private static final String FIELD_AMOUNT = "Amount";
    private static final String ACCOUNTS_PATH = "/accounts/";

    /**
     * Constructs an AccountService with the given bank info store and HTTP client.
     * An OAuthTokenService is created internally using the provided client.
     *
     * @param bankInfoService the service used to read and update in-memory bank/account data
     * @param client          the mTLS-enabled HTTP client used for API requests
     * @throws Exception if the OAuthTokenService cannot be initialised
     */
    @SuppressWarnings("EI_EXPOSE_REP2")
// bankInfoService and client are shared service dependencies — defensive copying is not appropriate.
    public AccountService(BankInfoService bankInfoService, HttpTlsClient client) throws Exception {
        this.bankInfoService = bankInfoService;
        this.client = client;
        this.oauthService = new OAuthTokenService(client);
    }


    /**
     * Sets the OAuth access token used to authenticate API requests.
     *
     * @param accessToken the Bearer token string.
     */
    public void setAccessToken(String accessToken) {

        this.accessToken = accessToken;
    }

//    /**
//     * Fetches accounts from the mock bank API and adds any new ones to the in-memory store.
//     * Accounts already present are skipped to avoid duplication.
//     * After accounts are stored, each new account is registered against the currentConsentId
//     * in BankInfoService so the consent can be revoked later.
//     *
//     * @throws IOException           if any HTTP request to the bank API fails.
//     * @throws BankInfoLoadException if the bank data cannot be loaded or accessed.
//     */
//    public void addMockBankAccountsInformation() throws IOException, BankInfoLoadException {
//        if (bankInfoService.getBanks() == null) {
//            bankInfoService.loadBanks();
//        }
//        String bankName = ConfigLoader.getMockBankName();
//        boolean bankExists = bankInfoService.isBankExists(bankName);
//        List<String> fetchedAccountIds = fetchAccountIds();
//        Set<String> existingAccountIds = getExistingAccountIds(bankName);
//        List<String> newAccountIds = fetchedAccountIds.stream()
//                .filter(id -> !existingAccountIds.contains(id))
//                .collect(Collectors.toList());
//
//        if (newAccountIds.isEmpty()) {
//            logger.info(
//                    "\n========== [AISP] ACCOUNTS — NO NEW ACCOUNTS ==========\n" +
//                            "  All fetched accounts are already stored. Nothing added.\n" +
//                            "========================================================"
//            );
//            return;
//        }
//
//        List<Account> newAccounts = fetchAccountsWithTransactions(newAccountIds, bankName);
//
//        if (bankExists) {
//            addAccountsToExistingBank(bankName, newAccounts);
//        } else {
//            addNewBank(bankName, newAccounts);
//        }
//
//        if (currentConsentId != null) {
//            for (Account account : newAccounts) {
//                bankInfoService.registerConsentForAccount(currentConsentId, account.getId());
//            }
//        } else {
//            logger.warn(
//                    "\n========== [AISP] ACCOUNTS — CONSENT ID MISSING ==========\n" +
//                            "  Accounts were stored but no ConsentId is available to register.\n" +
//                            "  Consent revocation will not be possible for these accounts.\n" +
//                            "==========================================================="
//            );
//        }
//    }

    /**
     * Initiates the account consent flow for the mock bank.
     * Stores the returned ConsentId for use after the user approves the consent.
     * Returns null if the provided bank name does not match the configured mock bank name.
     *
     * @param bankName the name of the bank for which consent is being requested.
     * @return the authorization URL the user must visit to grant consent,
     *         or null if the bank is not the configured mock bank.
     * @throws Exception if the token request, consent initiation, or authorization URL generation fails.
     */
    public String processAddAccount(String bankName) throws Exception {
        if (!bankName.equals(ConfigLoader.getMockBankName())) {
            return null;
        }
        String addAccountUrl = ConfigLoader.getAccountBaseUrl() + "/account-access-consents";
        String consentBody = createAccountConsentBody();
        String token = oauthService.getToken("accounts openid");
        String consentResponse = oauthService.initializeConsent(token, consentBody, addAccountUrl);
        this.currentConsentId = parseConsentId(consentResponse);
        return oauthService.authorizeConsent(consentResponse, "accounts openid");
    }

//    /**
//     * Revokes the account access consent with the bank and, on success, removes
//     * the associated account from the in-memory store.
//     * A fresh client-credentials token is obtained internally using the accounts openid scope.
//     *
//     * @param consentId the ConsentId to revoke.
//     * @return true if the bank confirmed revocation and the account was removed,
//     *         false if the bank rejected the revocation request.
//     * @throws AuthorizationException if the token request fails due to an authorization error.
//     * @throws IOException            if the HTTP DELETE request to the bank fails.
//     */
//    public boolean revokeAccountConsent(String consentId) throws AuthorizationException, IOException {
//        String tokenJson = oauthService.getToken("accounts openid");
//        String accessToken = new org.json.JSONObject(tokenJson).getString("access_token");
//        String revokeUrl = ConfigLoader.getAccountBaseUrl()
//                + "/account-access-consents/" + consentId;
//
//        logger.info(
//                "\n========== [AISP] CONSENT REVOCATION REQUEST ==========\n" +
//                        "  ConsentId : {}\n" +
//                        "  URL       : {}\n" +
//                        "=======================================================",
//                consentId, revokeUrl
//        );
//
//        boolean bankConfirmed = client.deleteWithAuth(revokeUrl, accessToken);
//
//        if (bankConfirmed) {
//            logger.info(
//                    "\n========== [AISP] CONSENT REVOCATION — BANK CONFIRMED ==========\n" +
//                            "  ConsentId : {}\n" +
//                            "  Status    : Bank returned 2xx — removing account from application\n" +
//                            "================================================================",
//                    consentId
//            );
//            bankInfoService.revokeConsentAndRemoveAccount(consentId);
//        } else {
//            logger.warn(
//                    "\n========== [AISP] CONSENT REVOCATION — BANK REJECTED ==========\n" +
//                            "  ConsentId : {}\n" +
//                            "  Status    : Bank did not return 2xx — account NOT removed\n" +
//                            "===============================================================",
//                    consentId
//            );
//        }
//
//        return bankConfirmed;
//    }

    /**
     * Parses the ConsentId from the consent initiation response JSON.
     *
     * @param consentResponse the raw JSON string returned by the consent initiation endpoint.
     * @return the ConsentId string, or null if parsing fails.
     */
    private String parseConsentId(String consentResponse) {
        try {
            return new JSONObject(consentResponse)
                    .getJSONObject("Data")
                    .optString("ConsentId", null);
        } catch (Exception e) {
            logger.warn("Failed to parse ConsentId from consent response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Returns the set of account IDs already stored for the given bank.
     *
     * @param bankName the name of the bank to look up.
     * @return a set of existing account ID strings, or an empty set if none are found.
     */
    private Set<String> getExistingAccountIds(String bankName) {
        Set<String> existingIds = new HashSet<>();
        if (bankInfoService.getBanks() == null) {
            return existingIds;
        }
        bankInfoService.getBanks().stream()
                .filter(bank -> bank.getName().equals(bankName))
                .flatMap(bank -> bank.getAccounts().stream())
                .forEach(account -> existingIds.add(account.getId()));
        return existingIds;
    }

    /**
     * Merges new accounts into an existing bank entry, skipping any already present.
     * If the bank currently has no account list, one is initialised before merging.
     *
     * @param bankName    the name of the bank to update.
     * @param newAccounts the list of new Account objects to add.
     */
    private void addAccountsToExistingBank(String bankName, List<Account> newAccounts) {
        bankInfoService.getBanks().stream()
                .filter(bank -> bank.getName().equals(bankName))
                .findFirst()
                .ifPresent(bank -> {
                    Set<String> currentIds = bank.getAccounts() == null
                            ? new HashSet<>()
                            : bank.getAccounts().stream()
                            .map(Account::getId)
                            .collect(Collectors.toSet());

                    newAccounts.stream()
                            .filter(account -> !currentIds.contains(account.getId()))
                            .forEach(bank::addAccount);
                });
    }

    /**
     * Calls the accounts endpoint and returns all account IDs accessible under
     * the current OAuth access token.
     *
     * @return a list of account ID strings.
     * @throws IOException if the HTTP request fails or the response cannot be parsed.
     */
    private List<String> fetchAccountIds() throws IOException {
        String response = client.getWithAuth(
                ConfigLoader.getAccountBaseUrl() + "/accounts",
                this.accessToken
        );
        JSONArray accountsArray = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray(FIELD_ACCOUNT);
        List<String> accountIds = new ArrayList<>();
        for (int i = 0; i < accountsArray.length(); i++) {
            accountIds.add(accountsArray.getJSONObject(i).getString("AccountId"));
        }
        return accountIds;
    }

    /**
     * Fetches full account details and transaction history for each of the given account IDs.
     *
     * @param accountIds the list of account IDs to fetch.
     * @param bankName   the bank name to associate with each account and its transactions.
     * @return a list of fully populated Account objects.
     * @throws IOException if any HTTP request fails or a response cannot be parsed.
     */
    private List<Account> fetchAccountsWithTransactions(List<String> accountIds, String bankName)
            throws IOException {
        List<Account> accounts = new ArrayList<>();
        for (String accountId : accountIds) {
            String accountName = fetchAccountName(accountId);
            double balance = fetchAccountBalance(accountId);
            List<Transaction> transactions = fetchAccountTransactions(accountId, bankName);
            Account account = new Account(accountId, accountName, balance, transactions);
            account.setBank(bankName);
            accounts.add(account);
        }
        return accounts;
    }

    /**
     * Fetches the display name for the given account from the bank API.
     * Returns "Open Banking Account" or "Standard Account" as fallbacks
     * if no name field is present in the response.
     *
     * @param accountId the ID of the account to look up.
     * @return the account display name string.
     * @throws IOException if the HTTP request fails or the response cannot be parsed.
     */
    private String fetchAccountName(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + ACCOUNTS_PATH + accountId;
        String response = client.getWithAuth(url, this.accessToken);
        JSONObject accountDataNode = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray(FIELD_ACCOUNT)
                .getJSONObject(0);
        if (accountDataNode.has(FIELD_ACCOUNT)) {
            return accountDataNode.getJSONArray(FIELD_ACCOUNT)
                    .getJSONObject(0)
                    .optString("Name", "Open Banking Account");
        }
        return accountDataNode.optString("Nickname", "Standard Account");
    }

    /**
     * Fetches the current balance for the given account from the bank API.
     *
     * @param accountId the ID of the account to look up.
     * @return the account balance as a double.
     * @throws IOException if the HTTP request fails or the response cannot be parsed.
     */
    private double fetchAccountBalance(String accountId) throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + ACCOUNTS_PATH + accountId + "/balances";
        String response = client.getWithAuth(url, this.accessToken);
        String amount = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Balance")
                .getJSONObject(0)
                .getJSONObject(FIELD_AMOUNT)
                .getString(FIELD_AMOUNT);
        return Double.parseDouble(amount);
    }

    /**
     * Fetches and parses the full transaction list for the given account from the bank API.
     *
     * @param accountId the ID of the account whose transactions are to be fetched.
     * @param bankName  the bank name to associate with each parsed transaction.
     * @return a list of Transaction objects.
     * @throws IOException if the HTTP request fails or the response cannot be parsed.
     */
    private List<Transaction> fetchAccountTransactions(String accountId, String bankName)
            throws IOException {
        String url = ConfigLoader.getAccountBaseUrl() + ACCOUNTS_PATH + accountId + "/transactions";
        String response = client.getWithAuth(url, this.accessToken);
        JSONArray transactionsArray = new JSONObject(response)
                .getJSONObject("Data")
                .getJSONArray("Transaction");
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsArray.length(); i++) {
            transactions.add(parseTransaction(transactionsArray.getJSONObject(i), bankName, accountId));
        }
        return transactions;
    }

    /**
     * Maps a raw transaction JSON object to a Transaction model,
     * enriched with the parent bank name and account ID.
     *
     * @param txn       the JSON object representing a single transaction.
     * @param bankName  the bank name to set on the transaction.
     * @param accountId the account ID to set on the transaction.
     * @return a populated Transaction instance.
     */
    private Transaction parseTransaction(JSONObject txn, String bankName, String accountId) {
        Transaction transaction = new Transaction();
        transaction.setId(txn.getString("TransactionId"));
        transaction.setDate(convertIsoDateTimeToDate(txn.getString("BookingDateTime")));
        transaction.setReference(txn.getString("TransactionInformation"));
        JSONObject amountObj = txn.getJSONObject(FIELD_AMOUNT);
        transaction.setAmount(amountObj.getString(FIELD_AMOUNT));
        transaction.setCurrency(amountObj.getString("Currency"));
        transaction.setCreditDebitStatus(txn.getString("CreditDebitIndicator"));
        transaction.setBank(bankName);
        transaction.setAccount(accountId);
        return transaction;
    }

    /**
     * Converts an ISO 8601 datetime string to a plain date string in yyyy-MM-dd format.
     * Attempts parsing with ISO_DATETIME_FORMATTER first, then falls back to
     * ISO_LOCAL_DATE_TIME, and finally takes a substring of the first 10 characters
     * if both parsers fail.
     *
     * @param isoDateTime the ISO 8601 datetime string to convert.
     * @return a yyyy-MM-dd date string.
     */
    private String convertIsoDateTimeToDate(String isoDateTime) {
        try {
            return ZonedDateTime.parse(isoDateTime, ISO_DATETIME_FORMATTER).format(DATE_FORMATTER);
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .format(DATE_FORMATTER);
            } catch (Exception e2) {
                if (isoDateTime.length() >= 10) {
                    return isoDateTime.substring(0, 10);
                }
                return isoDateTime;
            }
        }
    }

    /**
     * Creates a new Bank entry in the in-memory store using the mock bank
     * configuration from ConfigLoader and adds the given accounts to it.
     *
     * @param bankName the name of the new bank.
     * @param accounts the list of Account objects to associate with the bank.
     */
    private void addNewBank(String bankName, List<Account> accounts) {
        Bank newBank = new Bank(
                bankName,
                ConfigLoader.getMockBankLogo(),
                ConfigLoader.getMockBankPrimaryColor(),
                ConfigLoader.getMockBankSecondaryColor(),
                accounts
        );
        bankInfoService.addBank(newBank);
    }

    /**
     * Builds the JSON request body for the account access consent endpoint.
     * Requests read permissions for accounts, balances, and transactions,
     * with a 90-day expiry window and a 30-day historical transaction range.
     *
     * @return the consent request body as a JSON string.
     */
    private String createAccountConsentBody() {
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.of("+05:30"));
        JSONObject permissions = new JSONObject()
                .put("Permissions", new JSONArray()
                        .put("ReadAccountsBasic")
                        .put("ReadAccountsDetail")
                        .put("ReadBalances")
                        .put("ReadTransactionsDetail"))
                .put("ExpirationDateTime", now.plusDays(90).format(ISO_DATETIME_FORMATTER))
                .put("TransactionFromDateTime", now.minusDays(30).format(ISO_DATETIME_FORMATTER))
                .put("TransactionToDateTime", now.format(ISO_DATETIME_FORMATTER));
        return new JSONObject()
                .put("Data", permissions)
                .put("Risk", new JSONObject())
                .toString();
    }

    /**
     * Fetches accounts from the mock bank API and adds any new ones to the in-memory store.
     * Accounts already present are skipped to avoid duplication.
     */
    public void addMockBankAccountsInformation() throws IOException, BankInfoLoadException {
        if (bankInfoService.getBanks() == null) {
            bankInfoService.loadBanks();
        }
        String bankName = ConfigLoader.getMockBankName();
        boolean bankExists = bankInfoService.isBankExists(bankName);
        List<String> fetchedAccountIds = fetchAccountIds();
        Set<String> existingAccountIds = getExistingAccountIds(bankName);
        List<String> newAccountIds = fetchedAccountIds.stream()
                .filter(id -> !existingAccountIds.contains(id))
                .collect(Collectors.toList());
        if (newAccountIds.isEmpty()) {
            return;
        }
        List<Account> newAccounts = fetchAccountsWithTransactions(newAccountIds, bankName);
        if (bankExists) {
            addAccountsToExistingBank(bankName, newAccounts);
        } else {
            addNewBank(bankName, newAccounts);
        }
    }
}
