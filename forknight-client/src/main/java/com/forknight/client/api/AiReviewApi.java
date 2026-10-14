package com.forknight.client.api;

import com.forknight.common.dto.ai.AiReviewDTO;
import com.forknight.common.dto.ai.AiReviewRequest;

import java.util.concurrent.CompletableFuture;

public class AiReviewApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<AiReviewDTO> reviewCode(AiReviewRequest request) {
        return apiClient.post("/ai/review-code", request, AiReviewDTO.class);
    }
}
