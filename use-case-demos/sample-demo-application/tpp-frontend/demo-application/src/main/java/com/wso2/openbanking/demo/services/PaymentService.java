package com.wso2.openbanking.demo.services;


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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Handles payment consent initiation and applies completed payments
 * to the in-memory account ledger.
 */
public class PaymentService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Random RANDOM = new Random();

    private final BankInfoService bankInfoService;
    private final OAuthTokenService oauthService;
    private Payment currentPayment;

    /** Initialises the service and creates an OAuthTokenService for the payment flow. */
    public PaymentService(BankInfoService bankInfoService, HttpTlsClient client)
            throws GeneralSecurityException, IOException {
        this.bankInfoService = bankInfoService;
        this.oauthService = new OAuthTokenService(client);
    }

    /**
     * Initiates the payment consent flow — obtains a token, posts the consent,
     * and returns the authorization redirect URL for user approval.
     */
    public String processPaymentRequest(Payment payment) throws AuthorizationException {
        this.currentPayment = payment;
        try {
            String token = oauthService.getToken("payments openid");
            String paymentUrl = ConfigLoader.getPaymentBaseUrl() + "/payment-consents";
            String consentBody = createPaymentConsentBody(payment);
            String consentResponse = oauthService.initializeConsent(token, consentBody, paymentUrl);
            return oauthService.authorizeConsent(consentResponse, "payments openid");
        } catch (IOException e) {
            throw new AuthorizationException("Failed to contact payment consent endpoint", e);
        } catch (GeneralSecurityException e) {
            throw new AuthorizationException("Failed to sign payment consent request", e);
        }
    }

    /**
     * Applies the current pending payment to the user's account balance
     * and records it as a transaction. Clears currentPayment when done.
     */
    public void addPaymentToAccount() throws PaymentException {
        if (currentPayment == null) {
            return;
        }
        try {
            String[] userAccount = parseAccountIdentifier(currentPayment.getUserAccount());
            String bankName = userAccount[0];
            String accountNumber = userAccount[1];
            double paymentAmount = Double.parseDouble(currentPayment.getAmount());
            Transaction transaction = createPaymentTransaction(currentPayment, bankName, accountNumber);
            updateAccountBalance(bankName, accountNumber, paymentAmount, transaction);
        } catch (NumberFormatException e) {
            throw new PaymentException("Invalid payment amount: " + currentPayment.getAmount(), e);
        } finally {
            currentPayment = null;
        }
    }

    /** Builds a Transaction model from the current payment details. */
    private Transaction createPaymentTransaction(Payment payment, String bankName, String accountNumber) {
        Transaction transaction = new Transaction();
        transaction.setId(generateTransactionId());
        transaction.setDate(LocalDate.now().format(DATE_FORMATTER));
        transaction.setReference(payment.getReference());
        transaction.setAmount(payment.getAmount());
        transaction.setCurrency(payment.getCurrency());
        transaction.setCreditDebitStatus("c");
        transaction.setBank(bankName);
        transaction.setAccount(accountNumber);
        return transaction;
    }

    /** Validates the account exists and has sufficient balance, then deducts and records the transaction. */
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

    /** Builds the full payment consent request body as a JSON string. */
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

    /** Builds the Initiation object within the payment consent body. */
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

    /** Builds the InstructedAmount JSON object. */
    private JSONObject buildAmount(String amount, String currency) {
        return new JSONObject()
                .put("Amount", formatAmount(amount))
                .put("Currency", currency);
    }

    /** Builds the CreditorAccount JSON object for the payee. */
    private JSONObject buildCreditorAccount(String[] payeeAccount) {
        return new JSONObject()
                .put("SchemeName", "OB.SortCodeAccountNumber")
                .put("Identification", generateNumericId(14))
                .put("Name", payeeAccount[0])
                .put("SecondaryIdentification", "0002");
    }

    /** Builds the DebtorAccount JSON object for the payer. */
    private JSONObject buildDebtorAccount(String[] userAccount) {
        return new JSONObject()
                .put("SchemeName", "OB.SortCodeAccountNumber")
                .put("Identification", userAccount[1])
                .put("Name", userAccount[0])
                .put("SecondaryIdentification", userAccount[1] + "001");
    }

    /** Generates a random transaction ID in the format T########. */
    private String generateTransactionId() {
        return String.format("T%08d", RANDOM.nextInt(100_000_000));
    }

    /** Splits a "BankName-AccountNumber" identifier into a two-element array. */
    private String[] parseAccountIdentifier(String accountIdentifier) {
        String[] parts = accountIdentifier.split("-", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    /** Formats an amount string to two decimal places, returning the original if unparseable. */
    private String formatAmount(String amount) {
        try {
            return String.format("%.2f", Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            return amount;
        }
    }

    /** Generates a short unique instruction ID prefixed with "INST-". */
    private String generateInstructionId() {
        return "INST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /** Generates a short unique end-to-end ID prefixed with "E2E-". */
    private String generateEndToEndId() {
        return "E2E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /** Converts a hex character to a single decimal digit. */
    private int hexCharToDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        int letterValue = (c >= 'a') ? (c - 'a') : (c - 'A');
        return letterValue % 10;
    }

    /**
     * Generates a numeric-only ID of the given length by converting
     * UUID hex characters to digits, padding with random digits if needed.
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
