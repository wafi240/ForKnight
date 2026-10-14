package com.forknight.server.repository;

import com.forknight.server.model.RepoCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RepoCollaboratorRepository extends JpaRepository<RepoCollaborator, Long> {

    List<RepoCollaborator> findByRepositoryId(Long repoId);

    Optional<RepoCollaborator> findByRepositoryIdAndUserId(Long repoId, Long userId);

    boolean existsByRepositoryIdAndUserId(Long repoId, Long userId);
}
