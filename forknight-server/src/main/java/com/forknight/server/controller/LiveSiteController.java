package com.forknight.server.controller;

import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.Repository;
import com.forknight.server.repository.RepoRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/sites")
public class LiveSiteController {

    private final RepoRepository repoRepository;

    public LiveSiteController(RepoRepository repoRepository) {
        this.repoRepository = repoRepository;
    }

    @GetMapping("/{username}/{repoName}/**")
    public ResponseEntity<Resource> serveSiteFile(
            @PathVariable String username,
            @PathVariable String repoName,
            HttpServletRequest request) {

        Repository repo = repoRepository.findByOwnerUsernameAndName(username, repoName)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "name", repoName));

        // Security: Ensure repo is public or purchased (for simplicity of live sites, they must be public)
        if (!repo.isPublic()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Extract the remaining path (the file path inside the repo)
        String requestUrl = request.getRequestURL().toString();
        String basePath = "/sites/" + username + "/" + repoName;
        String filePath = requestUrl.substring(requestUrl.indexOf(basePath) + basePath.length());
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        if (filePath.isEmpty()) {
            filePath = "index.html"; // Default to index.html
        }

        try {
            Path repoRoot = Paths.get(repo.getDiskPath()).toRealPath();
            Path requestedFile = repoRoot.resolve(filePath).toRealPath();

            // Security: Prevent Directory Traversal Attacks!
            if (!requestedFile.startsWith(repoRoot)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            File file = requestedFile.toFile();
            if (!file.exists() || file.isDirectory()) {
                // If it's a directory, try to serve index.html inside it
                if (file.isDirectory()) {
                    File indexFile = new File(file, "index.html");
                    if (indexFile.exists()) {
                        file = indexFile;
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } else {
                    return ResponseEntity.notFound().build();
                }
            }

            Resource resource = new FileSystemResource(file);
            String contentType = determineContentType(file.getName());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".html")) return MediaType.TEXT_HTML_VALUE;
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".json")) return MediaType.APPLICATION_JSON_VALUE;
        return MediaType.TEXT_PLAIN_VALUE;
    }
}
