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
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/** PaymentService implementation. */
public final class PaymentService {

    private static final Random RANDOM = new Random();
    private final OAuthTokenService oauthService;
    private final HttpTlsClient client;
    private Payment currentPayment;
    private String currentConsentId;

    private PaymentService( HttpTlsClient client, OAuthTokenService oauthService) {
        this.client = client;
        this.oauthService = oauthService;
    }

    public static PaymentService create(
                                        HttpTlsClient client)
            throws GeneralSecurityException, IOException, SSLContextCreationException {
        OAuthTokenService oauthService = new OAuthTokenService(client);
        return new PaymentService(client, oauthService);
    }

    /**
     * Initiates the payment consent flow and returns an authorization URL.
     *
     * @param payment the payment object containing the payment details
     * @return the OAuth authorization URL for the user to grant payment consent
     * @throws AuthorizationException if token retrieval, consent initialization, or request signing fails
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
     * Submits the authorized payment to the bank endpoint using the provided access token.
     *
     * @param accessToken the OAuth access token obtained after user authorization
     * @return {@code true} if the payment was submitted successfully, {@code false} if no pending payment or consent exists
     * @throws PaymentException if the payment submission to the bank endpoint fails
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
     * Creates the JSON request body for a payment consent from the given payment details.
     *
     * @param payment the payment object containing payer, payee, amount, currency, and reference details
     * @return a JSON string representing the payment consent request body
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
     * Builds the payment initiation JSON object from the provided account and payment details.
     *
     * @param userAccount  the parsed account identifier array for the debtor (payer)
     * @param payeeAccount the parsed account identifier array for the creditor (payee)
     * @param amount       the payment amount as a string
     * @param currency     the currency code for the payment
     * @param reference    the optional remittance reference for the payment
     * @return a {@link JSONObject} representing the payment initiation block
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

    private JSONObject buildAmount(String amount, String currency) {
        return new JSONObject()
                .put("Amount", formatAmount(amount))
                .put("Currency", currency);
    }

    private JSONObject buildCreditorAccount(String[] payeeAccount) {
        return new JSONObject()
                .put("SchemeName", "OB.SortCodeAccountNumber")
                .put("Identification", generateNumericId(14))
                .put("Name", payeeAccount[0])
                .put("SecondaryIdentification", "0002");
    }

    private JSONObject buildDebtorAccount(String[] userAccount) {
        return new JSONObject()
                .put("SchemeName", "OB.SortCodeAccountNumber")
                .put("Identification", userAccount[1])
                .put("Name", userAccount[0])
                .put("SecondaryIdentification", userAccount[1] + "001");
    }

    private String[] parseAccountIdentifier(String accountIdentifier) {
        String[] parts = accountIdentifier.split("-", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    private String formatAmount(String amount) {
        try {
            return String.format("%.2f", Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            return amount;
        }
    }

    private String generateInstructionId() {
        return "INST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String generateEndToEndId() {

        return "E2E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /**
     * Converts a hexadecimal character to a single decimal digit.
     *
     * @param c the hexadecimal character to convert
     * @return a decimal digit in the range 0–9
     */
    private int hexCharToDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        int letterValue = (c >= 'a') ? (c - 'a') : (c - 'A');
        return letterValue % 10;
    }

    /**
     * Generates a random numeric string of the specified length derived from a UUID.
     *
     * @param length the desired length of the numeric ID
     * @return a random numeric string of exactly {@code length} digits
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
}
