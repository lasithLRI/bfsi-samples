package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.models.Account;
import com.wso2.openbanking.models.Payment;
import com.wso2.openbanking.models.Transaction;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class PaymentService {

    private final BankInfoService bankInfoService;
    private final OAuthTokenService oauthService;
    private Payment currentPayment;

    public PaymentService(BankInfoService bankInfoService, HttpTlsClient client) throws Exception {
        this.bankInfoService = bankInfoService;
        this.oauthService = new OAuthTokenService(client);
    }

    public String processPaymentRequest(Payment payment) throws Exception {
        this.currentPayment = payment;

        String token = oauthService.getToken("payments openid");
        System.out.println("Obtained payment token");

        String paymentUrl = ConfigLoader.getPaymentBaseUrl() + "/payment-consents";
        String consentBody = createPaymentConsentBody();
        String consentResponse = oauthService.initializeConsent(token, consentBody, paymentUrl);

        String authUrl = oauthService.authorizeConsent(consentResponse, "payments openid");
        System.out.println("Payment authorization URL: " + authUrl);

        return authUrl;
    }

    public void addPaymentToAccount() throws Exception {
        if (currentPayment == null) {
            System.out.println("No payment to process");
            return;
        }

        String[] accountParts = parseAccountIdentifier(currentPayment.getUserAccount());
        String bankName = accountParts[0];
        String accountNumber = accountParts[1];

        double paymentAmount = Double.parseDouble(currentPayment.getAmount());
        Transaction transaction = createPaymentTransaction();

        updateAccountWithPayment(bankName, accountNumber, paymentAmount, transaction);

        currentPayment = null;
    }

    private String[] parseAccountIdentifier(String accountIdentifier) {
        String[] parts = accountIdentifier.split("-", 2);
        String bankName = parts[0];
        String accountNumber = parts.length > 1 ? parts[1] : "";
        return new String[]{bankName, accountNumber};
    }

    private Transaction createPaymentTransaction() {
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZoneOffset offset = ZoneOffset.of("+05:30");
        ZonedDateTime now = ZonedDateTime.now(offset);
        String currentDate = now.format(formatter);

        return new Transaction(
                transactionId,
                currentDate,
                currentPayment.getReference(),
                currentPayment.getAmount(),
                currentPayment.getCurrency(),
                "Debit"
        );
    }

    private void updateAccountWithPayment(String bankName, String accountNumber,
                                          double amount, Transaction transaction) {
        Optional<Account> accountOpt = bankInfoService.findAccount(bankName, accountNumber);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setBalance(account.getBalance() - amount);
            bankInfoService.addTransactionToAccount(account, transaction);
            System.out.println("Payment processed successfully: " + bankName + "-" + accountNumber);
        } else {
            System.out.println("Account not found for payment: " + bankName + "-" + accountNumber);
        }
    }

    private String createPaymentConsentBody() {
        return "{\n" +
                "    \"Data\": {\n" +
                "        \"Initiation\": {\n" +
                "            \"InstructionIdentification\": \"ACME412\",\n" +
                "            \"EndToEndIdentification\": \"FRESCO.21302.GFX.20\",\n" +
                "            \"LocalInstrument\": \"OB.Paym\",\n" +
                "            \"InstructedAmount\": {\n" +
                "                \"Amount\": \"165.88\",\n" +
                "                \"Currency\": \"GBP\"\n" +
                "            },\n" +
                "            \"CreditorAccount\": {\n" +
                "                \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
                "                \"Identification\": \"08080021325698\",\n" +
                "                \"Name\": \"ACME Inc\",\n" +
                "                \"SecondaryIdentification\": \"0002\"\n" +
                "            },\n" +
                "            \"DebtorAccount\": {\n" +
                "                \"SchemeName\": \"OB.SortCodeAccountNumber\",\n" +
                "                \"Identification\": \"08080025612489\",\n" +
                "                \"Name\": \"Jane Smith\",\n" +
                "                \"SecondaryIdentification\": \"080801562314789\"\n" +
                "            },\n" +
                "            \"SupplementaryData\": {\n" +
                "                \"additionalProp1\": {}\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"Risk\": {}\n" +
                "}";
    }
}
