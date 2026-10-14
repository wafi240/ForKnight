package com.forknight.server.repository;

import com.forknight.common.dto.pr.PRStatus;
import com.forknight.server.model.PullRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {
    List<PullRequest> findByRepositoryIdOrderByCreatedAtDesc(Long repoId);
    Page<PullRequest> findByRepositoryIdAndStatusOrderByCreatedAtDesc(Long repoId, PRStatus status, Pageable pageable);
    long countByRepositoryIdAndStatus(Long repoId, PRStatus status);
}
