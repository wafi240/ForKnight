package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.ai.AiReviewDTO;
import com.forknight.common.dto.diff.DiffEntryDTO;
import com.forknight.common.dto.pr.PRCreateRequest;
import com.forknight.common.dto.pr.PullRequestDTO;
import com.forknight.common.util.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PullRequestApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<PullRequestDTO> createPR(Long repoId, PRCreateRequest request) {
        return apiClient.post("/repos/" + repoId + "/prs", request, PullRequestDTO.class);
    }

    public CompletableFuture<List<PullRequestDTO>> getRepoPRs(Long repoId) {
        return apiClient.get("/repos/" + repoId + "/prs", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<PullRequestDTO>>() {}));
    }

    public CompletableFuture<PullRequestDTO> getPRById(Long prId) {
        return apiClient.get("/prs/" + prId, PullRequestDTO.class);
    }

    public CompletableFuture<List<DiffEntryDTO>> getPRDiff(Long prId) {
        return apiClient.get("/prs/" + prId + "/diff", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<DiffEntryDTO>>() {}));
    }

    public CompletableFuture<PullRequestDTO> mergePR(Long prId) {
        return apiClient.post("/prs/" + prId + "/merge", null, PullRequestDTO.class);
    }

    public CompletableFuture<PullRequestDTO> closePR(Long prId) {
        return apiClient.post("/prs/" + prId + "/close", null, PullRequestDTO.class);
    }

    public CompletableFuture<AiReviewDTO> triggerAiReview(Long prId, String model, String apiKey) {
        Map<String, String> body = Map.of(
                "model", model != null ? model : "intelligent-reviewer-v2",
                "apiKey", apiKey != null ? apiKey : ""
        );
        return apiClient.post("/prs/" + prId + "/ai-review", body, AiReviewDTO.class);
    }

    public CompletableFuture<List<AiReviewDTO>> getPrReviews(Long prId) {
        return apiClient.get("/prs/" + prId + "/reviews", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<AiReviewDTO>>() {}));
    }
}
