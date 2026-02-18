package com.wso2.openbanking;

import com.fasterxml.jackson.databind.JsonNode;
import com.wso2.openbanking.exception.AuthorizationException;
import com.wso2.openbanking.exception.BankInfoLoadException;
import com.wso2.openbanking.models.*;
import com.wso2.openbanking.services.*;
import com.wso2.openbanking.utils.HtmlResponseBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Path("")
public class InitialData {

    private final BankInfoService bankInfoService;
    private final AccountService accountService;
    private final PaymentService paymentService;
    private final AuthService authService;
    private final ConfigService configService;

    public InitialData() throws Exception {
        this.bankInfoService = new BankInfoService();

        HttpTlsClient httpClient = new HttpTlsClient(
                ConfigLoader.getCertificatePath(),
                ConfigLoader.getKeyPath(),
                ConfigLoader.getTruststorePath(),
                ConfigLoader.getTruststorePassword()
        );

        this.accountService = new AccountService(bankInfoService, httpClient);
        this.paymentService = new PaymentService(bankInfoService, httpClient);
        this.authService = new AuthService(accountService, paymentService);
        this.configService = new ConfigService();
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode getData() {
        return configService.getConfigData();
    }

    @GET
    @Path("/initialize")
    @Produces(MediaType.APPLICATION_JSON)
    public Response initializeApplication() {
        try {
            bankInfoService.loadBanks();
            return Response.ok(bankInfoService.getConfigurations()).build();
        } catch (BankInfoLoadException e) {
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

    private Map<String, String> createRedirectResponse(String url) {
        Map<String, String> response = new HashMap<>();
        response.put("redirect", url);
        return response;
    }
}
