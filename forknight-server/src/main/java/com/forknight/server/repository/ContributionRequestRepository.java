package com.forknight.server.repository;

import com.forknight.common.enums.RequestStatus;
import com.forknight.server.model.ContributionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContributionRequestRepository extends JpaRepository<ContributionRequest, Long> {

    List<ContributionRequest> findByRepositoryIdOrderByCreatedAtDesc(Long repoId);

    List<ContributionRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @Query("SELECT cr FROM ContributionRequest cr WHERE cr.repository.owner.id = :ownerId ORDER BY cr.createdAt DESC")
    List<ContributionRequest> findByRepoOwnerId(@Param("ownerId") Long ownerId);

    Optional<ContributionRequest> findByRepositoryIdAndRequesterIdAndStatus(Long repoId, Long requesterId, RequestStatus status);
}
