package com.wso2.openbanking.demo.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


/**
 * Fluent builder for outbound HTTPS requests made over a mutual TLS SSLContext.
 *
 * Supports GET, POST, and DELETE methods. Use the static factory methods to begin
 * building a request, chain header and body setters, then call one of the terminal
 * methods to execute: execute returns the response body as a String,
 * executeAndGetRedirect returns the Location header from a 3xx response, and
 * executeAndGetStatus returns the raw HTTP status code.
 *
 * Error responses (4xx, 5xx) are read from the error stream so the caller always
 * receives the full response body regardless of status code.
 */
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
        this.body = null;
    }

    /**
     * Starts building a POST request to the given URL.
     *
     * @param url        the target URL.
     * @param sslContext the mTLS context to use for the connection.
     * @return a new HttpConnection configured for POST.
     */
    public static HttpConnection post(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "POST");
    }

    /**
     * Starts building a GET request to the given URL.
     *
     * @param url        the target URL.
     * @param sslContext the mTLS context to use for the connection.
     * @return a new HttpConnection configured for GET.
     */
    public static HttpConnection get(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "GET");
    }

    /**
     * Starts building a DELETE request to the given URL.
     * Used for consent revocation — the response body is typically empty (HTTP 204).
     *
     * @param url        the target URL.
     * @param sslContext the mTLS context to use for the connection.
     * @return a new HttpConnection configured for DELETE.
     */
    public static HttpConnection delete(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "DELETE");
    }

    /**
     * Adds a request header. Can be chained multiple times.
     *
     * @param key   the header name.
     * @param value the header value.
     * @return this instance for chaining.
     */
    public HttpConnection addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Sets the request body. Implicitly enables output on the connection.
     *
     * @param body the raw request body string (typically JSON or form-encoded).
     * @return this instance for chaining.
     */
    public HttpConnection withBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Controls whether HTTP redirects are automatically followed.
     * Set to false when the redirect URL itself is the desired result,
     * for example during the consent authorization redirect.
     *
     * @param follow true to follow redirects (default); false to stop at the 3xx response.
     * @return this instance for chaining.
     */
    public HttpConnection followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    /**
     * Executes the request and returns the full response body as a String.
     * Both success (2xx) and error (4xx/5xx) bodies are returned — the caller
     * is responsible for interpreting the content.
     *
     * @return the response body string.
     * @throws IOException if the connection or I/O fails.
     */
    public String execute() throws IOException {
        HttpsURLConnection connection = createConnection();
        if (body != null) {
            writeBody(connection);
        }
        return readResponse(connection);
    }

    /**
     * Executes the request and returns the Location header from a 3xx redirect response.
     * Intended for use with the consent authorization endpoint, which redirects to the
     * identity server's login page.
     *
     * @return the redirect URL from the Location header.
     * @throws IOException if the response is not a redirect or the Location header is absent.
     */
    public String executeAndGetRedirect() throws IOException {
        HttpsURLConnection connection = createConnection();
        int responseCode = connection.getResponseCode();
        if (responseCode >= 300 && responseCode <= 399) {
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl != null) {
                return redirectUrl;
            }
        }
        throw new IOException("Expected a redirect response but got HTTP " + responseCode);
    }

    /**
     * Executes the request and returns only the HTTP status code.
     * Intended for DELETE operations such as consent revocation where the response
     * body is empty and only the status matters.
     *
     * @return the HTTP status code (e.g. 204 for successful deletion).
     * @throws IOException if the connection or I/O fails.
     */
    public int executeAndGetStatus() throws IOException {
        HttpsURLConnection connection = createConnection();
        if (body != null) {
            writeBody(connection);
        }
        int status = connection.getResponseCode();
        if (status >= 400) {
            try {
                System.out.println("========== HTTP ERROR RESPONSE BODY ==========");
                System.out.println(readResponse(connection));
                System.out.println("==============================================");
            } catch (Exception e) {
                System.out.println("Failed to read error body: " + e.getMessage());
            }
        }
        return status;
    }

    /**
     * Opens and configures the underlying HttpsURLConnection from the current builder state.
     * Applies the SSLSocketFactory, request method, redirect policy, and all headers.
     *
     * @return a configured HttpsURLConnection ready for use.
     * @throws IOException if the URL is malformed or the connection cannot be opened.
     */
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
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
        return connection;
    }

    /**
     * Writes the request body to the connection's output stream using UTF-8 encoding.
     *
     * @param connection the open HttpsURLConnection to write the body to.
     * @throws IOException if the output stream cannot be written to.
     */
    private void writeBody(HttpsURLConnection connection) throws IOException {
        if (body == null) {
            return;
        }
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    /**
     * Reads the full response body from either the input stream (2xx) or the error
     * stream (4xx/5xx), so the caller always receives the complete server response.
     *
     * @param connection the open HttpsURLConnection to read the response from.
     * @return the full response body as a String.
     * @throws IOException if the response stream cannot be read.
     */
    private String readResponse(HttpsURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
