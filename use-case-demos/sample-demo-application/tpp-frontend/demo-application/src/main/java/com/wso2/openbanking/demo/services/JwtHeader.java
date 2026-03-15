package com.wso2.openbanking.demo.services;

import org.json.JSONObject;

/**
 * Represents the header of a JWT, carrying the signing algorithm,
 * key ID, and token type used during JWT construction.
 */
public class JwtHeader {

    private final String alg;
    private final String kid;
    private final String typ;

    public JwtHeader(String alg, String kid, String typ) {
        this.alg = alg;
        this.kid = kid;
        this.typ = typ;
    }

    /** Serializes the header to a JSON string for embedding in a JWT. */
    public String toJson() {
        return new JSONObject()
                .put("alg", alg)
                .put("kid", kid)
                .put("typ", typ)
                .toString();
    }
}
