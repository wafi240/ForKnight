package com.forknight.server.controller;

import com.forknight.common.dto.ai.AiReviewDTO;
import com.forknight.common.dto.ai.AiReviewRequest;
import com.forknight.server.service.AiReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiReviewController {

    private final AiReviewService aiReviewService;

    public AiReviewController(AiReviewService aiReviewService) {
        this.aiReviewService = aiReviewService;
    }

    @PostMapping("/review-code")
    public ResponseEntity<AiReviewDTO> reviewCode(@RequestBody AiReviewRequest request) {
        return ResponseEntity.ok(aiReviewService.reviewSnippetOrDiff(request));
    }
}
