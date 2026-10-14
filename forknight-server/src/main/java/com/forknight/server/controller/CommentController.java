package com.forknight.server.controller;

import com.forknight.common.dto.comment.CommentCreateRequest;
import com.forknight.common.dto.comment.CommentDTO;
import com.forknight.server.security.UserPrincipal;
import com.forknight.server.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentDTO> postComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CommentCreateRequest request
    ) {
        CommentDTO comment = commentService.createComment(userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/repo/{repoId}")
    public ResponseEntity<List<CommentDTO>> getRepoComments(@PathVariable Long repoId) {
        List<CommentDTO> comments = commentService.getCommentsForRepo(repoId);
        return ResponseEntity.ok(comments);
    }
}
