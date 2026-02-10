package com.wso2.openbanking.http;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpConnection {

    private final String url;
    private final SSLContext sslContext;
    private final String method;
    private final Map<String, String> headers;
    private String body;
    private boolean followRedirects = true;

    private HttpConnection(String url, SSLContext sslContext, String method) {
        this.url = url;
        this.sslContext = sslContext;
        this.method = method;
        this.headers = new HashMap<>();
    }

    public static HttpConnection post(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "POST");
    }

    public static HttpConnection get(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "GET");
    }

    public HttpConnection addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpConnection withBody(String body) {
        this.body = body;
        return this;
    }

    public HttpConnection followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    public String execute() throws IOException {
        HttpsURLConnection connection = createConnection();

        if (body != null) {
            writeBody(connection);
        }

        return readResponse(connection);
    }

    public String executeAndGetRedirect() throws IOException {
        HttpsURLConnection connection = createConnection();

        int responseCode = connection.getResponseCode();

        if (responseCode >= 300 && responseCode <= 399) {
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl != null) {
                return redirectUrl;
            }
        }

        throw new IOException("Failed to get redirect. HTTP Status: " + responseCode);
    }

    private HttpsURLConnection createConnection() throws IOException {
        URL urlObj = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        connection.setRequestMethod(method);
        connection.setInstanceFollowRedirects(followRedirects);
        connection.setDoInput(true);

        if (body != null || "POST".equals(method)) {
            connection.setDoOutput(true);
        }

        // Set headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        return connection;
    }

    private void writeBody(HttpsURLConnection connection) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readResponse(HttpsURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
