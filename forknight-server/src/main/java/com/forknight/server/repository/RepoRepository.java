package com.forknight.server.repository;

import com.forknight.server.model.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepoRepository extends JpaRepository<Repository, Long> {

    List<Repository> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Page<Repository> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Repository r WHERE r.isPublic = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Repository> searchPublicRepos(@Param("query") String query, Pageable pageable);

    boolean existsByOwnerIdAndName(Long ownerId, String name);

    Optional<Repository> findByOwnerIdAndName(Long ownerId, String name);

    @Query("SELECT r FROM Repository r JOIN r.owner o WHERE o.username = :username AND r.name = :name")
    Optional<Repository> findByOwnerUsernameAndName(@Param("username") String username, @Param("name") String name);
}
