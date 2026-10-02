package com.example;

import javafx.application.Platform;
import java.net.URI;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * 🚀 Native HTTP Client for ESP32 & Wokwi IoT Labs.
 * Pure standard Java implementation (Java 11+) without external dependencies.
 * Bypasses system proxies and automatically executes callbacks on the JavaFX Application Thread.
 */
public class Client {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(3))
            .proxy(ProxySelector.of(null)) // Direct connection, bypass all proxies
            .build();

    /**
     * Response wrapper containing status code, body text, and error if any.
     */
    public static class Response {
        private final int statusCode;
        private final String body;
        private final Throwable error;

        public Response(int statusCode, String body, Throwable error) {
            this.statusCode = statusCode;
            this.body = body != null ? body : "";
            this.error = error;
        }

        public boolean isSuccess() {
            return error == null && statusCode >= 200 && statusCode < 300;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public Throwable getError() {
            return error;
        }

        @Override
        public String toString() {
            return "Response{statusCode=" + statusCode + ", body='" + body + "'}";
        }
    }

    /**
     * Sends an asynchronous GET request to the specified URL.
     * The callback is guaranteed to be invoked on the JavaFX UI thread (Platform.runLater).
     *
     * @param url Target URL (e.g. "http://127.0.0.1:4000/status")
     * @param callback Lambda receiving the Response object
     */
    public static void get(String url, Consumer<Response> callback) {
        getAsync(url, callback);
    }

    /**
     * Sends an asynchronous GET request to the specified URL.
     * The callback is guaranteed to be invoked on the JavaFX UI thread (Platform.runLater).
     *
     * @param url Target URL
     * @param callback Lambda receiving the Response object
     */
    public static void getAsync(String url, Consumer<Response> callback) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", "JavaFX-IoT-Client/1.0")
                    .header("Connection", "close")
                    .header("Accept", "*/*")
                    .GET()
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .whenComplete((httpResponse, throwable) -> {
                        Response resp;
                        if (throwable != null) {
                            resp = new Response(0, "", throwable);
                        } else {
                            resp = new Response(httpResponse.statusCode(), httpResponse.body(), null);
                        }

                        if (callback != null) {
                            Platform.runLater(() -> callback.accept(resp));
                        }
                    });
        } catch (Exception ex) {
            Response resp = new Response(0, "", ex);
            if (callback != null) {
                Platform.runLater(() -> callback.accept(resp));
            }
        }
    }

    /**
     * Synchronously sends a GET request (blocking).
     *
     * @param url Target URL
     * @return Response object
     */
    public static Response getSync(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", "JavaFX-IoT-Client/1.0")
                    .header("Connection", "close")
                    .header("Accept", "*/*")
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return new Response(httpResponse.statusCode(), httpResponse.body(), null);
        } catch (Exception ex) {
            return new Response(0, "", ex);
        }
    }
}
