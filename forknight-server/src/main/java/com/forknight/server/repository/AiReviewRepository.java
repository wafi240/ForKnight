package com.forknight.server.repository;

import com.forknight.server.model.AiReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiReviewRepository extends JpaRepository<AiReview, Long> {
    List<AiReview> findByPullRequestIdOrderByCreatedAtDesc(Long pullRequestId);
    Optional<AiReview> findFirstByPullRequestIdOrderByCreatedAtDesc(Long pullRequestId);
}
