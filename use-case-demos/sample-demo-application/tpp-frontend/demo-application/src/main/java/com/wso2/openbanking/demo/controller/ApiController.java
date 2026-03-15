package com.wso2.openbanking.demo.controller;

import com.wso2.openbanking.demo.exceptions.BankInfoLoadException;
import com.wso2.openbanking.demo.models.ConfigResponse;
import com.wso2.openbanking.demo.services.BankInfoService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("")
public class ApiController {

    private final BankInfoService bankInfoService;

//    public ApiController(BankInfoService bankInfoService) {
//        this.bankInfoService = bankInfoService;
//    }

    public ApiController() {
        this.bankInfoService = new BankInfoService();
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
        } catch (BankInfoLoadException | IOException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
