package com.wso2.openbanking.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingOrdersTableHeaderData {
    private Map<String, String> headerFields = new HashMap<>();

    public StandingOrdersTableHeaderData() {
    }

    public StandingOrdersTableHeaderData(Map<String, String> headerFields) {
        this.headerFields = headerFields;
    }

    @JsonAnySetter
    public void addHeaderField(String key, Object value) {
        this.headerFields.put(key, value.toString());
    }

    public Map<String, String> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(Map<String, String> headerFields) {
        this.headerFields = headerFields;
    }
}
