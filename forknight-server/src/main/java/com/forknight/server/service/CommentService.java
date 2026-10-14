package com.forknight.server.service;

import com.forknight.common.dto.comment.CommentCreateRequest;
import com.forknight.common.dto.comment.CommentDTO;
import com.forknight.common.enums.NotificationType;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.Comment;
import com.forknight.server.model.Repository;
import com.forknight.server.model.User;
import com.forknight.server.repository.CommentRepository;
import com.forknight.server.repository.RepoRepository;
import com.forknight.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final RepoRepository repoRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentService(
            CommentRepository commentRepository,
            RepoRepository repoRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.commentRepository = commentRepository;
        this.repoRepository = repoRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public CommentDTO createComment(Long authorId, CommentCreateRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Repository repo = repoRepository.findById(request.repoId())
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", request.repoId()));

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.parentId()));
        }

        Comment comment = new Comment();
        comment.setRepository(repo);
        comment.setAuthor(author);
        comment.setParent(parent);
        comment.setContent(request.content());
        comment.setSuggestion(request.isSuggestion());
        comment.setFilePath(request.filePath());
        comment.setLineNumber(request.lineNumber());

        Comment saved = commentRepository.save(comment);

        // Notify repo owner (if author != owner)
        if (!repo.getOwner().getId().equals(author.getId())) {
            String title = request.isSuggestion() ? "New Code Suggestion" : "New Public Comment";
            String msg = String.format("@%s commented on '%s': %s",
                    author.getUsername(), repo.getName(),
                    comment.getContent().length() > 60 ? comment.getContent().substring(0, 60) + "..." : comment.getContent());
            notificationService.createNotification(repo.getOwner(), NotificationType.COMMENT, title, msg, "REPO", repo.getId());
        }

        // If reply, notify parent author
        if (parent != null && !parent.getAuthor().getId().equals(author.getId())) {
            String title = "Reply to your comment";
            String msg = String.format("@%s replied to your comment on '%s'", author.getUsername(), repo.getName());
            notificationService.createNotification(parent.getAuthor(), NotificationType.COMMENT, title, msg, "REPO", repo.getId());
        }

        return saved.toDTO();
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsForRepo(Long repoId) {
        return commentRepository.findByRepositoryIdAndParentIsNullOrderByCreatedAtDesc(repoId)
                .stream()
                .map(Comment::toDTO)
                .toList();
    }
}
