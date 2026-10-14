package com.forknight.server.model;

import com.forknight.common.dto.licensing.CopyLicenseRequestDTO;
import com.forknight.common.enums.CopyLicenseStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "copy_license_requests")
public class CopyLicenseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repo_id", nullable = false)
    private Repository repository;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CopyLicenseStatus status = CopyLicenseStatus.PENDING;

    @Column(name = "license_fee", precision = 12, scale = 2)
    private BigDecimal licenseFee;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "response_msg", columnDefinition = "TEXT")
    private String responseMsg;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "NONE";

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public CopyLicenseRequest() {}

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public CopyLicenseRequestDTO toDTO() {
        return new CopyLicenseRequestDTO(
                id,
                repository != null ? repository.getId() : null,
                repository != null ? repository.getName() : null,
                repository != null && repository.getOwner() != null ? repository.getOwner().getUsername() : null,
                requester != null ? requester.getId() : null,
                requester != null ? requester.getUsername() : null,
                status,
                licenseFee,
                message,
                responseMsg,
                paymentStatus,
                decidedAt,
                createdAt
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

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public CopyLicenseStatus getStatus() {
        return status;
    }

    public void setStatus(CopyLicenseStatus status) {
        this.status = status;
    }

    public BigDecimal getLicenseFee() {
        return licenseFee;
    }

    public void setLicenseFee(BigDecimal licenseFee) {
        this.licenseFee = licenseFee;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
