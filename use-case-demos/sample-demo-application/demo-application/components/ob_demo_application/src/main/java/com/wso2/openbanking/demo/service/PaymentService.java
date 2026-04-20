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

package com.wso2.openbanking.demo.service;

import com.wso2.openbanking.demo.constants.OpenBankingConstants;
import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.PaymentException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.models.Payment;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/** Handles payment consent creation, authorization, and payment submission. */
public final class PaymentService {

    private static final Random RANDOM = new Random();
    private final OAuthTokenService oauthService;
    private final HttpTlsClient client;
    private Payment currentPayment;
    private String currentConsentId;

    /**
     * Creates a PaymentService with the given HTTP client and OAuth service.
     *
     * @param client       TLS HTTP client for making API calls
     * @param oauthService service for obtaining OAuth tokens
     */
    private PaymentService(HttpTlsClient client, OAuthTokenService oauthService) {
        this.client = client;
        this.oauthService = oauthService;
    }

    /**
     * Creates a PaymentService instance using the given TLS client.
     *
     * @param client TLS HTTP client for API calls
     * @return new PaymentService instance
     * @throws GeneralSecurityException    if JWT service setup fails
     * @throws IOException                 if the signing key cannot be read
     * @throws SSLContextCreationException if TLS setup fails
     */
    public static PaymentService create(HttpTlsClient client)
            throws GeneralSecurityException, IOException, SSLContextCreationException {
        OAuthTokenService oauthService = new OAuthTokenService(client);
        return new PaymentService(client, oauthService);
    }

    /**
     * Creates a payment consent and returns the OAuth authorization redirect URL.
     *
     * @param payment payment details to create a consent for
     * @return authorization redirect URL for the payment consent
     * @throws AuthorizationException if consent creation or signing fails
     */
    public String processPaymentRequest(Payment payment) throws AuthorizationException {
        this.currentPayment = new Payment(payment);
        try {
            String token = oauthService.getToken(OpenBankingConstants.SCOPE_PAYMENTS);
            String paymentUrl = ConfigLoader.getPaymentBaseUrl() + OpenBankingConstants.PATH_PAYMENT_CONSENTS;
            String consentBody = createPaymentConsentBody(payment);
            String consentResponse = oauthService.initializePaymentConsent(token, consentBody, paymentUrl);
            this.currentConsentId = new JSONObject(consentResponse)
                    .getJSONObject(OpenBankingConstants.FIELD_DATA)
                    .getString(OpenBankingConstants.FIELD_CONSENT_ID);
            return oauthService.authorizeConsent(consentResponse, OpenBankingConstants.SCOPE_PAYMENTS);
        } catch (IOException e) {
            throw new AuthorizationException("Failed to contact payment consent endpoint", e);
        } catch (GeneralSecurityException e) {
            throw new AuthorizationException("Failed to sign payment consent request", e);
        }
    }

    /**
     * Submits the current payment using the given access token.
     *
     * @param accessToken valid OAuth access token from the authorization callback
     * @return true if payment was submitted successfully, false if no pending payment exists
     * @throws PaymentException if the payment submission request fails
     */
    public boolean processPaymentAuthorization(String accessToken) throws PaymentException {
        try {
            if (currentPayment == null || currentConsentId == null) {
                return false;
            }
            String paymentUrl = ConfigLoader.getPaymentBaseUrl() + OpenBankingConstants.PATH_PAYMENTS;
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
     * Builds the JSON request body for a payment consent.
     *
     * @param payment payment details to include in the consent body
     * @return payment consent request body as a JSON string
     */
    private String createPaymentConsentBody(Payment payment) {
        String[] userAccount = parseAccountIdentifier(payment.getUserAccount());
        String[] payeeAccount = parseAccountIdentifier(payment.getPayeeAccount());
        JSONObject initiation = buildInitiation(
                userAccount, payeeAccount,
                payment.getAmount(), payment.getCurrency(), payment.getReference());
        return new JSONObject()
                .put(OpenBankingConstants.FIELD_DATA,
                        new JSONObject().put(OpenBankingConstants.FIELD_INITIATION, initiation))
                .put(OpenBankingConstants.FIELD_RISK, new JSONObject())
                .toString(4);
    }

    /**
     * Builds the JSON request body for submitting a payment.
     *
     * @param payment   payment details to submit
     * @param consentId consent ID approved during the authorization step
     * @return payment submission request body as a JSON string
     */
    private String createPaymentSubmissionBody(Payment payment, String consentId) {
        String[] userAccount = parseAccountIdentifier(payment.getUserAccount());
        String[] payeeAccount = parseAccountIdentifier(payment.getPayeeAccount());
        JSONObject initiation = buildInitiation(
                userAccount, payeeAccount,
                payment.getAmount(), payment.getCurrency(), payment.getReference());
        return new JSONObject()
                .put(OpenBankingConstants.FIELD_DATA, new JSONObject()
                        .put(OpenBankingConstants.FIELD_CONSENT_ID, consentId)
                        .put(OpenBankingConstants.FIELD_INITIATION, initiation))
                .put(OpenBankingConstants.FIELD_RISK, new JSONObject())
                .toString(4);
    }

    /**
     * Builds the payment initiation JSON object from account and payment details.
     *
     * @param userAccount  parsed debtor account parts (name and ID)
     * @param payeeAccount parsed creditor account parts (name and ID)
     * @param amount       payment amount as a string
     * @param currency     payment currency code
     * @param reference    optional remittance reference text
     * @return payment initiation JSON object
     */
    private JSONObject buildInitiation(String[] userAccount, String[] payeeAccount,
                                       String amount, String currency, String reference) {
        JSONObject initiation = new JSONObject();
        initiation.put(OpenBankingConstants.FIELD_INSTRUCTION_IDENTIFICATION, generateInstructionId());
        initiation.put(OpenBankingConstants.FIELD_END_TO_END_IDENTIFICATION, generateEndToEndId());
        initiation.put(OpenBankingConstants.FIELD_LOCAL_INSTRUMENT, OpenBankingConstants.LOCAL_INSTRUMENT_PAYM);
        initiation.put(OpenBankingConstants.FIELD_INSTRUCTED_AMOUNT, buildAmount(amount, currency));
        initiation.put(OpenBankingConstants.FIELD_CREDITOR_ACCOUNT, buildCreditorAccount(payeeAccount));
        initiation.put(OpenBankingConstants.FIELD_DEBTOR_ACCOUNT, buildDebtorAccount(userAccount));
        if (reference != null && !reference.trim().isEmpty()) {
            initiation.put(OpenBankingConstants.FIELD_REMITTANCE_INFORMATION,
                    new JSONObject().put(OpenBankingConstants.FIELD_REFERENCE, reference));
        }
        initiation.put(OpenBankingConstants.FIELD_SUPPLEMENTARY_DATA,
                new JSONObject().put("additionalProp1", new JSONObject()));
        return initiation;
    }

    /**
     * Builds a JSON object representing the instructed payment amount.
     *
     * @param amount   payment amount as a string
     * @param currency payment currency code
     * @return JSON object with amount and currency fields
     */
    private JSONObject buildAmount(String amount, String currency) {
        return new JSONObject()
                .put(OpenBankingConstants.FIELD_AMOUNT, formatAmount(amount))
                .put(OpenBankingConstants.FIELD_CURRENCY, currency);
    }

    /**
     * Builds the creditor (payee) account JSON object.
     *
     * @param payeeAccount parsed payee account parts (name and ID)
     * @return JSON object representing the creditor account
     */
    private JSONObject buildCreditorAccount(String[] payeeAccount) {
        return new JSONObject()
                .put(OpenBankingConstants.FIELD_SCHEME_NAME, OpenBankingConstants.SCHEME_SORT_CODE_ACCOUNT_NUMBER)
                .put(OpenBankingConstants.FIELD_IDENTIFICATION, generateNumericId(14))
                .put(OpenBankingConstants.FIELD_NAME, payeeAccount[0])
                .put(OpenBankingConstants.FIELD_SECONDARY_IDENTIFICATION, OpenBankingConstants.PAYMENT_SECONDARY_ID_FIXED);
    }

    /**
     * Builds the debtor (user) account JSON object.
     *
     * @param userAccount parsed user account parts (name and ID)
     * @return JSON object representing the debtor account
     */
    private JSONObject buildDebtorAccount(String[] userAccount) {
        return new JSONObject()
                .put(OpenBankingConstants.FIELD_SCHEME_NAME, OpenBankingConstants.SCHEME_SORT_CODE_ACCOUNT_NUMBER)
                .put(OpenBankingConstants.FIELD_IDENTIFICATION, userAccount[1])
                .put(OpenBankingConstants.FIELD_NAME, userAccount[0])
                .put(OpenBankingConstants.FIELD_SECONDARY_IDENTIFICATION,
                        userAccount[1] + OpenBankingConstants.PAYMENT_SECONDARY_ID_SUFFIX);
    }

    /**
     * Splits an account identifier string into name and ID parts.
     *
     * @param accountIdentifier account string in "name-id" format
     * @return two-element array of name and ID
     */
    private String[] parseAccountIdentifier(String accountIdentifier) {
        String[] parts = accountIdentifier.split("-", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    /**
     * Formats an amount string to two decimal places.
     *
     * @param amount raw amount string to format
     * @return amount formatted to two decimal places, or original value if parsing fails
     */
    private String formatAmount(String amount) {
        try {
            return String.format("%.2f", Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            return amount;
        }
    }

    /**
     * Generates a unique instruction identification string.
     *
     * @return instruction ID with a constant prefix and random suffix
     */
    private String generateInstructionId() {
        return OpenBankingConstants.PAYMENT_INSTRUCTION_PREFIX
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /**
     * Generates a unique end-to-end identification string.
     *
     * @return end-to-end ID with a constant prefix and random suffix
     */
    private String generateEndToEndId() {
        return OpenBankingConstants.PAYMENT_END_TO_END_PREFIX
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /**
     * Converts a hex character to a single numeric digit (0–9).
     *
     * @param c hex character to convert
     * @return numeric digit derived from the hex character
     */
    private int hexCharToDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        int letterValue = (c >= 'a') ? (c - 'a') : (c - 'A');
        return letterValue % 10;
    }

    /**
     * Generates a numeric-only ID string of the given length using a UUID.
     *
     * @param length desired length of the numeric ID
     * @return numeric ID string of exactly the specified length
     */
    private String generateNumericId(int length) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        StringBuilder numericId = new StringBuilder();
        for (char c : uuid.toCharArray()) {
            if (numericId.length() >= length) break;
            numericId.append(hexCharToDigit(c));
        }
        while (numericId.length() < length) {
            numericId.append(RANDOM.nextInt(10));
        }
        return numericId.substring(0, length);
    }
}
