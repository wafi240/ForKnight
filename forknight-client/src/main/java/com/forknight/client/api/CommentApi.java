package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.comment.CommentCreateRequest;
import com.forknight.common.dto.comment.CommentDTO;
import com.forknight.common.util.JsonUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommentApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<CommentDTO> postComment(CommentCreateRequest request) {
        return apiClient.post("/comments", request, CommentDTO.class);
    }

    public CompletableFuture<List<CommentDTO>> getCommentsForRepo(Long repoId) {
        return apiClient.get("/comments/repo/" + repoId, String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<CommentDTO>>() {}));
    }
}
