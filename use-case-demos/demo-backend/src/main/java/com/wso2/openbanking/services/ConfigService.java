package com.wso2.openbanking.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ConfigService {

    private static final String CONFIG_FILE = "config.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getConfigData() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Configuration file not found: " + CONFIG_FILE);
            }
            return objectMapper.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration file", e);
        }
    }
}
