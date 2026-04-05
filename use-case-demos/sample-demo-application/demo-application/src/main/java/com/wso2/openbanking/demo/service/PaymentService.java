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

package com.wso2.openbanking.demo.service;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.PaymentException;
import com.wso2.openbanking.demo.models.Account;
import com.wso2.openbanking.demo.models.Payment;
import com.wso2.openbanking.demo.models.Transaction;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/** PaymentService implementation. */
public final class PaymentService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Random RANDOM = new Random();
    private final BankInfoService bankInfoService;
    private final OAuthTokenService oauthService;
    private final HttpTlsClient client;
    private Payment currentPayment;
    private String currentConsentId;
    private Transaction lastTransaction;
    private String lastPaymentBankName;


    private PaymentService(BankInfoService bankInfoService, HttpTlsClient client, OAuthTokenService oauthService) {
        this.bankInfoService = bankInfoService;
        this.client = client;
        this.oauthService = oauthService;
    }

    /**
     * Static factory — handles GeneralSecurityException/IOException before construction.
     * Use this instead of new PaymentService(...).
     */
    public static PaymentService create(BankInfoService bankInfoService,
                                        HttpTlsClient client)
            throws GeneralSecurityException, IOException {
        OAuthTokenService oauthService = new OAuthTokenService(client);
        return new PaymentService(bankInfoService, client, oauthService);
    }

    /**
     * Executes the processPaymentRequest operation and modify the payload if necessary.
     *
     * @param payment         The payment parameter
     * @throws AuthorizationException When an error occurs during the operation
     */
    public String processPaymentRequest(Payment payment) throws AuthorizationException {
        this.currentPayment = new Payment(payment);
        try {
            String token = oauthService.getToken("payments openid");
            String paymentUrl = ConfigLoader.getPaymentBaseUrl() + "/payment-consents";
            String consentBody = createPaymentConsentBody(payment);
            String consentResponse = oauthService.initializePaymentConsent(token, consentBody, paymentUrl);

            this.currentConsentId = new JSONObject(consentResponse).getJSONObject("Data").getString("ConsentId");

            return oauthService.authorizeConsent(consentResponse, "payments openid");
        } catch (IOException e) {
            throw new AuthorizationException("Failed to contact payment consent endpoint", e);
        } catch (GeneralSecurityException e) {
            throw new AuthorizationException("Failed to sign payment consent request", e);
        }
    }

    /**
     * Executes the addPaymentToAccount operation and modify the payload if necessary.
     *
     * @param accessToken     The accessToken parameter
     * @throws PaymentException When an error occurs during the operation
     */
    public boolean processPaymentAuthorization(String accessToken) throws PaymentException {
        try {
            if (currentPayment == null || currentConsentId == null) {
                return false;
            }
            String paymentUrl = ConfigLoader.getPaymentBaseUrl() + "/payments";
            String paymentBody = createPaymentSubmissionBody(currentPayment, currentConsentId);
            client.postPayments(paymentUrl, paymentBody, accessToken);
            return true;
        } catch (IOException e) {
            throw new PaymentException("Failed to submit payment to bank endpoint", e);
        } finally {
            currentPayment = null;
            currentConsentId = null;
        }
    }

    /**
     * Executes the createPaymentTransaction operation and modify the payload if necessary.
     *
     * @param payment         The payment parameter
     * @param bankName        The bankName parameter
     * @param accountNumber   The accountNumber parameter
     */
    private Transaction createPaymentTransaction(Payment payment, String bankName, String accountNumber) {
        Transaction transaction = new Transaction();
        transaction.setId(generateTransactionId());
        transaction.setDate(LocalDate.now().format(DATE_FORMATTER));
        transaction.setReference(payment.getReference());
        transaction.setAmount(payment.getAmount());
        transaction.setCurrency(payment.getCurrency());
        transaction.setCreditDebitStatus("c");

        transaction.setAccount(accountNumber);
        return transaction;
    }

    /**
     * Executes the updateAccountBalance operation and modify the payload if necessary.
     *
     * @param bankName        The bankName parameter
     * @param accountNumber   The accountNumber parameter
     * @param amount          The amount parameter
     * @param transaction     The transaction parameter
     * @throws PaymentException When an error occurs during the operation
     */
    private void updateAccountBalance(String bankName, String accountNumber,
                                      double amount, Transaction transaction) throws PaymentException {
        Optional<Account> accountOpt = bankInfoService.findAccount(bankName, accountNumber);
        if (!accountOpt.isPresent()) {
            throw new PaymentException("Account not found - Bank: " + bankName + ", Account: " + accountNumber);
        }
        Account account = accountOpt.get();
        double currentBalance = account.getBalance();
        if (currentBalance < amount) {
            throw new PaymentException(
                    "Insufficient balance. Required: " + amount + ", Available: " + currentBalance);
        }
        account.setBalance(currentBalance - amount);
        bankInfoService.addTransactionToAccount(account, transaction);
    }

    /**
     * Executes the createPaymentConsentBody operation and modify the payload if necessary.
     *
     * @param payment         The payment parameter
     */
    private String createPaymentConsentBody(Payment payment) {
        String[] userAccount = parseAccountIdentifier(payment.getUserAccount());
        String[] payeeAccount = parseAccountIdentifier(payment.getPayeeAccount());
        JSONObject initiation = buildInitiation(
                userAccount, payeeAccount,
                payment.getAmount(), payment.getCurrency(), payment.getReference()
        );
        return new JSONObject()
                .put("Data", new JSONObject().put("Initiation", initiation))
                .put("Risk", new JSONObject())
                .toString(4);
    }

    /**
     * Executes the createPaymentSubmissionBody operation and modify the payload if necessary.
     *
     * @param payment         The payment parameter
     * @param consentId       The consentId parameter
     */
    private String createPaymentSubmissionBody(Payment payment, String consentId) {
        String[] userAccount = parseAccountIdentifier(payment.getUserAccount());
        String[] payeeAccount = parseAccountIdentifier(payment.getPayeeAccount());
        JSONObject initiation = buildInitiation(
                userAccount, payeeAccount,
                payment.getAmount(), payment.getCurrency(), payment.getReference()
        );
        return new JSONObject()
                .put("Data", new JSONObject()
                        .put("ConsentId", consentId)
                        .put("Initiation", initiation))
                .put("Risk", new JSONObject())
                .toString(4);
    }

    /**
     * Executes the buildInitiation operation and modify the payload if necessary.
     *
     * @param userAccount     The userAccount parameter
     * @param payeeAccount    The payeeAccount parameter
     * @param amount          The amount parameter
     * @param currency        The currency parameter
     * @param reference       The reference parameter
     */
    private JSONObject buildInitiation(String[] userAccount, String[] payeeAccount,
                                       String amount, String currency, String reference) {
        JSONObject initiation = new JSONObject();
        initiation.put("InstructionIdentification", generateInstructionId());
        initiation.put("EndToEndIdentification", generateEndToEndId());
        initiation.put("LocalInstrument", "OB.Paym");
        initiation.put("InstructedAmount", buildAmount(amount, currency));
        initiation.put("CreditorAccount", buildCreditorAccount(payeeAccount));
        initiation.put("DebtorAccount", buildDebtorAccount(userAccount));
        if (reference != null && !reference.trim().isEmpty()) {
            initiation.put("RemittanceInformation", new JSONObject().put("Reference", reference));
        }
        initiation.put("SupplementaryData", new JSONObject().put("additionalProp1", new JSONObject()));
        return initiation;
    }

    /**
     * Executes the buildAmount operation and modify the payload if necessary.
     *
     * @param amount          The amount parameter
     * @param currency        The currency parameter
     */
    private JSONObject buildAmount(String amount, String currency) {
        return new JSONObject()
                .put("Amount", formatAmount(amount))
                .put("Currency", currency);
    }

    /**
     * Executes the buildCreditorAccount operation and modify the payload if necessary.
     *
     * @param payeeAccount    The payeeAccount parameter
     */
    private JSONObject buildCreditorAccount(String[] payeeAccount) {
        return new JSONObject()
                .put("SchemeName", "OB.SortCodeAccountNumber")
                .put("Identification", generateNumericId(14))
                .put("Name", payeeAccount[0])
                .put("SecondaryIdentification", "0002");
    }

    /**
     * Executes the buildDebtorAccount operation and modify the payload if necessary.
     *
     * @param userAccount     The userAccount parameter
     */
    private JSONObject buildDebtorAccount(String[] userAccount) {
        return new JSONObject()
                .put("SchemeName", "OB.SortCodeAccountNumber")
                .put("Identification", userAccount[1])
                .put("Name", userAccount[0])
                .put("SecondaryIdentification", userAccount[1] + "001");
    }

    /**
     * Executes the generateTransactionId operation and modify the payload if necessary.
     */
    private String generateTransactionId() {
        return String.format("T%08d", RANDOM.nextInt(100_000_000));
    }

    /**
     * Executes the parseAccountIdentifier operation and modify the payload if necessary.
     *
     * @param accountIdentifier The accountIdentifier parameter
     */
    private String[] parseAccountIdentifier(String accountIdentifier) {
        String[] parts = accountIdentifier.split("-", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    /**
     * Executes the formatAmount operation and modify the payload if necessary.
     *
     * @param amount          The amount parameter
     */
    private String formatAmount(String amount) {
        try {
            return String.format("%.2f", Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            return amount;
        }
    }

    /**
     * Executes the generateInstructionId operation and modify the payload if necessary.
     */
    private String generateInstructionId() {
        return "INST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /**
     * Executes the generateEndToEndId operation and modify the payload if necessary.
     */
    private String generateEndToEndId() {

        return "E2E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /**
     * Executes the hexCharToDigit operation and modify the payload if necessary.
     *
     * @param c               The c parameter
     */
    private int hexCharToDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        int letterValue = (c >= 'a') ? (c - 'a') : (c - 'A');
        return letterValue % 10;
    }

    /**
     * Executes the generateNumericId operation and modify the payload if necessary.
     *
     * @param length          The length parameter
     */
    private String generateNumericId(int length) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        StringBuilder numericId = new StringBuilder();
        for (char c : uuid.toCharArray()) {
            if (numericId.length() >= length) {
                break;
            }
            numericId.append(hexCharToDigit(c));
        }
        while (numericId.length() < length) {
            numericId.append(RANDOM.nextInt(10));
        }
        return numericId.substring(0, length);
    }

    public Transaction getLastTransaction() {
        return lastTransaction;
    }

    public String getLastPaymentBankName() {
        return lastPaymentBankName;
    }
}
