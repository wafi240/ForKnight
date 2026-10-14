package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.secret.SecretScanRequest;
import com.forknight.common.dto.secret.SecretScanResultDTO;
import com.forknight.common.util.JsonUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SecretScanApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<List<SecretScanResultDTO>> scanContent(SecretScanRequest request) {
        return apiClient.post("/secrets/scan-content", request, String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<SecretScanResultDTO>>() {}));
    }

    public CompletableFuture<List<SecretScanResultDTO>> scanRepository(Long repoId) {
        return apiClient.post("/secrets/scan-repo/" + repoId, null, String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<SecretScanResultDTO>>() {}));
    }

    public CompletableFuture<List<SecretScanResultDTO>> getRepoScanResults(Long repoId) {
        return apiClient.get("/secrets/repo/" + repoId, String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<SecretScanResultDTO>>() {}));
    }
}
