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

/** HttpConnection implementation. */
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
     * Executes the post operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param sslContext      The sslContext parameter
     */
    public static HttpConnection post(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "POST");
    }

    /**
     * Executes the get operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param sslContext      The sslContext parameter
     */
    public static HttpConnection get(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "GET");
    }

    /**
     * Executes the delete operation and modify the payload if necessary.
     *
     * @param url             The url parameter
     * @param sslContext      The sslContext parameter
     */
    public static HttpConnection delete(String url, SSLContext sslContext) {
        return new HttpConnection(url, sslContext, "DELETE");
    }

    /**
     * Executes the addHeader operation and modify the payload if necessary.
     *
     * @param key             The key parameter
     * @param value           The value parameter
     */
    public HttpConnection addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Executes the withBody operation and modify the payload if necessary.
     *
     * @param body            The body parameter
     */
    public HttpConnection withBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Executes the followRedirects operation and modify the payload if necessary.
     *
     * @param follow          The follow parameter
     */
    public HttpConnection followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    /**
     * Executes the execute operation and modify the payload if necessary.
     *
     * @throws IOException    When an error occurs during the operation
     */
    public String execute() throws IOException {
        HttpsURLConnection connection = createConnection();
        if (body != null) {
            writeBody(connection);
        }
        return readResponse(connection);
    }

    /**
     * Executes the executeAndGetRedirect operation and modify the payload if necessary.
     *
     * @throws IOException    When an error occurs during the operation
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
     * Executes the executeAndGetStatus operation and modify the payload if necessary.
     *
     * @throws IOException    When an error occurs during the operation
     */
    public int executeAndGetStatus() throws IOException {
        HttpsURLConnection connection = createConnection();
        if (body != null) {
            writeBody(connection);
        }

        return connection.getResponseCode();
    }

    /**
     * Executes the createConnection operation and modify the payload if necessary.
     *
     * @throws IOException    When an error occurs during the operation
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
     * Executes the writeBody operation and modify the payload if necessary.
     *
     * @param connection      The connection parameter
     * @throws IOException    When an error occurs during the operation
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
     * Executes the readResponse operation and modify the payload if necessary.
     *
     * @param connection      The connection parameter
     * @throws IOException    When an error occurs during the operation
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
