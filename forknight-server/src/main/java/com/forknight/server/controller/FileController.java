package com.forknight.server.controller;

import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.SingleFileDownloadRequest;
import com.forknight.server.service.GitHubApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final GitHubApiService gitHubApiService;

    public FileController(GitHubApiService gitHubApiService) {
        this.gitHubApiService = gitHubApiService;
    }

    /**
     * Bypasses GitHub repo cloning: fetches single file raw content directly.
     */
    @PostMapping("/download-single")
    public ResponseEntity<FileContentDTO> downloadSingleFile(
            @RequestBody SingleFileDownloadRequest request
    ) {
        FileContentDTO file = gitHubApiService.fetchSingleFile(request);
        return ResponseEntity.ok(file);
    }
}
