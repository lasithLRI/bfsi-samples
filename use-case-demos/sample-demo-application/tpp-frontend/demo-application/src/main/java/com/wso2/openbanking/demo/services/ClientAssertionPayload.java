package com.wso2.openbanking.demo.services;

import org.json.JSONObject;

/**
 * Represents the payload of a JWT client assertion used to authenticate
 * this application to the token endpoint during OAuth flows.
 *
 * Once constructed, the payload is serialized to JSON via toJson()
 * for embedding in a signed JWT.
 */
public class ClientAssertionPayload {

    /** The issuer — typically the client ID of this application. */
    private final String iss;

    /** The subject — typically the same as the issuer for client assertions. */
    private final String sub;

    /** The expiration time as a Unix epoch timestamp (seconds). */
    private final long exp;

    /** The issued-at time as a Unix epoch timestamp (seconds). */
    private final long iat;

    /** A unique JWT ID used to prevent replay attacks. */
    private final String jti;

    /** The intended audience — typically the token endpoint URL. */
    private final String aud;

    /**
     * Constructs a new ClientAssertionPayload with all required JWT claims.
     *
     * @param iss the issuer claim — typically the client ID of this application.
     * @param sub the subject claim — typically the same as iss for client assertions.
     * @param exp the expiration time as a Unix epoch timestamp (seconds).
     * @param iat the issued-at time as a Unix epoch timestamp (seconds).
     * @param jti a unique JWT ID to prevent replay attacks.
     * @param aud the audience claim — typically the token endpoint URL.
     */
    public ClientAssertionPayload(String iss, String sub, long exp, long iat, String jti, String aud) {
        this.iss = iss;
        this.sub = sub;
        this.exp = exp;
        this.iat = iat;
        this.jti = jti;
        this.aud = aud;
    }

    /**
     * Serializes this payload to a JSON string for embedding in a JWT.
     *
     * @return a JSON string containing all JWT claims.
     */
    public String toJson() {
        return new JSONObject()
                .put("iss", iss)
                .put("sub", sub)
                .put("exp", exp)
                .put("iat", iat)
                .put("jti", jti)
                .put("aud", aud)
                .toString();
    }
}
