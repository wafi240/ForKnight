package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.contrib.CollaboratorDTO;
import com.forknight.common.dto.contrib.ContribCreateRequest;
import com.forknight.common.dto.contrib.ContribDecisionRequest;
import com.forknight.common.dto.contrib.ContribRequestDTO;
import com.forknight.common.util.JsonUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ContribApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<ContribRequestDTO> requestContribution(ContribCreateRequest request) {
        return apiClient.post("/contrib/requests", request, ContribRequestDTO.class);
    }

    public CompletableFuture<ContribRequestDTO> decideRequest(Long requestId, ContribDecisionRequest decision) {
        return apiClient.post("/contrib/requests/" + requestId + "/decision", decision, ContribRequestDTO.class);
    }

    public CompletableFuture<List<ContribRequestDTO>> getIncomingRequests() {
        return apiClient.get("/contrib/incoming", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<ContribRequestDTO>>() {}));
    }

    public CompletableFuture<List<ContribRequestDTO>> getMyRequests() {
        return apiClient.get("/contrib/my-requests", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<ContribRequestDTO>>() {}));
    }

    public CompletableFuture<List<CollaboratorDTO>> getCollaborators(Long repoId) {
        return apiClient.get("/contrib/repo/" + repoId + "/collaborators", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<CollaboratorDTO>>() {}));
    }
}
