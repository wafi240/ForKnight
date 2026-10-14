package com.forknight.server.service;

import com.forknight.common.dto.secret.SecretScanRequest;
import com.forknight.common.dto.secret.SecretScanResultDTO;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.Repository;
import com.forknight.server.model.SecretScanResult;
import com.forknight.server.repository.RepoRepository;
import com.forknight.server.repository.SecretScanResultRepository;
import com.forknight.server.scanner.SecretScanner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
public class SecretScannerService {

    private final SecretScanner secretScanner;
    private final SecretScanResultRepository scanResultRepository;
    private final RepoRepository repoRepository;

    public SecretScannerService(
            SecretScanner secretScanner,
            SecretScanResultRepository scanResultRepository,
            RepoRepository repoRepository
    ) {
        this.secretScanner = secretScanner;
        this.scanResultRepository = scanResultRepository;
        this.repoRepository = repoRepository;
    }

    /**
     * Scans raw content provided in a request (e.g. pre-commit hook validation).
     */
    public List<SecretScanResultDTO> scanContent(SecretScanRequest request) {
        List<SecretScanner.Finding> findings = secretScanner.scanContent(
                request.filePath() != null ? request.filePath() : "snippet",
                request.content()
        );

        Repository repo = null;
        if (request.repoId() != null) {
            repo = repoRepository.findById(request.repoId()).orElse(null);
        }

        List<SecretScanResultDTO> results = new ArrayList<>();
        for (SecretScanner.Finding finding : findings) {
            SecretScanResult entity = new SecretScanResult();
            entity.setRepository(repo);
            entity.setFilePath(finding.filePath());
            entity.setLineNumber(finding.lineNumber());
            entity.setSecretType(finding.secretType());
            entity.setRuleName(finding.ruleName());
            entity.setMaskedSnippet(finding.maskedSnippet());
            entity.setSeverity(finding.severity());
            entity.setResolved(false);

            SecretScanResult saved = scanResultRepository.save(entity);
            results.add(saved.toDTO());
        }

        return results;
    }

    /**
     * Scans all files in an entire repository disk path.
     */
    public List<SecretScanResultDTO> scanRepository(Long repoId) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        File repoDir = new File(repo.getDiskPath());
        if (!repoDir.exists()) {
            return List.of();
        }

        List<SecretScanResultDTO> allResults = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(repoDir.toPath())) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> !p.toString().contains(".git"))
                  .filter(p -> p.toFile().length() < 2_000_000) // Skip huge binary files
                  .forEach(path -> {
                      try {
                          String content = Files.readString(path);
                          String relativePath = repoDir.toPath().relativize(path).toString().replace('\\', '/');
                          List<SecretScanner.Finding> findings = secretScanner.scanContent(relativePath, content);
                          for (SecretScanner.Finding finding : findings) {
                              SecretScanResult entity = new SecretScanResult();
                              entity.setRepository(repo);
                              entity.setFilePath(finding.filePath());
                              entity.setLineNumber(finding.lineNumber());
                              entity.setSecretType(finding.secretType());
                              entity.setRuleName(finding.ruleName());
                              entity.setMaskedSnippet(finding.maskedSnippet());
                              entity.setSeverity(finding.severity());
                              entity.setResolved(false);
                              allResults.add(scanResultRepository.save(entity).toDTO());
                          }
                      } catch (IOException ignored) {
                          // Skip unreadable files
                      }
                  });
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan repository files", e);
        }

        return allResults;
    }

    public List<SecretScanResultDTO> getResultsForRepo(Long repoId) {
        return scanResultRepository.findByRepositoryIdOrderByDetectedAtDesc(repoId)
                .stream()
                .map(SecretScanResult::toDTO)
                .toList();
    }
}
