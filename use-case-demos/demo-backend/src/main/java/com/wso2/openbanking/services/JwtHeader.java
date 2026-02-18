package com.wso2.openbanking.services;

import org.json.JSONObject;

public class JwtHeader {

    private final String alg;
    private final String kid;
    private final String typ;

    public JwtHeader(String alg, String kid, String typ) {
        this.alg = alg;
        this.kid = kid;
        this.typ = typ;
    }

    public String toJson() {
        return new JSONObject()
                .put("alg", alg)
                .put("kid", kid)
                .put("typ", typ)
                .toString();
    }
}
