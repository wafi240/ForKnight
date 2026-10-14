package com.forknight.server.service;

import com.forknight.common.dto.licensing.CopyLicenseCreateRequest;
import com.forknight.common.dto.licensing.CopyLicenseDecisionRequest;
import com.forknight.common.dto.licensing.CopyLicenseRequestDTO;
import com.forknight.common.enums.CopyLicenseStatus;
import com.forknight.common.enums.NotificationType;
import com.forknight.server.exception.BadRequestException;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.CopyLicenseRequest;
import com.forknight.server.model.Repository;
import com.forknight.server.model.User;
import com.forknight.server.repository.CopyLicenseRequestRepository;
import com.forknight.server.repository.RepoRepository;
import com.forknight.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CopyLicenseService {

    private final CopyLicenseRequestRepository licenseRepository;
    private final RepoRepository repoRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CopyLicenseService(
            CopyLicenseRequestRepository licenseRepository,
            RepoRepository repoRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.licenseRepository = licenseRepository;
        this.repoRepository = repoRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public CopyLicenseRequestDTO createRequest(Long requesterId, CopyLicenseCreateRequest request) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));

        Repository repo = repoRepository.findById(request.repoId())
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", request.repoId()));

        if (repo.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("You are already the owner of this repository");
        }

        CopyLicenseRequest clr = new CopyLicenseRequest();
        clr.setRepository(repo);
        clr.setRequester(requester);
        clr.setStatus(CopyLicenseStatus.PENDING);
        clr.setMessage(request.message());

        CopyLicenseRequest saved = licenseRepository.save(clr);

        // Notify repo owner
        String title = "New Copy / License Request";
        String msg = String.format("@%s requested permission to copy/fork '%s'", requester.getUsername(), repo.getName());
        notificationService.createNotification(repo.getOwner(), NotificationType.COPY_REQUEST, title, msg, "REPO", repo.getId());

        return saved.toDTO();
    }

    public CopyLicenseRequestDTO decideRequest(Long ownerId, Long requestId, CopyLicenseDecisionRequest decision) {
        CopyLicenseRequest request = licenseRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CopyLicenseRequest", "id", requestId));

        if (!request.getRepository().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Only the repository owner can decide copy/license requests");
        }

        request.setStatus(decision.status());
        request.setResponseMsg(decision.responseMsg());
        request.setDecidedAt(Instant.now());

        if (decision.status() == CopyLicenseStatus.APPROVED_PAID) {
            request.setLicenseFee(decision.licenseFee());
            request.setPaymentStatus("PENDING");

            String title = "Commercial License Approved ($" + decision.licenseFee() + ") 📜";
            String msg = String.format("Owner approved your copy request for '%s' under a Commercial License for $%s.",
                    request.getRepository().getName(), decision.licenseFee());
            notificationService.createNotification(request.getRequester(), NotificationType.COPY_DECISION, title, msg, "REPO", request.getRepository().getId());
        } else if (decision.status() == CopyLicenseStatus.APPROVED_FREE) {
            request.setPaymentStatus("COMPLETED");

            String title = "Open Source Fork/Copy Approved! 🚀";
            String msg = String.format("Owner approved your request to copy '%s' as Open Source (Free)!",
                    request.getRepository().getName());
            notificationService.createNotification(request.getRequester(), NotificationType.COPY_DECISION, title, msg, "REPO", request.getRepository().getId());
        } else {
            String title = "Copy Request Denied";
            String msg = String.format("Your copy request for '%s' was denied.%s",
                    request.getRepository().getName(),
                    decision.responseMsg() != null ? " Reason: " + decision.responseMsg() : "");
            notificationService.createNotification(request.getRequester(), NotificationType.COPY_DECISION, title, msg, "REPO", request.getRepository().getId());
        }

        return licenseRepository.save(request).toDTO();
    }

    @Transactional(readOnly = true)
    public List<CopyLicenseRequestDTO> getIncomingRequestsForOwner(Long ownerId) {
        return licenseRepository.findByRepoOwnerId(ownerId)
                .stream()
                .map(CopyLicenseRequest::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CopyLicenseRequestDTO> getMySubmittedRequests(Long requesterId) {
        return licenseRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId)
                .stream()
                .map(CopyLicenseRequest::toDTO)
                .toList();
    }
}
