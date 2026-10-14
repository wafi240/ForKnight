package com.forknight.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.forknight.common.dto.PagedResponse;
import com.forknight.common.dto.repo.CommitDTO;
import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.FileNodeDTO;
import com.forknight.common.dto.repo.RepoCreateRequest;
import com.forknight.common.dto.repo.RepoDTO;
import com.forknight.common.util.JsonUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RepoApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<RepoDTO> createRepo(RepoCreateRequest request) {
        return apiClient.post("/repos", request, RepoDTO.class);
    }

    public CompletableFuture<List<RepoDTO>> getMyRepos() {
        return apiClient.get("/repos", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<RepoDTO>>() {}));
    }

    public CompletableFuture<PagedResponse<RepoDTO>> exploreRepos(String search, int page, int size) {
        StringBuilder query = new StringBuilder("/repos/explore?page=").append(page).append("&size=").append(size);
        if (search != null && !search.isBlank()) {
            query.append("&search=").append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8));
        }
        return apiClient.get(query.toString(), String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<PagedResponse<RepoDTO>>() {}));
    }

    public CompletableFuture<RepoDTO> getRepoById(Long id) {
        return apiClient.get("/repos/" + id, RepoDTO.class);
    }

    public CompletableFuture<List<FileNodeDTO>> getFileTree(Long id) {
        return apiClient.get("/repos/" + id + "/tree", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<FileNodeDTO>>() {}));
    }

    public CompletableFuture<FileContentDTO> getFileContent(Long id, String path) {
        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        return apiClient.get("/repos/" + id + "/files?path=" + encodedPath, FileContentDTO.class);
    }

    public CompletableFuture<List<CommitDTO>> getCommitHistory(Long id) {
        return apiClient.get("/repos/" + id + "/commits", String.class)
                .thenApply(json -> JsonUtil.fromJson(json, new TypeReference<List<CommitDTO>>() {}));
    }

    public CompletableFuture<com.forknight.common.dto.graph.GitGraphDataDTO> getGraph3D(Long id) {
        return apiClient.get("/repos/" + id + "/graph-3d", com.forknight.common.dto.graph.GitGraphDataDTO.class);
    }

    public CompletableFuture<com.forknight.common.dto.repo.GitStatusDTO> getGitStatus(Long id) {
        return apiClient.get("/repos/" + id + "/git/status", com.forknight.common.dto.repo.GitStatusDTO.class);
    }

    public CompletableFuture<com.forknight.common.dto.repo.GitStatusDTO> createBranch(Long id, String branchName) {
        var req = new com.forknight.common.dto.repo.GitCommandRequest("BRANCH", branchName, null);
        return apiClient.post("/repos/" + id + "/git/branch", req, com.forknight.common.dto.repo.GitStatusDTO.class);
    }

    public CompletableFuture<com.forknight.common.dto.repo.GitStatusDTO> checkoutBranch(Long id, String branchName) {
        var req = new com.forknight.common.dto.repo.GitCommandRequest("CHECKOUT", branchName, null);
        return apiClient.post("/repos/" + id + "/git/checkout", req, com.forknight.common.dto.repo.GitStatusDTO.class);
    }

    public CompletableFuture<CommitDTO> commitChanges(Long id, String commitMessage) {
        var req = new com.forknight.common.dto.repo.GitCommandRequest("COMMIT", null, commitMessage);
        return apiClient.post("/repos/" + id + "/git/commit", req, CommitDTO.class);
    }

    public CompletableFuture<com.forknight.common.dto.repo.GitStatusDTO> stashChanges(Long id) {
        return apiClient.post("/repos/" + id + "/git/stash", null, com.forknight.common.dto.repo.GitStatusDTO.class);
    }

    public CompletableFuture<Void> uploadFile(Long id, com.forknight.common.dto.repo.FileUploadRequest request) {
        return apiClient.post("/repos/" + id + "/files/upload", request, Void.class);
    }
}
