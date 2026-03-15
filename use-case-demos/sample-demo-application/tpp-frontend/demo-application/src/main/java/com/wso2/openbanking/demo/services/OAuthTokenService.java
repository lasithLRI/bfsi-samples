package com.wso2.openbanking.demo.services;

import com.wso2.openbanking.demo.exceptions.AuthorizationException;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import com.wso2.openbanking.demo.utils.JwtUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Handles OAuth token acquisition and consent initialization for both
 * account and payment flows. Builds and signs JWT client assertions
 * for client_credentials and authorization_code grant requests.
 */
public final class OAuthTokenService {

    private final HttpTlsClient client;
    private final JwtTokenService jwtTokenService;

    /**
     * Initialises the service and acquires the shared JwtTokenService singleton.
     *
     * @param client the mTLS HTTP client used for all outbound token and consent requests.
     * @throws GeneralSecurityException if the JwtTokenService cannot load the signing key.
     * @throws IOException if the signing key file cannot be read.
     */
    @SuppressWarnings("EI_EXPOSE_REP2")
// client is a shared service dependency — defensive copying is not appropriate.
    public OAuthTokenService(HttpTlsClient client)
            throws GeneralSecurityException, IOException {
        this.client = client;
        this.jwtTokenService = JwtTokenService.getInstance();
    }

    /**
     * Obtains a client_credentials access token for the given scope.
     * A signed client assertion JWT is built and posted to the token endpoint,
     * and the raw JSON token response is returned to the caller.
     *
     * @param scope the OAuth scope to request (e.g. "accounts" or "payments").
     * @return the raw JSON token response string containing the access token.
     * @throws AuthorizationException if the token endpoint cannot be reached or signing fails.
     */
    public String getToken(String scope) throws AuthorizationException {
        try {
            String clientAssertion = jwtTokenService.createClientAssertion(JwtUtils.generateJti());
            String body = buildTokenRequestBody(scope, clientAssertion);
            return client.postJwt(ConfigLoader.getTokenUrl(), body);
        } catch (IOException e) {
            throw new AuthorizationException("Failed to contact token endpoint", e);
        } catch (GeneralSecurityException e) {
            throw new AuthorizationException("Failed to sign client assertion", e);
        }
    }

    /**
     * Posts an account consent initiation request and returns the raw consent response JSON.
     * The access token is extracted from the token response before the request is sent.
     * No idempotency key is required for account consent initiation.
     *
     * @param token the raw JSON token response string containing the access token.
     * @param consentBody the JSON consent request body specifying permissions and date range.
     * @param url the account-access-consents endpoint URL.
     * @return the raw JSON consent response containing the ConsentId.
     * @throws IOException if the HTTP call fails.
     * @throws JSONException if the token response cannot be parsed.
     */
    public String initializeConsent(String token, String consentBody, String url)
            throws IOException, JSONException {
        String accessToken = new JSONObject(token).getString("access_token");
        return client.postConsentInit(url, consentBody, accessToken);
    }

    /**
     * Posts a payment consent initiation request and returns the raw consent response JSON.
     * The access token is extracted from the token response before the request is sent.
     * The underlying HTTP call includes the required x-idempotency-key header.
     *
     * @param token the raw JSON token response string containing the access token.
     * @param consentBody the JSON payment consent request body.
     * @param url the payment-consents endpoint URL.
     * @return the raw JSON consent response containing the ConsentId.
     * @throws IOException if the HTTP call fails.
     * @throws JSONException if the token response cannot be parsed.
     */
    public String initializePaymentConsent(String token, String consentBody, String url)
            throws IOException, JSONException {
        String accessToken = new JSONObject(token).getString("access_token");
        return client.postPaymentConsentInit(url, consentBody, accessToken);
    }

    /**
     * Extracts the consent ID from the consent response, builds a signed request object,
     * and sends the authorization request, returning the redirect URL for user consent.
     *
     * @param consentResponse the raw JSON consent response returned by the bank.
     * @param scope the OAuth scope associated with the consent (e.g. "accounts openid").
     * @return the redirect URL the user must visit to approve the consent.
     * @throws GeneralSecurityException if the request object JWT cannot be signed.
     * @throws IOException if the authorization request fails.
     */
    public String authorizeConsent(String consentResponse, String scope)
            throws GeneralSecurityException, IOException {
        String consentId = extractConsentId(consentResponse);
        String requestObject = jwtTokenService.createRequestObject(consentId);
        return client.postConsentAuthRequest(requestObject, ConfigLoader.getClientId(), scope);
    }

    /**
     * Parses the ConsentId from the consent initiation response.
     *
     * @param consentResponse the raw JSON consent response returned by the bank.
     * @return the ConsentId string extracted from the Data object.
     */
    private String extractConsentId(String consentResponse) {
        return new JSONObject(consentResponse)
                .getJSONObject("Data")
                .getString("ConsentId");
    }

    /**
     * Builds the URL-encoded body for a client_credentials token request.
     *
     * @param scope the OAuth scope to request.
     * @param clientAssertion the signed JWT client assertion string.
     * @return a URL-encoded form body string ready for posting to the token endpoint.
     */
    private String buildTokenRequestBody(String scope, String clientAssertion) {
        return "grant_type=client_credentials" +
                "&scope=" + scope +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + ConfigLoader.getClientId() +
                "&client_assertion=" + clientAssertion +
                "&redirect_uri=" + ConfigLoader.getRedirectUri();
    }
}
