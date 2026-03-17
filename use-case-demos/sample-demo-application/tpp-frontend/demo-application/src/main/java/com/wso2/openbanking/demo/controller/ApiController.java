package com.wso2.openbanking.demo.controller;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.models.ConfigResponse;
import com.wso2.openbanking.demo.services.AccountService;
import com.wso2.openbanking.demo.services.AuthService;
import com.wso2.openbanking.demo.services.BankInfoService;
import com.wso2.openbanking.demo.services.HttpTlsClient;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import com.wso2.openbanking.demo.utils.HtmlResponseBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Path("")
public class ApiController {

//    private final BankInfoService bankInfoService;
//    private final AccountService accountService;

    private final BankInfoService bankInfoService;
    private final AccountService accountService;
    private final AuthService authService;

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
        this.authService = new AuthService(accountService);


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
}
