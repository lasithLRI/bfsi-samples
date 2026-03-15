package com.wso2.openbanking.demo.services;


import com.wso2.openbanking.demo.exceptions.SSLContextCreationException;
import com.wso2.openbanking.demo.http.AuthUrlBuilder;
import com.wso2.openbanking.demo.http.HttpConnection;
import com.wso2.openbanking.demo.http.SSLContextFactory;
import com.wso2.openbanking.demo.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

import javax.net.ssl.SSLContext;

/**
 * HTTP client for all outbound Open Banking API calls made over mutual TLS (mTLS).
 *
 * Each method targets a specific Open Banking endpoint type and applies the correct
 * headers and authentication for that call. The underlying SSLContext is initialized
 * once at construction time from the provided certificate, key, and truststore material
 * and reused across all requests.
 *
 * All request headers, request bodies, and response bodies are logged at INFO level
 * via SLF4J. Bearer tokens are truncated to the first 20 characters in log output to
 * prevent accidental credential leakage.
 *
 * Supported operations:
 * JWT form post for client assertion token requests,
 * access token exchange for authorization code grant,
 * account consent initiation (AISP consent POST),
 * consent authorization redirect (authorization endpoint GET),
 * authenticated resource GET for accounts, balances, and transactions,
 * payment consent initiation (PISP consent POST with idempotency key),
 * payment submission (PISP payment POST with idempotency key),
 * and consent revocation (DELETE for account access consent).
 */
public final class HttpTlsClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpTlsClient.class);

    private static final String HEADER_CONTENT_TYPE  = "Content-Type";
    private static final String HEADER_ACCEPT        = "Accept";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_FAPI_ID       = "x-fapi-financial-id";
    private static final String HEADER_IDEMPOTENCY   = "x-idempotency-key";

    private static final String MEDIA_JSON            = "application/json";
    private static final String MEDIA_JSON_UTF8       = "application/json; charset=UTF-8";
    private static final String MEDIA_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private static final String BEARER_PREFIX = "Bearer ";

    /** Number of characters of the Bearer token shown in logs before truncation. */
    private static final int TOKEN_LOG_PREFIX_LENGTH = 20;

    private final SSLContext sslContext;

    /**
     * Creates a new HttpTlsClient and initializes the mTLS SSLContext.
     *
     * @param certPath           path to the client certificate (PEM or PKCS12).
     * @param keyPath            path to the client private key.
     * @param trustStorePath     path to the truststore containing the server CA.
     * @param trustStorePassword password for the truststore.
     * @throws SSLContextCreationException if the SSL context cannot be initialized.
     */
    public HttpTlsClient(String certPath, String keyPath,
                         String trustStorePath, String trustStorePassword)
            throws SSLContextCreationException {
        this.sslContext = SSLContextFactory.create(certPath, keyPath, trustStorePath, trustStorePassword);
    }

    /**
     * Posts a signed JWT client assertion as a form-encoded body to the token endpoint.
     * Used for client_credentials grant requests where the client authenticates
     * using a private key JWT.
     *
     * @param url  the token endpoint URL.
     * @param body URL-encoded form body containing the JWT and grant parameters.
     * @return the raw JSON token response string.
     * @throws IOException if the HTTP call fails.
     */
    public String postJwt(String url, String body) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_FORM_URLENCODED)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .withBody(body)
                .execute();
    }

    /**
     * Posts an authorization code exchange request to the token endpoint.
     * Used during the OAuth callback to swap an authorization code for an access token.
     *
     * @param url  the token endpoint URL.
     * @param body URL-encoded form body containing the authorization code and grant parameters.
     * @return the raw JSON token response string.
     * @throws IOException if the HTTP call fails.
     */
    public String postAccessToken(String url, String body) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_FORM_URLENCODED)
                .addHeader("Cache-Control", "no-cache")
                .withBody(body)
                .execute();
    }

    /**
     * Posts an account access consent initiation request to the AISP consent endpoint.
     * The consent body specifies the permissions and date range being requested.
     * No idempotency key is required for account consent initiation.
     * Logs the outbound request and inbound response at INFO level.
     *
     * @param url   the account-access-consents endpoint URL.
     * @param body  JSON consent request body.
     * @param token Bearer access token with accounts scope.
     * @return the raw JSON consent response containing the ConsentId.
     * @throws IOException if the HTTP call fails.
     */
    public String postConsentInit(String url, String body, String token) throws IOException {
        String fapiId = ConfigLoader.getFapiFinancialId();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [AISP] CONSENT INITIATION OUTBOUND REQUEST ==========\n" +
                            "  Method : POST\n" +
                            "  URL    : {}\n" +
                            "  Headers:\n" +
                            "    {}: Bearer {}***\n" +
                            "    {}: {}\n" +
                            "    {}: {}\n" +
                            "  Body   :\n{}\n" +
                            "=================================================================",
                    url,
                    HEADER_AUTHORIZATION, truncateToken(token),
                    HEADER_FAPI_ID,       fapiId,
                    HEADER_CONTENT_TYPE,  MEDIA_JSON,
                    body
            );
        }

        String response = HttpConnection.post(url, sslContext)
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_FAPI_ID, fapiId)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                .withBody(body)
                .execute();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [AISP] CONSENT INITIATION INBOUND RESPONSE ==========\n" +
                            "  Method : POST\n" +
                            "  URL    : {}\n" +
                            "  Body   :\n{}\n" +
                            "=================================================================",
                    url, response
            );
        }

        return response;
    }

    /**
     * Sends the signed consent authorization request to the authorization endpoint.
     * Redirects are intentionally not followed — the Location header from the 302 response
     * is returned directly so the caller can pass the authorization URL back to the user.
     *
     * @param requestObjectJwt signed JWT request object containing the consent ID and claims.
     * @param clientId         the OAuth client ID of this application.
     * @param scope            the requested OAuth scope (e.g. "accounts openid").
     * @return the redirect URL the user must visit to approve the consent.
     * @throws IOException if the HTTP call fails.
     */
    public String postConsentAuthRequest(String requestObjectJwt, String clientId, String scope)
            throws IOException {
        String authUrl = AuthUrlBuilder.build(requestObjectJwt, clientId, scope);
        return HttpConnection.get(authUrl, sslContext)
                .followRedirects(false)
                .executeAndGetRedirect();
    }

    /**
     * Performs an authenticated GET request to an Open Banking AISP resource endpoint.
     * Used to fetch accounts, balances, and transactions after consent has been granted.
     * Logs the outbound request headers (token truncated) and the full inbound response body.
     *
     * @param url   the fully-qualified resource URL (accounts, balances, transactions).
     * @param token Bearer access token with accounts scope.
     * @return the raw JSON response body.
     * @throws IOException if the HTTP call fails.
     */
    public String getWithAuth(String url, String token) throws IOException {
        String fapiId = ConfigLoader.getFapiFinancialId();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [AISP] OUTBOUND REQUEST ==========\n" +
                            "  Method : GET\n" +
                            "  URL    : {}\n" +
                            "  Headers:\n" +
                            "    {}: {}\n" +
                            "    {}: Bearer {}***\n" +
                            "    {}: {}\n" +
                            "    {}: {}\n" +
                            "==============================================",
                    url,
                    HEADER_FAPI_ID,       fapiId,
                    HEADER_AUTHORIZATION, truncateToken(token),
                    HEADER_ACCEPT,        MEDIA_JSON,
                    HEADER_CONTENT_TYPE,  MEDIA_JSON_UTF8
            );
        }

        String response = HttpConnection.get(url, sslContext)
                .addHeader(HEADER_FAPI_ID, fapiId)
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_JSON_UTF8)
                .execute();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [AISP] INBOUND RESPONSE ==========\n" +
                            "  Method : GET\n" +
                            "  URL    : {}\n" +
                            "  Body   :\n{}\n" +
                            "==============================================",
                    url, response
            );
        }

        return response;
    }

    /**
     * Posts a payment consent initiation request to the PISP consent endpoint.
     * A fresh x-idempotency-key (UUID) is generated for every call, satisfying the Open Banking
     * requirement that each consent initiation is uniquely identified. The key is included
     * in the log output so it can be correlated with the bank's records if a dispute arises.
     * Logs the outbound request headers, idempotency key, and full request body,
     * as well as the full inbound response body.
     *
     * @param url   the payment-consents endpoint URL.
     * @param body  JSON payment consent request body.
     * @param token Bearer access token with payments scope.
     * @return the raw JSON consent response containing the ConsentId.
     * @throws IOException if the HTTP call fails.
     */
    public String postPaymentConsentInit(String url, String body, String token) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        String fapiId = ConfigLoader.getFapiFinancialId();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [PISP] OUTBOUND REQUEST ==========\n" +
                            "  Method : POST\n" +
                            "  URL    : {}\n" +
                            "  Headers:\n" +
                            "    {}: Bearer {}***\n" +
                            "    {}: {}\n" +
                            "    {}: {}\n" +
                            "    {}: {}\n" +
                            "  Body   :\n{}\n" +
                            "==============================================",
                    url,
                    HEADER_AUTHORIZATION, truncateToken(token),
                    HEADER_FAPI_ID,       fapiId,
                    HEADER_CONTENT_TYPE,  MEDIA_JSON,
                    HEADER_IDEMPOTENCY,   idempotencyKey,
                    body
            );
        }

        String response = HttpConnection.post(url, sslContext)
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_FAPI_ID, fapiId)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                .addHeader(HEADER_IDEMPOTENCY, idempotencyKey)
                .withBody(body)
                .execute();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [PISP] INBOUND RESPONSE ==========\n" +
                            "  Method : POST\n" +
                            "  URL    : {}\n" +
                            "  Body   :\n{}\n" +
                            "==============================================",
                    url, response
            );
        }

        return response;
    }

    /**
     * Posts a payment submission request to the PISP payments endpoint.
     * Called after the user has approved the payment consent via the authorization flow.
     * A fresh x-idempotency-key is generated and included, as required by the Open Banking
     * specification for all PISP POST endpoints.
     * Logs the outbound request headers, idempotency key, and full request body,
     * as well as the full inbound response body.
     *
     * @param url   the payments endpoint URL.
     * @param body  JSON payment submission body.
     * @param token Bearer user access token with payments scope, obtained from the
     *              authorization code exchange after user consent.
     * @return the raw JSON payment submission response.
     * @throws IOException if the HTTP call fails.
     */
    public String postPayments(String url, String body, String token) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        String fapiId = ConfigLoader.getFapiFinancialId();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [PISP] PAYMENT SUBMISSION OUTBOUND REQUEST ==========\n" +
                            "  Method : POST\n" +
                            "  URL    : {}\n" +
                            "  Headers:\n" +
                            "    {}: Bearer {}***\n" +
                            "    {}: {}\n" +
                            "    {}: {}\n" +
                            "    {}: {}\n" +
                            "    {}: {}\n" +
                            "  Body   :\n{}\n" +
                            "=================================================================",
                    url,
                    HEADER_AUTHORIZATION, truncateToken(token),
                    HEADER_FAPI_ID,       fapiId,
                    HEADER_ACCEPT,        MEDIA_JSON,
                    HEADER_CONTENT_TYPE,  MEDIA_JSON_UTF8,
                    HEADER_IDEMPOTENCY,   idempotencyKey,
                    body
            );
        }

        String response = HttpConnection.post(url, sslContext)
                .addHeader(HEADER_FAPI_ID, fapiId)
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_JSON_UTF8)
                .addHeader(HEADER_IDEMPOTENCY, idempotencyKey)
                .withBody(body)
                .execute();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [PISP] PAYMENT SUBMISSION INBOUND RESPONSE ==========\n" +
                            "  Method : POST\n" +
                            "  URL    : {}\n" +
                            "  Body   :\n{}\n" +
                            "=================================================================",
                    url, response
            );
        }

        return response;
    }

    /**
     * Sends an authenticated DELETE request to revoke an account access consent.
     * The Open Banking specification mandates HTTP 204 (No Content) for a successful
     * revocation. This method returns true for any 2xx status code to handle servers
     * that return 200 OK instead.
     *
     * @param url   the fully-qualified consent revocation URL
     *              (e.g. /account-access-consents/{consentId}).
     * @param token Bearer access token with accounts scope.
     * @return true if the bank responded with a 2xx status, false otherwise.
     * @throws IOException if the HTTP call fails.
     */
    public boolean deleteWithAuth(String url, String token) throws IOException {
        int statusCode = HttpConnection.delete(url, sslContext)
                .addHeader(HEADER_FAPI_ID, ConfigLoader.getFapiFinancialId())
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .executeAndGetStatus();
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Returns the first 20 characters of a Bearer token for safe inclusion in log output.
     * Prevents full credentials from appearing in logs.
     *
     * @param token the raw Bearer token value.
     * @return truncated token string, or the original if shorter than 20 characters.
     */
    private String truncateToken(String token) {
        if (token == null) {
            return "null";
        }
        return token.length() > TOKEN_LOG_PREFIX_LENGTH
                ? token.substring(0, TOKEN_LOG_PREFIX_LENGTH)
                : token;
    }


}
