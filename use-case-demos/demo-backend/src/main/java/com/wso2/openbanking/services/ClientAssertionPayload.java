package com.wso2.openbanking.services;

public class ClientAssertionPayload {

    private final String iss;
    private final String sub;
    private final long exp;
    private final long iat;
    private final String jti;
    private final String aud;

    public ClientAssertionPayload(String iss, String sub, long exp, long iat, String jti, String aud) {
        this.iss = iss;
        this.sub = sub;
        this.exp = exp;
        this.iat = iat;
        this.jti = jti;
        this.aud = aud;
    }

    public String toJson() {
        return "{"
                + "\"iss\":\"" + iss + "\","
                + "\"sub\":\"" + sub + "\","
                + "\"exp\":" + exp + ","
                + "\"iat\":" + iat + ","
                + "\"jti\":\"" + jti + "\","
                + "\"aud\":\"" + aud + "\""
                + "}";
    }
}
