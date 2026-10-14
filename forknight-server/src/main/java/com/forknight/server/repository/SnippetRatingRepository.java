package com.forknight.server.repository;

import com.forknight.server.model.SnippetRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SnippetRatingRepository extends JpaRepository<SnippetRating, Long> {

    List<SnippetRating> findBySnippetIdOrderByCreatedAtDesc(Long snippetId);

    Optional<SnippetRating> findBySnippetIdAndUserId(Long snippetId, Long userId);
}
