package com.forknight.common.dto.repo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data transfer object representing a file or directory node in a repository tree.
 */
public record FileNodeDTO(
        @JsonProperty("name") String name,
        @JsonProperty("path") String path,
        @JsonProperty("isDirectory") boolean isDirectory,
        @JsonProperty("size") long size,
        @JsonProperty("children") List<FileNodeDTO> children
) {
    public static FileNodeDTO file(String name, String path, long size) {
        return new FileNodeDTO(name, path, false, size, List.of());
    }

    public static FileNodeDTO directory(String name, String path, List<FileNodeDTO> children) {
        return new FileNodeDTO(name, path, true, 0L, children);
    }
}
