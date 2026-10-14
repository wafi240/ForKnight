package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.licensing.CopyLicenseCreateRequest;
import com.forknight.common.dto.licensing.CopyLicenseDecisionRequest;
import com.forknight.common.dto.licensing.CopyLicenseRequestDTO;
import com.forknight.common.util.JsonUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CopyLicenseApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<CopyLicenseRequestDTO> requestCopy(CopyLicenseCreateRequest request) {
        return apiClient.post("/licensing/requests", request, CopyLicenseRequestDTO.class);
    }

    public CompletableFuture<CopyLicenseRequestDTO> decideRequest(Long requestId, CopyLicenseDecisionRequest decision) {
        return apiClient.post("/licensing/requests/" + requestId + "/decision", decision, CopyLicenseRequestDTO.class);
    }

    public CompletableFuture<List<CopyLicenseRequestDTO>> getIncomingRequests() {
        return apiClient.get("/licensing/incoming", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<CopyLicenseRequestDTO>>() {}));
    }

    public CompletableFuture<List<CopyLicenseRequestDTO>> getMyRequests() {
        return apiClient.get("/licensing/my-requests", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<CopyLicenseRequestDTO>>() {}));
    }
}
