package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.UUID;

public class OAuthTokenService {

    private final HttpTlsClient client;
    private final JwtTokenService jwtTokenService;

    public OAuthTokenService(HttpTlsClient client) throws Exception {
        this.client = client;
        this.jwtTokenService = JwtTokenService.getInstance();
    }

    /**
     * Get OAuth token for the specified scope
     */
    public String getToken(String scope) {
        try {
            String jti = generateJti();
            String clientAssertion = jwtTokenService.createClientAssertion(jti);

            String body = buildTokenRequestBody(scope, clientAssertion);
            return client.postJwt(ConfigLoader.getTokenUrl(), body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initialize consent with the authorization server
     */
    public String initializeConsent(String token, String consentBody, String url) throws Exception {
        JSONObject jsonObject = new JSONObject(token);
        String accessToken = jsonObject.getString("access_token");
        System.out.println("accessToken: " + accessToken);
        System.out.println("consentBody: " + consentBody);
        System.out.println("url: " + url);
        String aaaa = client.postConsentInit(url, consentBody, accessToken);
        System.out.println("sijdvhdshv$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println(aaaa+"8888888888888888888888888888888");
        return aaaa;
    }

    /**
     * Authorize consent and get authorization URL
     */
    public String authorizeConsent(String consentResponse, String scope) throws Exception {
        String consentId = extractConsentId(consentResponse);
        String jti = generateJti();
        String requestObject = jwtTokenService.createRequestObject(consentId, jti);
        System.out.println("requestObject: " + requestObject);
        return client.postConsentAuthRequest(requestObject, ConfigLoader.getClientId(), scope);
    }

    private String extractConsentId(String consentResponse) {
        JSONObject jsonObject = new JSONObject(consentResponse);
        JSONObject data = jsonObject.getJSONObject("Data");
        return data.getString("ConsentId");
    }

    private String buildTokenRequestBody(String scope, String clientAssertion) {
        return "grant_type=client_credentials" +
                "&scope=" + scope +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }

    private String generateJti() {
        return new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
    }
}
