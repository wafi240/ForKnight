package com.forknight.server.controller;

import com.forknight.common.dto.PagedResponse;
import com.forknight.common.dto.repo.CommitDTO;
import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.FileNodeDTO;
import com.forknight.common.dto.repo.RepoCreateRequest;
import com.forknight.common.dto.repo.RepoDTO;
import com.forknight.server.security.UserPrincipal;
import com.forknight.server.service.RepoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repos")
public class RepoController {

    private final RepoService repoService;
    private final com.forknight.server.service.GitGraphService gitGraphService;

    public RepoController(RepoService repoService, com.forknight.server.service.GitGraphService gitGraphService) {
        this.repoService = repoService;
        this.gitGraphService = gitGraphService;
    }

    @PostMapping
    public ResponseEntity<RepoDTO> createRepo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody RepoCreateRequest request
    ) {
        RepoDTO repo = repoService.createRepository(userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo);
    }

    @GetMapping
    public ResponseEntity<List<RepoDTO>> getMyRepos(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<RepoDTO> repos = repoService.getRepositoriesForUser(userPrincipal.id());
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/explore")
    public ResponseEntity<PagedResponse<RepoDTO>> exploreRepos(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Page<RepoDTO> paged = repoService.getPublicRepositories(search, PageRequest.of(page, size));
        PagedResponse<RepoDTO> response = new PagedResponse<>(
                paged.getContent(),
                paged.getNumber(),
                paged.getSize(),
                paged.getTotalElements(),
                paged.getTotalPages(),
                paged.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepoDTO> getRepoById(@PathVariable Long id) {
        return ResponseEntity.ok(repoService.getRepositoryById(id));
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<List<FileNodeDTO>> getFileTree(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id) {
        return ResponseEntity.ok(repoService.getFileTree(id));
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<FileContentDTO> getFileContent(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id,
            @RequestParam String path
    ) {
        return ResponseEntity.ok(repoService.getFileContent(id, path));
    }

    @GetMapping("/{id}/commits")
    public ResponseEntity<List<CommitDTO>> getCommitHistory(@PathVariable Long id) {
        return ResponseEntity.ok(repoService.getCommitHistory(id));
    }

    @GetMapping("/{id}/graph-3d")
    public ResponseEntity<com.forknight.common.dto.graph.GitGraphDataDTO> getGraph3D(@PathVariable Long id) {
        return ResponseEntity.ok(gitGraphService.getGraph3D(id));
    }

    @GetMapping("/{id}/git/status")
    public ResponseEntity<com.forknight.common.dto.repo.GitStatusDTO> getGitStatus(@PathVariable Long id) {
        return ResponseEntity.ok(repoService.getRepoGitStatus(id));
    }

    @PostMapping("/{id}/git/branch")
    public ResponseEntity<com.forknight.common.dto.repo.GitStatusDTO> createBranch(
            @PathVariable Long id,
            @RequestBody com.forknight.common.dto.repo.GitCommandRequest req) {
        return ResponseEntity.ok(repoService.createBranch(id, req.branchName()));
    }

    @PostMapping("/{id}/git/checkout")
    public ResponseEntity<com.forknight.common.dto.repo.GitStatusDTO> checkoutBranch(
            @PathVariable Long id,
            @RequestBody com.forknight.common.dto.repo.GitCommandRequest req) {
        return ResponseEntity.ok(repoService.checkoutBranch(id, req.branchName()));
    }

    @PostMapping("/{id}/git/commit")
    public ResponseEntity<CommitDTO> commitChanges(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody com.forknight.common.dto.repo.GitCommandRequest req) {
        return ResponseEntity.ok(repoService.commitLocalChanges(id, user.getId(), req.commitMessage()));
    }

    @PostMapping("/{id}/git/stash")
    public ResponseEntity<com.forknight.common.dto.repo.GitStatusDTO> stashChanges(@PathVariable Long id) {
        return ResponseEntity.ok(repoService.stashLocalChanges(id));
    }

    @PostMapping("/{id}/files/upload")
    public ResponseEntity<Void> uploadFile(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id,
            @RequestBody com.forknight.common.dto.repo.FileUploadRequest request) {
        repoService.uploadFile(user.getId(), id, request);
        return ResponseEntity.ok().build();
    }
}

