package com.forknight.server.repository;

import com.forknight.server.model.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SnippetRepository extends JpaRepository<Snippet, Long> {

    List<Snippet> findByAuthorIdAndIsActiveTrueOrderByCreatedAtDesc(Long authorId);

    Page<Snippet> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT s FROM Snippet s WHERE s.isActive = true AND " +
           "(:language IS NULL OR LOWER(s.language) = LOWER(:language)) AND " +
           "(:maxPrice IS NULL OR s.price <= :maxPrice) AND " +
           "(:search IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.tags) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Snippet> searchSnippets(
            @Param("language") String language,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("search") String search,
            Pageable pageable
    );
}
