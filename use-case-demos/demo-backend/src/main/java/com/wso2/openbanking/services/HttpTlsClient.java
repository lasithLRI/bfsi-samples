package com.wso2.openbanking.services;

import com.wso2.openbanking.ConfigLoader;
import com.wso2.openbanking.http.HttpConnection;
import com.wso2.openbanking.http.SSLContextFactory;
import com.wso2.openbanking.http.AuthUrlBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public class HttpTlsClient {

    private final SSLContext sslContext;

    public HttpTlsClient(String certPath, String keyPath, String trustStorePath, String trustStorePassword)
            throws Exception {
        this.sslContext = SSLContextFactory.create(certPath, keyPath, trustStorePath, trustStorePassword);
    }

    // OAuth Token Methods
    public String postJwt(String url, String body) throws Exception {
        return HttpConnection.post(url, sslContext)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .withBody(body)
                .execute();
    }

    public String postAccesstoken(String url, String body) throws IOException {
        return HttpConnection.post(url, sslContext)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cache-Control", "no-cache")
                .withBody(body)
                .execute();
    }


    public String postConsentInit(String url, String body, String token) throws Exception {
        return HttpConnection.post(url, sslContext)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("x-fapi-financial-id", ConfigLoader.getFapiFinancialId())
                .addHeader("Content-Type", "application/json")
                .withBody(body)
                .execute();
    }

//    public String postConsentInit(String url, String body, String token) throws Exception {
//        System.out.println("==================== POST CONSENT INIT DEBUG ====================");
//        System.out.println("URL: " + url);
//        System.out.println("Request Body: " + body);
//        System.out.println("Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
//        System.out.println("FAPI Financial ID: " + ConfigLoader.getFapiFinancialId());
//        System.out.println("================================================================");
//
//        try {
//            String response = HttpConnection.post(url, sslContext)
//                    .addHeader("Authorization", "Bearer " + token)
//                    .addHeader("x-fapi-financial-id", ConfigLoader.getFapiFinancialId())
//                    .addHeader("Content-Type", "application/json")
//                    .withBody(body)
//                    .execute();
//
//            System.out.println("==================== POST CONSENT RESPONSE ====================");
//            System.out.println("Response: " + response);
//            System.out.println("================================================================");
//
//            return response;
//        } catch (Exception e) {
//            System.err.println("==================== POST CONSENT ERROR ====================");
//            System.err.println("Error Message: " + e.getMessage());
//            e.printStackTrace();
//            System.err.println("============================================================");
//            throw e;
//        }
//    }

    public String postConsentAuthRequest(String requestObjectJwt, String clientId, String scope)
            throws IOException {
        String authUrl = AuthUrlBuilder.build(requestObjectJwt, clientId, scope);

        return HttpConnection.get(authUrl, sslContext)
                .followRedirects(false)
                .executeAndGetRedirect();
    }

    // Account Methods
    public String getAccountsRequest(String url, String token) throws IOException {
        return HttpConnection.get(url, sslContext)
                .addHeader("x-fapi-financial-id", ConfigLoader.getFapiFinancialId())
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .execute();
    }

    public String getAccountFromId(String url, String token) throws IOException {
        return HttpConnection.get(url, sslContext)
                .addHeader("x-fapi-financial-id", ConfigLoader.getFapiFinancialId())
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .execute();
    }

    // Payment Methods
    public String postPaymentConsentInit(String url, String body, String token) throws Exception {
        return HttpConnection.post(url, sslContext)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("x-fapi-financial-id", ConfigLoader.getFapiFinancialId())
                .addHeader("Content-Type", "application/json")
                .addHeader("x-idempotency-key", ConfigLoader.getPaymentIdempotencyKey())
                .withBody(body)
                .execute();
    }

    public String postPayments(String url, String body, String token) throws Exception {
        return HttpConnection.post(url, sslContext)
                .addHeader("x-fapi-financial-id", ConfigLoader.getFapiFinancialId())
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .withBody(body)
                .execute();
    }
}
