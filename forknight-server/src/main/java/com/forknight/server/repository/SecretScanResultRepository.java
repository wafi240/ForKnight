package com.forknight.server.repository;

import com.forknight.server.model.SecretScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SecretScanResultRepository extends JpaRepository<SecretScanResult, Long> {

    List<SecretScanResult> findByRepositoryIdOrderByDetectedAtDesc(Long repoId);

    List<SecretScanResult> findByRepositoryIdAndIsResolvedFalse(Long repoId);

    long countByRepositoryIdAndIsResolvedFalse(Long repoId);
}
