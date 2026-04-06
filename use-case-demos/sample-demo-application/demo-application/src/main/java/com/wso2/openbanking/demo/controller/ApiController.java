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

package com.wso2.openbanking.demo.controller;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.models.*;
import com.wso2.openbanking.demo.service.AccountService;
import com.wso2.openbanking.demo.service.AuthService;
import com.wso2.openbanking.demo.service.HttpTlsClient;
import com.wso2.openbanking.demo.service.PaymentService;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/** ApiController implementation. */
@Path("")
public final class ApiController {

    private final AccountService accountService;
    private final AuthService authService;
    private final PaymentService paymentService;

    public ApiController() throws BankInfoLoadException {

        try {
            HttpTlsClient httpClient = new HttpTlsClient(
                    ConfigLoader.getCertificatePath(),
                    ConfigLoader.getKeyPath()

            );

            this.accountService = AccountService.create(httpClient);
            this.paymentService = PaymentService.create(httpClient);
            this.authService = AuthService.create(accountService, paymentService, httpClient);

        } catch (SSLContextCreationException | GeneralSecurityException | IOException e) {
            throw new BankInfoLoadException("Failed to initialize API controller: " + e.getMessage(), e);
        }
    }

    /**
     * Initiates the account addition flow and returns a redirect URL.
     *
     * @param requestBody a map containing the request parameters for adding an account
     * @return a 200 OK response containing the redirect URL for the account addition flow
     */
    @POST
    @Path("/add-accounts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectAccountToAdd(Map<String, String> requestBody) throws Exception {
        String redirectUrl = accountService.processAddAccount();
        authService.setRequestStatus("accounts");
        return Response.ok(createRedirectResponse(redirectUrl)).build();
    }

    private Map<String, String> createRedirectResponse(String url) {
        Map<String, String> response = new HashMap<>();
        response.put("redirect", url);
        return response;
    }

    /**
     * Processes a payment request and returns a redirect URL.
     *
     * @param payment the payment object containing the payment details
     * @return a 200 OK response containing the redirect URL for the payment flow
     */
    @POST
    @Path("/payment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response makePayment(Payment payment) throws Exception {
        String redirectUrl = paymentService.processPaymentRequest(payment);
        authService.setRequestStatus("payments");
        return Response.ok(createRedirectResponse(redirectUrl)).build();
    }

    /**
     * Handles the OAuth authorization callback and returns a status response based on the request type.
     *
     * @param code the authorization code received from the OAuth callback
     * @return a 200 OK response with account or payment status, or a 500 error response if authorization fails
     */
    @GET
    @Path("/processAuth")
    @Produces(MediaType.APPLICATION_JSON)
    public Response processAuth(@QueryParam("code") String code) throws IOException {
        try {
            authService.processAuthorizationCallback(code);

            String status = authService.getRequestStatus();

            if ("accounts".equals(status)) {
                Map<String, Object> response = getStringObjectMap();

                return Response.ok(new JSONObject(response).toString())
                        .type(MediaType.APPLICATION_JSON)
                        .build();

            } else if ("payments".equals(status)) {
                boolean success = authService.isLastPaymentSuccess();

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("type",    "payments");
                response.put("status",  "success");
                response.put("success", success);

                return Response.ok(new JSONObject(response).toString())
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            return Response.ok("{\"status\":\"success\",\"type\":\"" + status + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } catch (AuthorizationException e) {
            return Response.serverError()
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    private Map<String, Object> getStringObjectMap() {
        List<Account> accounts = authService.getLastFetchedAccounts();

        List<Map<String, Object>> accountsList = new ArrayList<>();
        for (Account acc : accounts) {
            List<Map<String, Object>> txnList = new ArrayList<>();
            if (acc.getTransactions() != null) {
                for (Transaction txn : acc.getTransactions()) {
                    Map<String, Object> t = new LinkedHashMap<>();
                    t.put("id",                txn.getId());
                    t.put("date",              txn.getDate());
                    t.put("reference",         txn.getReference());
                    t.put("account",           txn.getAccount());
                    t.put("amount",            txn.getAmount());
                    t.put("currency",          txn.getCurrency());
                    t.put("creditDebitStatus", txn.getCreditDebitStatus());
                    txnList.add(t);
                }
            }
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id",           acc.getId());
            a.put("name",         acc.getName());
            a.put("balance",      acc.getBalance());
            a.put("consentId",    acc.getConsentId());
            a.put("transactions", txnList);
            accountsList.add(a);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type",     "accounts");
        response.put("status",   "success");
        response.put("accounts", accountsList);
        return response;
    }

    /**
     * Revokes the consent for a linked bank account.
     *
     * @param accountId the unique identifier of the account whose consent is to be revoked
     * @param bankName  the name of the bank associated with the account
     * @param consentId the unique identifier of the consent to be revoked
     * @return a 200 OK response if revocation succeeds, 400 if required params are missing, 404 if account is not found, or 500 on error
     */
    @DELETE
    @Path("/revoke-consent")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeConsent(@QueryParam("accountId") String accountId,
                                  @QueryParam("bankName") String bankName,
                                  @QueryParam("consentId") String consentId) {
        try {
            if (accountId == null || accountId.isEmpty() || bankName == null || bankName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"accountId and bankName are required\"}")
                        .build();
            }
            boolean success = accountService.revokeAccountConsent(accountId, bankName,consentId);
            if (success) {
                return Response.ok("{\"status\":\"revoked\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Account not found or revocation failed\"}")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
