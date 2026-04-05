/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.demo.service;

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

/** HttpTlsClient implementation. */
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

    private static final int TOKEN_LOG_PREFIX_LENGTH = 20;

    private final SSLContext sslContext;

    public HttpTlsClient(String certPath, String keyPath,
                         String trustStorePath, String trustStorePassword)
            throws SSLContextCreationException {
        this.sslContext = SSLContextFactory.create(certPath, keyPath, trustStorePath, trustStorePassword);
    }

    /**
     * Executes the postJwt operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param body            The body parameter
     * @throws IOException    When an error occurs during the operation
     */
    public String postJwt(String url, String body) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_FORM_URLENCODED)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .withBody(body)
                .execute();
    }

    /**
     * Executes the postAccessToken operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param body            The body parameter
     * @throws IOException    When an error occurs during the operation
     */
    public String postAccessToken(String url, String body) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_FORM_URLENCODED)
                .addHeader("Cache-Control", "no-cache")
                .withBody(body)
                .execute();
    }

    /**
     * Executes the postConsentInit operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param body            The body parameter
     * @param token           The token parameter
     * @throws IOException    When an error occurs during the operation
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

    public String postConsentAuthRequest(String requestObjectJwt, String clientId, String scope)
            throws IOException {
        String authUrl = AuthUrlBuilder.build(requestObjectJwt, clientId, scope);
        System.out.println(authUrl);
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//        return HttpConnection.get(authUrl, sslContext)
//                .followRedirects(false)
//                .executeAndGetRedirect();
        return authUrl;
    }

    /**
     * Executes the getWithAuth operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param token           The token parameter
     * @throws IOException    When an error occurs during the operation
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
     * Executes the postPaymentConsentInit operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param body            The body parameter
     * @param token           The token parameter
     * @throws IOException    When an error occurs during the operation
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
     * Executes the postPayments operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param body            The body parameter
     * @param token           The token parameter
     * @throws IOException    When an error occurs during the operation
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
     * Executes the deleteWithAuth operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param token           The token parameter
     * @throws IOException    When an error occurs during the operation
     */
    public boolean deleteWithAuth(String url, String token) throws IOException {
        String fapiId = ConfigLoader.getFapiFinancialId();
        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [AISP] CONSENT REVOCATION OUTBOUND REQUEST ==========\n" +
                            "  Method : DELETE\n" +
                            "  URL    : {}\n" +
                            "  Headers:\n" +
                            "    {}: {}\n" +
                            "    {}: Bearer {}***\n" +
                            "    {}: {}\n" +
                            "=================================================================",
                    url,
                    HEADER_FAPI_ID,       fapiId,
                    HEADER_AUTHORIZATION, truncateToken(token),
                    HEADER_ACCEPT,        MEDIA_JSON
            );
        }

        int statusCode = HttpConnection.delete(url, sslContext)
                .addHeader(HEADER_FAPI_ID, fapiId)
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .executeAndGetStatus();

        if (logger.isInfoEnabled()) {
            logger.info(
                    "\n========== [AISP] CONSENT REVOCATION INBOUND RESPONSE ==========\n" +
                            "  Method : DELETE\n" +
                            "  URL    : {}\n" +
                            "  Status : {}\n" +
                            "=================================================================",
                    url, statusCode
            );
        }

        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Executes the truncateToken operation and modify the payload if necessary.
     *
     * @param token           The token parameter
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
