package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.models.Account;
import com.wso2.openbanking.models.Payment;
import com.wso2.openbanking.models.Transaction;
import org.json.JSONObject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class PaymentService {

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+05:30");

    private final BankInfoService bankInfoService;
    private final OAuthTokenService oauthService;
    private Payment currentPayment;

    public PaymentService(BankInfoService bankInfoService, HttpTlsClient client) throws Exception {
        this.bankInfoService = bankInfoService;
        this.oauthService = new OAuthTokenService(client);
    }

    // ==================== Public API ====================

    public String processPaymentRequest(Payment payment) throws Exception {
        this.currentPayment = payment;

        String token = oauthService.getToken("payments openid");
        String paymentUrl = ConfigLoader.getPaymentBaseUrl() + "/payment-consents";
        String consentBody = createPaymentConsentBody(payment);
        String consentResponse = oauthService.initializeConsent(token, consentBody, paymentUrl);

        return oauthService.authorizeConsent(consentResponse, "payments openid");
    }

    public void addPaymentToAccount() {
        if (currentPayment == null) {
            return;
        }

        try {
            String[] userAccount = parseAccountIdentifier(currentPayment.getUserAccount());
            double paymentAmount = Double.parseDouble(currentPayment.getAmount());
            Transaction transaction = createPaymentTransaction(currentPayment);

            updateAccountBalance(userAccount[0], userAccount[1], paymentAmount, transaction);
        } finally {
            currentPayment = null;
        }
    }

    // ==================== Transaction Management ====================

    private Transaction createPaymentTransaction(Payment payment) {
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        String currentDate = ZonedDateTime.now(ZONE_OFFSET).format(DATETIME_FORMATTER);

        return new Transaction(
                transactionId,
                currentDate,
                payment.getReference(),
                payment.getAmount(),
                payment.getCurrency(),
                "Debit"
        );
    }

    private void updateAccountBalance(String bankName, String accountNumber,
                                      double amount, Transaction transaction) {
        Optional<Account> accountOpt = bankInfoService.findAccount(bankName, accountNumber);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setBalance(account.getBalance() - amount);
            bankInfoService.addTransactionToAccount(account, transaction);
        }
    }

    // ==================== Consent Body Building ====================

    private String createPaymentConsentBody(Payment payment) {
        String[] userAccount = parseAccountIdentifier(payment.getUserAccount());
        String[] payeeAccount = parseAccountIdentifier(payment.getPayeeAccount());

        JSONObject consent = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject initiation = buildInitiation(
                userAccount,
                payeeAccount,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getReference()
        );

        data.put("Initiation", initiation);
        consent.put("Data", data);
        consent.put("Risk", new JSONObject());

        return consent.toString(4);
    }

    private JSONObject buildInitiation(String[] userAccount, String[] payeeAccount,
                                       String amount, String currency, String reference) {
        JSONObject initiation = new JSONObject();

        // Identifiers
        initiation.put("InstructionIdentification", generateInstructionId());
        initiation.put("EndToEndIdentification", generateEndToEndId());
        initiation.put("LocalInstrument", "OB.Paym");

        // Amount
        initiation.put("InstructedAmount", buildAmount(amount, currency));

        // Accounts
        initiation.put("CreditorAccount", buildCreditorAccount(payeeAccount));
        initiation.put("DebtorAccount", buildDebtorAccount(userAccount));

        // Optional reference
        if (reference != null && !reference.trim().isEmpty()) {
            JSONObject remittanceInfo = new JSONObject();
            remittanceInfo.put("Reference", reference);
            initiation.put("RemittanceInformation", remittanceInfo);
        }

        // Supplementary data
        JSONObject supplementaryData = new JSONObject();
        supplementaryData.put("additionalProp1", new JSONObject());
        initiation.put("SupplementaryData", supplementaryData);

        return initiation;
    }

    private JSONObject buildAmount(String amount, String currency) {
        JSONObject amountObj = new JSONObject();
        amountObj.put("Amount", formatAmount(amount));
        amountObj.put("Currency", currency);
        return amountObj;
    }

    private JSONObject buildCreditorAccount(String[] payeeAccount) {
        JSONObject creditor = new JSONObject();
        creditor.put("SchemeName", "OB.SortCodeAccountNumber");
        creditor.put("Identification", generateNumericId(14));
        creditor.put("Name", payeeAccount[0]);
        creditor.put("SecondaryIdentification", "0002");
        return creditor;
    }

    private JSONObject buildDebtorAccount(String[] userAccount) {
        JSONObject debtor = new JSONObject();
        debtor.put("SchemeName", "OB.SortCodeAccountNumber");
        debtor.put("Identification", userAccount[1]);
        debtor.put("Name", userAccount[0]);
        debtor.put("SecondaryIdentification", userAccount[1] + "001");
        return debtor;
    }

    // ==================== Helper Methods ====================

    private String[] parseAccountIdentifier(String accountIdentifier) {
        String[] parts = accountIdentifier.split("-", 2);
        String bankName = parts[0];
        String accountNumber = parts.length > 1 ? parts[1] : "";
        return new String[]{bankName, accountNumber};
    }

    private String formatAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return String.format("%.2f", value);
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

    private String generateNumericId(int length) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        StringBuilder numericId = new StringBuilder();

        for (char c : uuid.toCharArray()) {
            if (numericId.length() >= length) break;

            int digit = (c >= '0' && c <= '9')
                    ? c - '0'
                    : (c >= 'a' ? c - 'a' : c - 'A') % 10;
            numericId.append(digit);
        }

        while (numericId.length() < length) {
            numericId.append((int) (Math.random() * 10));
        }

        return numericId.substring(0, length);
    }
}
