package com.forknight.client.api;

import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.SingleFileDownloadRequest;

import java.util.concurrent.CompletableFuture;

public class FileDownloadApi {

    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<FileContentDTO> downloadSingleFile(SingleFileDownloadRequest request) {
        return apiClient.post("/files/download-single", request, FileContentDTO.class);
    }
}
