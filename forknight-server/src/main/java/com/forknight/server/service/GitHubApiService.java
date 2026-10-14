package com.forknight.server.service;

import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.SingleFileDownloadRequest;
import com.forknight.server.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class GitHubApiService {

    private static final Logger log = LoggerFactory.getLogger(GitHubApiService.class);
    private final HttpClient httpClient;

    public GitHubApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Fetches the raw content of a single file from a remote GitHub repository
     * without cloning the repository.
     */
    public FileContentDTO fetchSingleFile(SingleFileDownloadRequest request) {
        String owner = request.owner();
        String repo = request.repo();
        String branch = (request.branch() != null && !request.branch().isBlank()) ? request.branch() : "main";
        String filePath = request.filePath();

        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        // Method 1: Using GitHub Contents API with raw media type
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                owner, repo, filePath, branch);

        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/vnd.github.raw")
                    .header("User-Agent", "ForKnight-App")
                    .timeout(Duration.ofSeconds(30))
                    .GET();

            if (request.githubToken() != null && !request.githubToken().isBlank()) {
                reqBuilder.header("Authorization", "Bearer " + request.githubToken());
            }

            HttpResponse<byte[]> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                byte[] bytes = response.body();
                boolean isBinary = isBinaryContent(bytes);
                String content = isBinary ? "" : new String(bytes, StandardCharsets.UTF_8);
                String fileName = filePath.contains("/") ? filePath.substring(filePath.lastIndexOf('/') + 1) : filePath;

                return new FileContentDTO(filePath, fileName, content, bytes.length, isBinary);
            }

            log.warn("GitHub API fetch returned status {}. Trying raw fallback...", response.statusCode());

            // Method 2: Fallback to raw.githubusercontent.com
            String rawUrl = String.format("https://raw.githubusercontent.com/%s/%s/%s/%s",
                    owner, repo, branch, filePath);

            HttpRequest rawRequest = HttpRequest.newBuilder()
                    .uri(URI.create(rawUrl))
                    .header("User-Agent", "ForKnight-App")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<byte[]> rawResponse = httpClient.send(rawRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (rawResponse.statusCode() == 200) {
                byte[] bytes = rawResponse.body();
                boolean isBinary = isBinaryContent(bytes);
                String content = isBinary ? "" : new String(bytes, StandardCharsets.UTF_8);
                String fileName = filePath.contains("/") ? filePath.substring(filePath.lastIndexOf('/') + 1) : filePath;

                return new FileContentDTO(filePath, fileName, content, bytes.length, isBinary);
            }

            throw new BadRequestException("Failed to fetch file from GitHub. HTTP Status: " + rawResponse.statusCode());

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching single file from GitHub", e);
            throw new RuntimeException("Error communicating with GitHub API: " + e.getMessage(), e);
        }
    }

    private boolean isBinaryContent(byte[] bytes) {
        int checkLength = Math.min(bytes.length, 1024);
        for (int i = 0; i < checkLength; i++) {
            byte b = bytes[i];
            if (b == 0) {
                return true;
            }
        }
        return false;
    }
}
