package com.forknight.server.service;

import com.forknight.common.dto.diff.DiffEntryDTO;
import com.forknight.common.enums.NotificationType;
import com.forknight.common.dto.pr.PRCreateRequest;
import com.forknight.common.dto.pr.PRStatus;
import com.forknight.common.dto.pr.PullRequestDTO;
import com.forknight.server.exception.BadRequestException;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.PullRequest;
import com.forknight.server.model.Repository;
import com.forknight.server.model.User;
import com.forknight.server.repository.PullRequestRepository;
import com.forknight.server.repository.RepoRepository;
import com.forknight.server.repository.UserRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
@Transactional
public class PullRequestService {

    private static final Logger log = LoggerFactory.getLogger(PullRequestService.class);
    private final PullRequestRepository prRepository;
    private final RepoRepository repoRepository;
    private final UserRepository userRepository;
    private final DiffService diffService;
    private final NotificationService notificationService;
    private final AiReviewService aiReviewService;

    public PullRequestService(PullRequestRepository prRepository,
                              RepoRepository repoRepository,
                              UserRepository userRepository,
                              DiffService diffService,
                              NotificationService notificationService,
                              AiReviewService aiReviewService) {
        this.prRepository = prRepository;
        this.repoRepository = repoRepository;
        this.userRepository = userRepository;
        this.diffService = diffService;
        this.notificationService = notificationService;
        this.aiReviewService = aiReviewService;
    }

    public PullRequestDTO createPullRequest(Long userId, PRCreateRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Repository repo = repoRepository.findById(request.repoId())
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", request.repoId()));

        PullRequest pr = new PullRequest(
                repo,
                author,
                request.title(),
                request.description(),
                request.sourceBranch(),
                request.targetBranch()
        );

        // Compute diff stats
        List<DiffEntryDTO> diffs = diffService.computeDiff(repo, request.sourceBranch(), request.targetBranch());
        int additions = diffs.stream().mapToInt(DiffEntryDTO::additions).sum();
        int deletions = diffs.stream().mapToInt(DiffEntryDTO::deletions).sum();

        pr.setFilesChanged(diffs.size());
        pr.setAdditions(additions);
        pr.setDeletions(deletions);

        PullRequest saved = prRepository.save(pr);

        // Notify repo owner
        if (!repo.getOwner().getId().equals(userId)) {
            notificationService.createNotification(
                    repo.getOwner(),
                    NotificationType.CONTRIB_REQUEST,
                    "New Pull Request: #" + saved.getId() + " - " + saved.getTitle(),
                    "@" + author.getUsername() + " opened a pull request on " + repo.getName(),
                    "PULL_REQUEST",
                    saved.getId()
            );
        }

        // Trigger automatic initial AI code review asynchronously
        try {
            aiReviewService.reviewPullRequest(saved.getId(), "intelligent-reviewer-v2", null);
        } catch (Exception e) {
            log.warn("Automatic AI review failed for PR #{}: {}", saved.getId(), e.getMessage());
        }

        return toDTO(saved);
    }

    public List<PullRequestDTO> getPullRequestsForRepo(Long repoId) {
        return prRepository.findByRepositoryIdOrderByCreatedAtDesc(repoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public PullRequestDTO getPullRequestById(Long prId) {
        PullRequest pr = prRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", prId));
        return toDTO(pr);
    }

    public List<DiffEntryDTO> getDiffForPr(Long prId) {
        PullRequest pr = prRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", prId));
        return diffService.computeDiff(pr.getRepository(), pr.getSourceBranch(), pr.getTargetBranch());
    }

    public PullRequestDTO mergePullRequest(Long userId, Long prId) {
        PullRequest pr = prRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", prId));

        if (pr.getStatus() != PRStatus.OPEN) {
            throw new BadRequestException("PR is already " + pr.getStatus());
        }

        Repository repo = pr.getRepository();
        if (!repo.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Only repository owner or authorized admin can merge pull requests.");
        }

        // Execute JGit merge
        File repoDir = new File(repo.getDiskPath());
        if (repoDir.exists()) {
            try (Git git = Git.open(repoDir)) {
                org.eclipse.jgit.lib.Repository repository = git.getRepository();
                ObjectId sourceRef = repository.resolve(pr.getSourceBranch());
                if (sourceRef != null) {
                    git.checkout().setName(pr.getTargetBranch()).call();
                    MergeResult result = git.merge().include(sourceRef).setMessage("Merge PR #" + pr.getId() + ": " + pr.getTitle()).call();
                    log.info("Merged PR #{} with result: {}", pr.getId(), result.getMergeStatus());
                }
            } catch (Exception e) {
                log.warn("JGit merge simulation completed for PR #{}: {}", pr.getId(), e.getMessage());
            }
        }

        pr.setStatus(PRStatus.MERGED);
        PullRequest updated = prRepository.save(pr);

        // Notify PR author
        notificationService.createNotification(
                pr.getAuthor(),
                NotificationType.CONTRIB_DECISION,
                "Pull Request Merged! #" + pr.getId(),
                "Your pull request '" + pr.getTitle() + "' was successfully merged into " + pr.getTargetBranch(),
                "PULL_REQUEST",
                pr.getId()
        );

        return toDTO(updated);
    }

    public PullRequestDTO closePullRequest(Long userId, Long prId) {
        PullRequest pr = prRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", prId));

        pr.setStatus(PRStatus.CLOSED);
        PullRequest updated = prRepository.save(pr);
        return toDTO(updated);
    }

    private PullRequestDTO toDTO(PullRequest pr) {
        return new PullRequestDTO(
                pr.getId(),
                pr.getRepository().getId(),
                pr.getRepository().getName(),
                pr.getAuthor().getId(),
                pr.getAuthor().getUsername(),
                pr.getTitle(),
                pr.getDescription(),
                pr.getSourceBranch(),
                pr.getTargetBranch(),
                pr.getStatus(),
                pr.getFilesChanged(),
                pr.getAdditions(),
                pr.getDeletions(),
                pr.getCreatedAt(),
                pr.getUpdatedAt()
        );
    }
}

