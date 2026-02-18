package com.wso2.openbanking.services;

public class AppContext {

    private final String clientId;
    private final String jti;
    private final JwtTokenService jwtTokenService;

    public AppContext(String clientId, String kid, String alg, String typ, String jti) throws Exception {
        this.clientId = clientId;
        this.jti = jti;
        this.jwtTokenService = JwtTokenService.getInstance();
    }

    public String createClientAssertion() throws Exception {
        return jwtTokenService.createClientAssertion(jti);
    }

    public String getClientId() {
        return clientId;
    }

    public String makeRequestObject(String consentId) throws Exception {
        return jwtTokenService.createRequestObject(consentId);
    }
}
