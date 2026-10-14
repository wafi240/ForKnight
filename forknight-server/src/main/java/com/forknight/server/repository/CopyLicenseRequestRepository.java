package com.forknight.server.repository;

import com.forknight.server.model.CopyLicenseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CopyLicenseRequestRepository extends JpaRepository<CopyLicenseRequest, Long> {

    List<CopyLicenseRequest> findByRepositoryIdOrderByCreatedAtDesc(Long repoId);

    List<CopyLicenseRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @Query("SELECT clr FROM CopyLicenseRequest clr WHERE clr.repository.owner.id = :ownerId ORDER BY clr.createdAt DESC")
    List<CopyLicenseRequest> findByRepoOwnerId(@Param("ownerId") Long ownerId);
}
