package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.models.Account;
import com.wso2.openbanking.models.Payment;
import com.wso2.openbanking.models.Transaction;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class PaymentService {

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
            System.out.println("WARNING: No current payment to add");
            return;
        }

        try {
            String[] userAccount = parseAccountIdentifier(currentPayment.getUserAccount());
            String bankName = userAccount[0];
            String accountNumber = userAccount[1];
            double paymentAmount = Double.parseDouble(currentPayment.getAmount());

            // Create transaction with bank and account fields properly set
            Transaction transaction = createPaymentTransaction(currentPayment, bankName, accountNumber);

            // Update account balance and add transaction
            updateAccountBalance(bankName, accountNumber, paymentAmount, transaction);

            System.out.println("✓ Payment transaction added successfully");
            System.out.println("  Transaction ID: " + transaction.getId());
            System.out.println("  Bank: " + transaction.getBank());
            System.out.println("  Account: " + transaction.getAccount());
            System.out.println("  Date: " + transaction.getDate());
            System.out.println("  Amount: " + transaction.getCurrency() + " " + transaction.getAmount());

        } catch (Exception e) {
            System.err.println("ERROR: Failed to add payment to account: " + e.getMessage());
            e.printStackTrace();
        } finally {
            currentPayment = null;
        }
    }

    // ==================== Transaction Management ====================

    /**
     * Creates a payment transaction with proper date format (yyyy-MM-dd)
     * Sets the date to today's date instead of using day of month
     */
    private Transaction createPaymentTransaction(Payment payment, String bankName, String accountNumber) {
        Transaction transaction = new Transaction();

        // Set basic transaction fields
        // Generate ID in format: T00123456
        transaction.setId(generateTransactionId());

        // Use current date in yyyy-MM-dd format (consistent with loaded transactions)
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        transaction.setDate(currentDate);

        transaction.setReference(payment.getReference());
        transaction.setAmount(payment.getAmount());
        transaction.setCurrency(payment.getCurrency());
        transaction.setCreditDebitStatus("c");  // 'd' for debit (outgoing payment)

        // CRITICAL: Set bank and account fields so they appear in the flat transactions array
        transaction.setBank(bankName);
        transaction.setAccount(accountNumber);

        System.out.println("Created transaction with ID: " + transaction.getId() + " and date: " + currentDate);

        return transaction;
    }

    private void updateAccountBalance(String bankName, String accountNumber,
                                      double amount, Transaction transaction) {
        Optional<Account> accountOpt = bankInfoService.findAccount(bankName, accountNumber);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();

            // Check for sufficient balance
            double currentBalance = account.getBalance();
            if (currentBalance < amount) {
                System.err.println("ERROR: Insufficient balance. Required: " + amount + ", Available: " + currentBalance);
                throw new RuntimeException("Insufficient balance");
            }

            // Deduct balance
            account.setBalance(currentBalance - amount);

            // Add transaction to account at index 0 (top of list)
            // The addTransactionToAccount method will handle sorting
            bankInfoService.addTransactionToAccount(account, transaction);

            System.out.println("✓ Account balance updated: " + currentBalance + " -> " + (currentBalance - amount));
        } else {
            System.err.println("ERROR: Account not found - Bank: " + bankName + ", Account: " + accountNumber);
            throw new RuntimeException("Account not found");
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

    /**
     * Generates a transaction ID in the format T00123456
     * T followed by 8 digits with leading zeros
     */
    private String generateTransactionId() {
        // Generate a random 8-digit number
        int randomNumber = (int) (Math.random() * 100000000);
        // Format with leading zeros to ensure 8 digits
        return String.format("T%08d", randomNumber);
    }

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
