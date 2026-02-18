package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.utils.JwtUtils;
import org.json.JSONObject;

public class OAuthTokenService {

    private final HttpTlsClient client;
    private final JwtTokenService jwtTokenService;

    public OAuthTokenService(HttpTlsClient client) throws Exception {
        this.client = client;
        this.jwtTokenService = JwtTokenService.getInstance();
    }

    public String getToken(String scope) {
        try {
            String clientAssertion = jwtTokenService.createClientAssertion(JwtUtils.generateJti());
            String body = buildTokenRequestBody(scope, clientAssertion);
            return client.postJwt(ConfigLoader.getTokenUrl(), body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String initializeConsent(String token, String consentBody, String url) throws Exception {
        String accessToken = new JSONObject(token).getString("access_token");
        return client.postConsentInit(url, consentBody, accessToken);
    }

    public String authorizeConsent(String consentResponse, String scope) throws Exception {
        String consentId = extractConsentId(consentResponse);
        String requestObject = jwtTokenService.createRequestObject(consentId, JwtUtils.generateJti());
        return client.postConsentAuthRequest(requestObject, ConfigLoader.getClientId(), scope);
    }

    private String extractConsentId(String consentResponse) {
        return new JSONObject(consentResponse)
                .getJSONObject("Data")
                .getString("ConsentId");
    }

    private String buildTokenRequestBody(String scope, String clientAssertion) {
        return "grant_type=client_credentials" +
                "&scope=" + scope +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }
}
