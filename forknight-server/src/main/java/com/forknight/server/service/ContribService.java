package com.forknight.server.service;

import com.forknight.common.dto.contrib.CollaboratorDTO;
import com.forknight.common.dto.contrib.ContribCreateRequest;
import com.forknight.common.dto.contrib.ContribDecisionRequest;
import com.forknight.common.dto.contrib.ContribRequestDTO;
import com.forknight.common.enums.AccessLevel;
import com.forknight.common.enums.NotificationType;
import com.forknight.common.enums.RequestStatus;
import com.forknight.server.exception.BadRequestException;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.ContributionRequest;
import com.forknight.server.model.RepoCollaborator;
import com.forknight.server.model.Repository;
import com.forknight.server.model.User;
import com.forknight.server.repository.ContributionRequestRepository;
import com.forknight.server.repository.RepoCollaboratorRepository;
import com.forknight.server.repository.RepoRepository;
import com.forknight.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ContribService {

    private final ContributionRequestRepository contribRepository;
    private final RepoCollaboratorRepository collaboratorRepository;
    private final RepoRepository repoRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ContribService(
            ContributionRequestRepository contribRepository,
            RepoCollaboratorRepository collaboratorRepository,
            RepoRepository repoRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.contribRepository = contribRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.repoRepository = repoRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public ContribRequestDTO createRequest(Long requesterId, ContribCreateRequest request) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));

        Repository repo = repoRepository.findById(request.repoId())
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", request.repoId()));

        if (repo.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("You are already the owner of this repository");
        }

        if (collaboratorRepository.existsByRepositoryIdAndUserId(repo.getId(), requesterId)) {
            throw new BadRequestException("You are already a collaborator on this repository");
        }

        // Check if there is already an active pending request
        contribRepository.findByRepositoryIdAndRequesterIdAndStatus(repo.getId(), requesterId, RequestStatus.PENDING)
                .ifPresent(r -> {
                    throw new BadRequestException("You already have a pending contribution request for this repository");
                });

        ContributionRequest cr = new ContributionRequest();
        cr.setRepository(repo);
        cr.setRequester(requester);
        cr.setStatus(RequestStatus.PENDING);
        cr.setMessage(request.message());

        ContributionRequest saved = contribRepository.save(cr);

        // Notify repo owner
        String title = "New Contribution Request";
        String msg = String.format("@%s requested contributor access to '%s'", requester.getUsername(), repo.getName());
        notificationService.createNotification(repo.getOwner(), NotificationType.CONTRIB_REQUEST, title, msg, "REPO", repo.getId());

        return saved.toDTO();
    }

    public ContribRequestDTO decideRequest(Long ownerId, Long requestId, ContribDecisionRequest decision) {
        ContributionRequest request = contribRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ContributionRequest", "id", requestId));

        if (!request.getRepository().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Only the repository owner can approve or deny contribution requests");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("This contribution request has already been decided: " + request.getStatus());
        }

        request.setStatus(decision.status());
        request.setResponseMsg(decision.responseMsg());
        request.setDecidedAt(Instant.now());

        if (decision.status() == RequestStatus.APPROVED) {
            AccessLevel level = decision.accessLevel() != null ? decision.accessLevel() : AccessLevel.WRITE;
            request.setAccessLevel(level);

            // Grant collaborator access
            RepoCollaborator collaborator = collaboratorRepository
                    .findByRepositoryIdAndUserId(request.getRepository().getId(), request.getRequester().getId())
                    .orElseGet(() -> {
                        RepoCollaborator rc = new RepoCollaborator();
                        rc.setRepository(request.getRepository());
                        rc.setUser(request.getRequester());
                        return rc;
                    });

            collaborator.setAccessLevel(level);
            collaboratorRepository.save(collaborator);

            // Notify requester of approval
            String title = "Contribution Request Approved! 🎉";
            String msg = String.format("You are now a collaborator on '%s' with %s access!",
                    request.getRepository().getName(), level);
            notificationService.createNotification(request.getRequester(), NotificationType.CONTRIB_DECISION, title, msg, "REPO", request.getRepository().getId());
        } else {
            // Notify requester of denial
            String title = "Contribution Request Denied";
            String msg = String.format("Your contribution request for '%s' was denied.%s",
                    request.getRepository().getName(),
                    decision.responseMsg() != null ? " Reason: " + decision.responseMsg() : "");
            notificationService.createNotification(request.getRequester(), NotificationType.CONTRIB_DECISION, title, msg, "REPO", request.getRepository().getId());
        }

        return contribRepository.save(request).toDTO();
    }

    @Transactional(readOnly = true)
    public List<ContribRequestDTO> getIncomingRequestsForOwner(Long ownerId) {
        return contribRepository.findByRepoOwnerId(ownerId)
                .stream()
                .map(ContributionRequest::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContribRequestDTO> getMySubmittedRequests(Long requesterId) {
        return contribRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId)
                .stream()
                .map(ContributionRequest::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollaboratorDTO> getCollaboratorsForRepo(Long repoId) {
        return collaboratorRepository.findByRepositoryId(repoId)
                .stream()
                .map(RepoCollaborator::toDTO)
                .toList();
    }
}
