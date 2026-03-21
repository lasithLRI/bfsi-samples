package com.wso2.openbanking.demo.controller;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.models.*;
import com.wso2.openbanking.demo.services.*;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import com.wso2.openbanking.demo.utils.HtmlResponseBuilder;
import org.json.JSONArray;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("")
public class ApiController {

//    private final BankInfoService bankInfoService;
//    private final AccountService accountService;

    private final BankInfoService bankInfoService;
    private final AccountService accountService;
    private final AuthService authService;
    private final PaymentService paymentService;

//    public ApiController(BankInfoService bankInfoService) {
//        this.bankInfoService = bankInfoService;
//    }

    public ApiController() throws Exception {

        this.bankInfoService = new BankInfoService();

        HttpTlsClient httpClient = new HttpTlsClient(
                ConfigLoader.getCertificatePath(),
                ConfigLoader.getKeyPath(),
                ConfigLoader.getTruststorePath(),
                ConfigLoader.getTruststorePassword()
        );

        this.accountService = new AccountService(bankInfoService, httpClient);
        this.paymentService=new PaymentService(bankInfoService,httpClient);
        this.authService = new AuthService(accountService, paymentService);
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public String getData() {
        return "Server works";
    }

    @GET
    @Path("/initialize")
    @Produces(MediaType.APPLICATION_JSON)
    public Response initializeApplication() {
        try {
            bankInfoService.loadBanks();
            ConfigResponse config = bankInfoService.getConfigurations();
            return Response.ok(config).build();
        } catch (BankInfoLoadException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/bank")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBankData() {
        try {
            bankInfoService.loadBanks();
            return Response.ok(bankInfoService.getConfigurations()).build();
        } catch (BankInfoLoadException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/accounts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAddAccountBanks() {
        try {
            return Response.ok(bankInfoService.getAddAccountBanksInformation()).build();
        } catch (BankInfoLoadException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/addaccounts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectAccountToAdd(Map<String, String> requestBody) throws Exception {
        String redirectUrl = accountService.processAddAccount(requestBody.get("bankName"));
        authService.setRequestStatus("accounts");

        System.out.println("================================================+");

        return Response.ok(createRedirectResponse(redirectUrl)).build();
    }

    private Map<String, String> createRedirectResponse(String url) {
        Map<String, String> response = new HashMap<>();
        response.put("redirect", url);
        return response;
    }

    @GET
    @Path("/load-payment")
    @Produces(MediaType.APPLICATION_JSON)
    public LoadPaymentPageResponse getLoadPaymentData() {
        return bankInfoService.getPaymentPageInfo();
    }

    @POST
    @Path("/payment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response makePayment(Payment payment) throws Exception {
        String redirectUrl = paymentService.processPaymentRequest(payment);
        authService.setRequestStatus("payments");
        return Response.ok(createRedirectResponse(redirectUrl)).build();
    }

    @GET
    @Path("/redirected")
    @Produces("text/html")
    public Response redirectedPath() {
        String html = HtmlResponseBuilder.buildAuthRedirectPage();
        return Response.ok(html).build();
    }

    @GET
    @Path("/processAuth")
    public Response processAuth(@QueryParam("code") String code) {
        try {
            authService.processAuthorizationCallback(code);
            return Response.ok().build();
        } catch (AuthorizationException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

//    @GET
//    @Path("/get-delete-account-info")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getDeleteAccountInfo() {
//        try {
//            return Response.ok(bankInfoService.getAccountsGroupedByConsent()).build();
//        } catch (Exception e) {
//            return Response.serverError().entity(e.getMessage()).build();
//        }
//    }

    @POST
    @Path("/delete-accounts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccounts(Map<String, String> requestBody) {
        try {
            boolean success = accountService.revokeConsentAndRemoveAccounts(requestBody.get("consentId"));
            if (success) {
                return Response.ok(Collections.singletonMap("status", "success")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Failed to revoke consent").build();
            }
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/revoke-consent")
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeConsent(@QueryParam("consentId") String consentId) {
        try {
            if (consentId == null || consentId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"consentId is required\"}")
                        .build();
            }
            boolean success = accountService.revokeConsentAndRemoveAccounts(consentId);
            if (success) {
                return Response.ok("{\"status\":\"revoked\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Consent not found or revocation failed\"}")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/get-delete-account-info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeleteAccountInfo() {
        try {
            List<Map<String, Object>> groups = new ArrayList<>();
            if (bankInfoService.getBanks() != null) {
                // Group accounts by consentId
                Map<String, List<Account>> byConsent = new LinkedHashMap<>();
                for (Bank bank : bankInfoService.getBanks()) {
                    for (Account acc : bank.getAccounts()) {
                        byConsent
                                .computeIfAbsent(acc.getConsentId(), k -> new ArrayList<>())
                                .add(acc);
                    }
                }
                for (Map.Entry<String, List<Account>> entry : byConsent.entrySet()) {
                    Map<String, Object> group = new LinkedHashMap<>();
                    group.put("consentId", entry.getKey());
                    group.put("bankName", entry.getValue().get(0).getBank());
                    List<Map<String, String>> accounts = new ArrayList<>();
                    for (Account acc : entry.getValue()) {
                        Map<String, String> a = new LinkedHashMap<>();
                        a.put("id", acc.getId());
                        a.put("name", acc.getName());
                        accounts.add(a);
                    }
                    group.put("accounts", accounts);
                    groups.add(group);
                }
            }
            return Response.ok(new JSONArray(groups).toString()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
