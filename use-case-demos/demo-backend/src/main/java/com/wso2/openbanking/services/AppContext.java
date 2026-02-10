package com.wso2.openbanking.services;

/**
 * @deprecated Use JwtTokenService.getInstance() instead
 * This class is kept for backward compatibility
 */
@Deprecated
public class AppContext {

    private final String clientId;
    private final String jti;
    private final JwtTokenService jwtTokenService;

    public AppContext(String clientId, String kid, String alg, String typ, String jti) throws Exception {
        this.clientId = clientId;
        this.jti = jti;
        this.jwtTokenService = JwtTokenService.getInstance();
    }

    public String createClientAsserstion() throws Exception {
        return jwtTokenService.createClientAssertion(jti);
    }

    public String getClientId() {
        return clientId;
    }

    public String makeRequestObject(String consentId) throws Exception {
        return jwtTokenService.createRequestObject(consentId, jti);
    }
}
