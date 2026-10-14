package com.forknight.common.dto.graph;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a directed 3D edge between two commits in the DAG.
 */
public record Edge3D(
        @JsonProperty("fromHash") String fromHash,
        @JsonProperty("toHash") String toHash,
        @JsonProperty("colorHex") String colorHex
) {}
