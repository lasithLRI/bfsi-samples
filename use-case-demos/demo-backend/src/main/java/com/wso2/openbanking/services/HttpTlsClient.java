package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.exception.SSLContextCreationException;
import com.wso2.openbanking.http.AuthUrlBuilder;
import com.wso2.openbanking.http.HttpConnection;
import com.wso2.openbanking.http.SSLContextFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;

public class HttpTlsClient {

    private static final String HEADER_CONTENT_TYPE   = "Content-Type";
    private static final String HEADER_ACCEPT         = "Accept";
    private static final String HEADER_AUTHORIZATION  = "Authorization";
    private static final String HEADER_FAPI_ID        = "x-fapi-financial-id";

    private static final String MEDIA_JSON            = "application/json";
    private static final String MEDIA_JSON_UTF8       = "application/json; charset=UTF-8";
    private static final String MEDIA_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private static final String BEARER_PREFIX         = "Bearer ";

    private final SSLContext sslContext;

    public HttpTlsClient(String certPath, String keyPath, String trustStorePath, String trustStorePassword)
            throws SSLContextCreationException {
        this.sslContext = SSLContextFactory.create(certPath, keyPath, trustStorePath, trustStorePassword);
    }

    public String postJwt(String url, String body) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_FORM_URLENCODED)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .withBody(body)
                .execute();
    }

    public String postAccesstoken(String url, String body) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_FORM_URLENCODED)
                .addHeader("Cache-Control", "no-cache")
                .withBody(body)
                .execute();
    }

    public String postConsentInit(String url, String body, String token) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_FAPI_ID, ConfigLoader.getFapiFinancialId())
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                .withBody(body)
                .execute();
    }

    public String postConsentAuthRequest(String requestObjectJwt, String clientId, String scope)
            throws IOException {
        String authUrl = AuthUrlBuilder.build(requestObjectJwt, clientId, scope);
        return HttpConnection.get(authUrl, sslContext)
                .followRedirects(false)
                .executeAndGetRedirect();
    }

    public String getWithAuth(String url, String token) throws IOException {
        return HttpConnection.get(url, sslContext)
                .addHeader(HEADER_FAPI_ID, ConfigLoader.getFapiFinancialId())
                .addHeader(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                .addHeader(HEADER_ACCEPT, MEDIA_JSON)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_JSON_UTF8)
                .execute();
    }
}
