package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.models.Account;
import com.wso2.openbanking.models.Payment;
import com.wso2.openbanking.models.Transaction;
import org.json.JSONObject;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class PaymentService {

    private final BankInfoService bankInfoService;
    private final HttpTlsClient client;
    private Payment currentPayment;

    public PaymentService(BankInfoService bankInfoService) throws Exception {
        this.bankInfoService = bankInfoService;
        this.client = new HttpTlsClient(
                ConfigLoader.getCertificatePath(),
                ConfigLoader.getKeyPath(),
                ConfigLoader.getTruststorePath(),
                ConfigLoader.getTruststorePassword()
        );
    }

    public String processPaymentRequest(Payment payment) throws Exception {
        this.currentPayment = payment;

        String token = getToken("payments openid");
        System.out.println("token: " + token);

        String paymentUrl = ConfigLoader.getPaymentBaseUrl() + "/payment-consents";
        String consentBody = createPaymentConsentBody();
        String response = consentInit(token, consentBody, paymentUrl);

        String url = consentAuth(response, "payments openid");
        System.out.println("Payment authorization URL: " + url);

        return url;
    }

    public void addPaymentToAccount() throws Exception {
        if (currentPayment == null) {
            System.out.println("No payment to process");
            return;
        }

        String account = currentPayment.getUserAccount();
        String[] parts = account.split("-", 2);
        String bankName = parts[0];
        String accountNumber = parts.length > 1 ? parts[1] : "";

        double paymentAmount = Double.parseDouble(currentPayment.getAmount());
        Transaction transaction = createPaymentTransaction();

        updateAccountWithPayment(bankName, accountNumber, paymentAmount, transaction);

        currentPayment = null;
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

    private String getToken(String scope) {
        try {
            String jti = new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
            AppContext context = new AppContext(
                    ConfigLoader.getClientId(),
                    ConfigLoader.getClientSecret(),
                    ConfigLoader.getOAuthAlgorithm(),
                    ConfigLoader.getTokenType(),
                    jti
            );

            String body = "grant_type=client_credentials" +
                    "&scope=" + scope +
                    "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                    "&client_id=" + context.getClientId() +
                    "&client_assertion=" + context.createClientAsserstion() +
                    "&redirect_uri=https://www.google.com/redirects/redirect1";

            String response = client.postJwt(ConfigLoader.getTokenUrl(), body);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String consentInit(String token, String consentBody, String url) throws Exception {
        JSONObject jsonObject = new JSONObject(token);
        token = jsonObject.get("access_token").toString();
        String response = client.postPaymentConsentInit(url, consentBody, token);
        return response;
    }

    private String consentAuth(String consentInit, String scope) throws Exception {
        JSONObject jsonObject = new JSONObject(consentInit);
        JSONObject data = jsonObject.getJSONObject("Data");
        String consentId = data.getString("ConsentId");

        String jti = new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
        AppContext context = new AppContext(
                ConfigLoader.getClientId(),
                ConfigLoader.getClientSecret(),
                ConfigLoader.getOAuthAlgorithm(),
                ConfigLoader.getTokenType(),
                jti
        );

        String requestObject = context.makeRequestObject(consentId);
        String url = client.postConsentAuthRequest(requestObject, ConfigLoader.getClientId(), scope);

        return url;
    }
}
