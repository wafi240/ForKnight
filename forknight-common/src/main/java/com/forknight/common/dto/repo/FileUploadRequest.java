package com.forknight.common.dto.repo;

public record FileUploadRequest(
        String path,
        String base64Content
) {
}
