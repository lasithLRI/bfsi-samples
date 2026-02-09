package com.wso2.openbanking.services;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.apache.cxf.common.util.UrlUtils.urlEncode;

public class HttpTlsClient {
    private final SSLContext sslContext;

    public HttpTlsClient(String certPath, String keyPath, // certPath and keyPath are now ignored
                         String trustStorePath, String trustStorePassword) throws Exception {

        KeyManager[] keyManagers = null;

        TrustManager[] trustManagers = null;

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate clientCert;
        try (InputStream certInput = HttpTlsClient.class.getResourceAsStream(certPath);) {
            clientCert = (X509Certificate) cf.generateCertificate(certInput);
        }

        // Load client private key using KeyReader
        PrivateKey privateKey = KeyReader.loadPrivateKeyFromStream(HttpTlsClient.class.getResourceAsStream(keyPath));

        // Create a KeyStore containing client certificate + private key
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null); // initialize empty
        keyStore.setKeyEntry("client", privateKey, new char[0], new java.security.cert.Certificate[]{clientCert});

        // Create KeyManagerFactory from KeyStore
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, new char[0]);



        if (trustStorePath != null) {

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = trustStorePassword != null ? trustStorePassword.toCharArray() : null;

            try (InputStream is = HttpTlsClient.class.getResourceAsStream(trustStorePath)) {
                if (is == null) {
                    throw new FileNotFoundException("Trust store not found at: " + trustStorePath);
                }
                trustStore.load(is, password);
            }


            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            trustManagers = tmf.getTrustManagers();

        }



        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), trustManagers, null);
    }

    public String postJwt(String path, String body) throws Exception {
        URL url = new URL(path);

//        String aa = "grant_type=client_credentials" +
//                "&scope=accounts openid" +
//                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
//                "&client_id=eW5UtNdueq7hwrH95I6XMEUG91sa" +
//                "&client_assertion=eyJhbGciOiJQUzI1NiIsImtpZCI6InNDZWtOZ1NXSWF1UTM0a2xSaERHcWZ3cGpjNCIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJlVzVVdE5kdWVxN2h3ckg5NUk2WE1FVUc5MXNhIiwic3ViIjoiZVc1VXROZHVlcTdod3JIOTVJNlhNRVVHOTFzYSIsImV4cCI6MTc2NTMxNTMxNywiaWF0IjoxNzY1MjkzNzgzLCJqdGkiOiIxNzQ5MDEyMjU0NTM5ODMzIiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0Ni9vYXV0aDIvdG9rZW4ifQ.IDwsOtE3oaCup8HTpShYTMpf58k549T7fUh-WXnyLsbwVGA2a0Fnb_ld-WKXIh12nK1BUmQ1JlpJx0t47R-elMBvWt0ni9BLw4f5HCjV-esm-0E3xvNRMlqO2hZPyipFvZ77h5iwJOkuvgwjoD_XYZy9nCc4VmBEDxogjd6fv4YRMF7ExPiOHq6WNznyM5Ud2N5UsLgzQWnmDMcjjgLQh8q8PJI0GZgDBp433EeTf0dq3UuyJ_Wt-aM1JXBFnsL4wzEUSM6Q_97-lDNwwop-f7pvCGCFhQ3FveYqS3C2DgA6aIXdXi1lh7J-k3-xxGHdqipIhYUVPTE1LFdD5rYzkg" +
//                "&redirect_uri=https://www.google.com/redirects/redirect1";

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }

    }

    public String postConsentInit(String path, String body, String token) throws Exception {
        URL url = new URL(path);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("x-fapi-financial-id", "open-bank");
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();
        System.out.println(is);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    public String postConsentAuthRequest(String requestObjectJwt, String clientId,String scope) throws IOException {

        String encodedResponseType = urlEncode("code id_token");
        String encodedScope = urlEncode(scope);
        String encodedRedirectUri = urlEncode("https://tpp.local.ob/ob_demo_backend_war/init/redirected");
        String encodedState = urlEncode("YWlzcDozMTQ2");
        String encodedRequestObject = urlEncode(requestObjectJwt);
        System.out.println(requestObjectJwt);
        String encodedPrompt = urlEncode("login");
        String encodedNonce = urlEncode("nonce");


        StringBuilder urlBuilder = new StringBuilder("https://localhost:9446/oauth2/authorize");
        urlBuilder.append("?response_type=").append(encodedResponseType);
        urlBuilder.append("&client_id=").append(clientId); // Note: client_id is NOT URL-encoded here if it's alphanumeric
        urlBuilder.append("&scope=").append(encodedScope);
        urlBuilder.append("&redirect_uri=").append(encodedRedirectUri);
        urlBuilder.append("&state=").append(encodedState);
        urlBuilder.append("&request=").append(encodedRequestObject); // The signed JWT
        urlBuilder.append("&prompt=").append(encodedPrompt);
        urlBuilder.append("&nonce=").append(encodedNonce);

        HttpsURLConnection connection = (HttpsURLConnection) new URL(urlBuilder.toString()).openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);

        int responseCode = connection.getResponseCode();

        if (responseCode >= 300 && responseCode <= 399) {
            String redirectUrl = connection.getHeaderField("Location");

            return redirectUrl;

        } else {
            throw new IOException("Failed to initiate authorization flow. HTTP Status: " + responseCode);
        }




//        int responseCode = connection.getResponseCode();
//
//
//        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();
//
//        System.out.println(is);
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
//            URL redirect = new URL(sb.toString());
//            return redirect.toString();
//        }
    }


    public String postAccesstoken(String path,String body) throws IOException {
        URL url = new URL(path);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    public String getAccountsRequest(String path,String token) throws IOException {
        URL url = new URL(path);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");

        connection.setRequestProperty("x-fapi-financial-id", "open-bank");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    public String getAccountFromId(String path,String token) throws IOException {
        URL url = new URL(path);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");

        connection.setRequestProperty("x-fapi-financial-id", "open-bank");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    public String postPaymentConsentInit(String path, String body, String token) throws Exception {
        URL url = new URL(path);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("x-fapi-financial-id", "open-bank");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("x-idempotency-key","709909");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    public String postPayments(String path, String body, String token) throws Exception {
        URL url = new URL(path);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("x-fapi-financial-id", "open-bank");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//        connection.setRequestProperty("x-idempotency-key","249667");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >=200 && responseCode < 300) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }


}
