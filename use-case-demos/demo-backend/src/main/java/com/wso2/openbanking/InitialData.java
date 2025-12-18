package com.wso2.openbanking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Path("")
public class InitialData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode getData(){
        String fileName = "config.json";

        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)){

            if (inputStream == null) {
                return null;
            }

            return objectMapper.readTree(inputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
