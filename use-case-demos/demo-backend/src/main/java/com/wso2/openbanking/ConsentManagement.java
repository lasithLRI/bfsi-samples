package com.wso2.openbanking;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/consent")
public class ConsentManagement {
    @GET
    @Path("/test")
    public String test() {
        System.out.println("test");
        return "test";
    }
}
