package com.wso2.openbanking.services;

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
        return "{"
                + "\"alg\":\"" + alg + "\","
                + "\"kid\":\"" + kid + "\","
                + "\"typ\":\"" + typ + "\""
                + "}";
    }

    public String getAlg() {
        return alg;
    }

    public String getKid() {
        return kid;
    }

    public String getTyp() {
        return typ;
    }
}
