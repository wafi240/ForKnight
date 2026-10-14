package com.forknight.server.repository;

import com.forknight.server.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRepositoryIdAndParentIsNullOrderByCreatedAtDesc(Long repoId);

    List<Comment> findByRepositoryIdOrderByCreatedAtAsc(Long repoId);

    List<Comment> findByRepositoryIdAndFilePath(Long repoId, String filePath);
}
