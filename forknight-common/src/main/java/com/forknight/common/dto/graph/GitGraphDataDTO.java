package com.forknight.common.dto.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data transfer object containing full 3D graph DAG data for a repository.
 */
public record GitGraphDataDTO(
        @JsonProperty("repoId") Long repoId,
        @JsonProperty("nodes") List<CommitNode3D> nodes,
        @JsonProperty("edges") List<Edge3D> edges
) {}
