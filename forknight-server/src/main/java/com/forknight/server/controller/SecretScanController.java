package com.forknight.server.controller;

import com.forknight.common.dto.secret.SecretScanRequest;
import com.forknight.common.dto.secret.SecretScanResultDTO;
import com.forknight.server.service.SecretScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/secrets")
public class SecretScanController {

    private final SecretScannerService secretScannerService;

    public SecretScanController(SecretScannerService secretScannerService) {
        this.secretScannerService = secretScannerService;
    }

    /**
     * Pre-commit scanner: checks code/diff content for hardcoded secrets.
     */
    @PostMapping("/scan-content")
    public ResponseEntity<List<SecretScanResultDTO>> scanContent(
            @RequestBody SecretScanRequest request
    ) {
        List<SecretScanResultDTO> results = secretScannerService.scanContent(request);
        return ResponseEntity.ok(results);
    }

    /**
     * Full repository secret scan.
     */
    @PostMapping("/scan-repo/{repoId}")
    public ResponseEntity<List<SecretScanResultDTO>> scanRepository(
            @PathVariable Long repoId
    ) {
        List<SecretScanResultDTO> results = secretScannerService.scanRepository(repoId);
        return ResponseEntity.ok(results);
    }

    /**
     * Retrieves previous scan findings for a repository.
     */
    @GetMapping("/repo/{repoId}")
    public ResponseEntity<List<SecretScanResultDTO>> getRepoScanResults(
            @PathVariable Long repoId
    ) {
        List<SecretScanResultDTO> results = secretScannerService.getResultsForRepo(repoId);
        return ResponseEntity.ok(results);
    }
}
