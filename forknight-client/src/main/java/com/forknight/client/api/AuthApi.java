package com.forknight.client.api;

import com.forknight.common.dto.auth.LoginRequest;
import com.forknight.common.dto.auth.LoginResponse;
import com.forknight.common.dto.auth.RegisterRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthApi {
    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<LoginResponse> login(String username, String password) {
        LoginRequest req = new LoginRequest(username, password);
        return apiClient.post("/auth/login", req, LoginResponse.class);
    }

    public CompletableFuture<LoginResponse> register(String username, String email, String password, String displayName) {
        RegisterRequest req = new RegisterRequest(username, email, password, displayName);
        return apiClient.post("/auth/register", req, LoginResponse.class);
    }

    public CompletableFuture<LoginResponse> refreshToken(String refreshToken) {
        return apiClient.post("/auth/refresh", Map.of("refreshToken", refreshToken), LoginResponse.class);
    }
}
