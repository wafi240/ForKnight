package com.forknight.client.api;

import com.forknight.client.config.AppConfig;
import com.forknight.client.config.SessionManager;
import com.forknight.client.config.ThreadPools;
import com.forknight.common.dto.ApiError;
import com.forknight.common.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private final HttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AppConfig.CONNECT_TIMEOUT_SECONDS))
                .executor(ThreadPools.IO_POOL)
                .build();
    }

    public <T> CompletableFuture<T> get(String path, Class<T> responseType) {
        HttpRequest request = buildRequest(path)
                .GET()
                .build();
        return sendRequest(request, responseType);
    }

    public <T> CompletableFuture<T> post(String path, Object body, Class<T> responseType) {
        HttpRequest request = buildRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(body)))
                .build();
        return sendRequest(request, responseType);
    }

    public <T> CompletableFuture<T> put(String path, Object body, Class<T> responseType) {
        HttpRequest request = buildRequest(path)
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(body)))
                .build();
        return sendRequest(request, responseType);
    }

    public CompletableFuture<Void> delete(String path) {
        HttpRequest request = buildRequest(path)
                .DELETE()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::handleResponse)
                .thenApply(res -> null);
    }

    private HttpRequest.Builder buildRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_BASE_URL + path))
                .timeout(Duration.ofSeconds(AppConfig.REQUEST_TIMEOUT_SECONDS))
                .header("Content-Type", "application/json");

        String token = SessionManager.getInstance().getAccessToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    @SuppressWarnings("unchecked")
    private <T> CompletableFuture<T> sendRequest(HttpRequest request, Class<T> responseType) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::handleResponse)
                .thenApply(body -> {
                    if (responseType == Void.class || body == null || body.trim().isEmpty()) {
                        return null;
                    }
                    if (responseType == String.class) {
                        return (T) body;
                    }
                    return JsonUtil.fromJson(body, responseType);
                });
    }

    public <T> CompletableFuture<T> get(String path, com.fasterxml.jackson.core.type.TypeReference<T> typeRef) {
        HttpRequest request = buildRequest(path).GET().build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::handleResponse)
                .thenApply(body -> JsonUtil.fromJson(body, typeRef));
    }

    private String handleResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            String errorMsg = null;
            if (response.body() != null && !response.body().isBlank()) {
                try {
                    ApiError error = JsonUtil.fromJson(response.body(), ApiError.class);
                    if (error != null) {
                        errorMsg = error.message() != null ? error.message() : error.error();
                    }
                } catch (Exception ignored) {
                    errorMsg = response.body();
                }
            }
            if (errorMsg == null || errorMsg.isBlank()) {
                errorMsg = "HTTP Error " + response.statusCode();
            }
            throw new RuntimeException(errorMsg);
        }
    }
}
