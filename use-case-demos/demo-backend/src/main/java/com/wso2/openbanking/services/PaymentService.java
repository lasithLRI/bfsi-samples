package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.exception.AuthorizationException;
import com.wso2.openbanking.exception.PaymentException;
import com.wso2.openbanking.models.Account;
import com.wso2.openbanking.models.Payment;
import com.wso2.openbanking.models.Transaction;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class PaymentService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Random RANDOM = new Random();

    private final BankInfoService bankInfoService;
    private final OAuthTokenService oauthService;
    private Payment currentPayment;

    public PaymentService(BankInfoService bankInfoService, HttpTlsClient client)
            throws Exception {
        this.bankInfoService = bankInfoService;
        this.oauthService = new OAuthTokenService(client);
    }

    public String processPaymentRequest(Payment payment) throws AuthorizationException {
        this.currentPayment = payment;
        try {
            String token = oauthService.getToken("payments openid");
            String paymentUrl = ConfigLoader.getPaymentBaseUrl() + "/payment-consents";
            String consentBody = createPaymentConsentBody(payment);
            String consentResponse = oauthService.initializeConsent(token, consentBody, paymentUrl);
            return oauthService.authorizeConsent(consentResponse, "payments openid");
        } catch (Exception e) {
            throw new AuthorizationException("Failed to process payment consent request", e);
        }
    }

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

    private String generateTransactionId() {
        return String.format("T%08d", RANDOM.nextInt(100_000_000));
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
        return "INST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateEndToEndId() {
        return "E2E-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private int hexCharToDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        int letterValue = (c >= 'a') ? (c - 'a') : (c - 'A');
        return letterValue % 10;
    }

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
