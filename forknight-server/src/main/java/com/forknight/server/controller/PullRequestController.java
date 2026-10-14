package com.forknight.server.controller;

import com.forknight.common.dto.ai.AiReviewDTO;
import com.forknight.common.dto.diff.DiffEntryDTO;
import com.forknight.common.dto.pr.PRCreateRequest;
import com.forknight.common.dto.pr.PullRequestDTO;
import com.forknight.server.model.PullRequest;
import com.forknight.server.repository.PullRequestRepository;
import com.forknight.server.security.UserPrincipal;
import com.forknight.server.service.AiReviewService;
import com.forknight.server.service.PullRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PullRequestController {

    private final PullRequestService prService;
    private final AiReviewService aiReviewService;
    private final PullRequestRepository prRepository;

    public PullRequestController(PullRequestService prService,
                                 AiReviewService aiReviewService,
                                 PullRequestRepository prRepository) {
        this.prService = prService;
        this.aiReviewService = aiReviewService;
        this.prRepository = prRepository;
    }

    @PostMapping("/repos/{repoId}/prs")
    public ResponseEntity<PullRequestDTO> createPR(
            @PathVariable Long repoId,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody PRCreateRequest request) {
        PRCreateRequest effectiveReq = new PRCreateRequest(repoId, request.title(), request.description(), request.sourceBranch(), request.targetBranch());
        return ResponseEntity.status(HttpStatus.CREATED).body(prService.createPullRequest(user.getId(), effectiveReq));
    }

    @GetMapping("/repos/{repoId}/prs")
    public ResponseEntity<List<PullRequestDTO>> getRepoPRs(@PathVariable Long repoId) {
        return ResponseEntity.ok(prService.getPullRequestsForRepo(repoId));
    }

    @GetMapping("/prs/{id}")
    public ResponseEntity<PullRequestDTO> getPRById(@PathVariable Long id) {
        return ResponseEntity.ok(prService.getPullRequestById(id));
    }

    @GetMapping("/prs/{id}/diff")
    public ResponseEntity<List<DiffEntryDTO>> getPRDiff(@PathVariable Long id) {
        return ResponseEntity.ok(prService.getDiffForPr(id));
    }

    @PostMapping("/prs/{id}/merge")
    public ResponseEntity<PullRequestDTO> mergePR(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(prService.mergePullRequest(user.getId(), id));
    }

    @PostMapping("/prs/{id}/close")
    public ResponseEntity<PullRequestDTO> closePR(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(prService.closePullRequest(user.getId(), id));
    }

    @PostMapping("/prs/{id}/ai-review")
    public ResponseEntity<AiReviewDTO> triggerAiReview(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        
        String model = body != null ? body.get("model") : "intelligent-reviewer-v2";
        String apiKey = body != null ? body.get("apiKey") : null;
        return ResponseEntity.ok(aiReviewService.reviewPullRequest(id, model, apiKey));
    }

    @GetMapping("/prs/{id}/reviews")
    public ResponseEntity<List<AiReviewDTO>> getPrReviews(@PathVariable Long id) {
        return ResponseEntity.ok(aiReviewService.getReviewsForPr(id));
    }
}

