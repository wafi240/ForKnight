package com.forknight.server.model;

import com.forknight.common.dto.secret.SecretScanResultDTO;
import com.forknight.common.enums.SecretSeverity;
import com.forknight.common.enums.SecretType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "secret_scan_results")
public class SecretScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private Repository repository;

    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "secret_type", nullable = false, length = 50)
    private SecretType secretType;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "masked_snippet", length = 255)
    private String maskedSnippet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SecretSeverity severity = SecretSeverity.HIGH;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved = false;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    public SecretScanResult() {}

    @PrePersist
    protected void onCreate() {
        detectedAt = Instant.now();
    }

    public SecretScanResultDTO toDTO() {
        return new SecretScanResultDTO(
                id,
                repository != null ? repository.getId() : null,
                filePath,
                lineNumber,
                secretType,
                ruleName,
                maskedSnippet,
                severity,
                isResolved,
                detectedAt
        );
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public SecretType getSecretType() {
        return secretType;
    }

    public void setSecretType(SecretType secretType) {
        this.secretType = secretType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getMaskedSnippet() {
        return maskedSnippet;
    }

    public void setMaskedSnippet(String maskedSnippet) {
        this.maskedSnippet = maskedSnippet;
    }

    public SecretSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(SecretSeverity severity) {
        this.severity = severity;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }
}
