package com.forknight.server.service;

import com.forknight.common.dto.repo.CommitDTO;
import com.forknight.common.dto.repo.FileContentDTO;
import com.forknight.common.dto.repo.FileNodeDTO;
import com.forknight.common.dto.repo.GitStatusDTO;
import com.forknight.server.repository.RepoCollaboratorRepository;
import com.forknight.server.model.RepoCollaborator;
import com.forknight.common.enums.AccessLevel;
import com.forknight.common.dto.repo.RepoCreateRequest;
import com.forknight.common.dto.repo.RepoDTO;
import com.forknight.server.exception.BadRequestException;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.Repository;
import com.forknight.server.model.User;
import com.forknight.server.repository.RepoRepository;
import com.forknight.server.repository.UserRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RepoService {

    private static final Logger log = LoggerFactory.getLogger(RepoService.class);
        private final RepoRepository repoRepository;
    private final UserRepository userRepository;
    private final RepoCollaboratorRepository repoCollaboratorRepository;

    public RepoService(RepoRepository repoRepository, UserRepository userRepository, RepoCollaboratorRepository repoCollaboratorRepository) {
        this.repoRepository = repoRepository;
        this.userRepository = userRepository;
        this.repoCollaboratorRepository = repoCollaboratorRepository;
    }

    public RepoDTO createRepository(Long userId, RepoCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (repoRepository.existsByOwnerIdAndName(userId, request.name())) {
            throw new BadRequestException("You already have a repository named: " + request.name());
        }

        // Setup disk path: <userHome>/.forknight/repos/<username>/<repoName>
        String userHome = System.getProperty("user.home");
        Path repoPath = Path.of(userHome, ".forknight", "repos", user.getUsername(), request.name());

        try {
            Files.createDirectories(repoPath);

            // Initialize Git repository via JGit
            try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
                // Create initial README
                File readme = new File(repoPath.toFile(), "README.md");
                String initialContent = String.format("# %s\n\n%s\n",
                        request.name(),
                        request.description() != null ? request.description() : "A ForKnight repository.");
                Files.writeString(readme.toPath(), initialContent);

                git.add().addFilepattern("README.md").call();
                git.commit()
                        .setMessage("Initial commit")
                        .setAuthor(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(), user.getEmail())
                        .setCommitter(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(), user.getEmail())
                        .call();
            }
        } catch (Exception e) {
            log.error("Failed to initialize git repository on disk", e);
            throw new RuntimeException("Failed to initialize git repository: " + e.getMessage(), e);
        }

        Repository repo = new Repository();
        repo.setOwner(user);
        repo.setName(request.name());
        repo.setDescription(request.description());
        repo.setPublic(request.isPublic());
        repo.setLicenseType(request.licenseType() != null ? request.licenseType() : "MIT");
        repo.setDefaultBranch("main");
        repo.setDiskPath(repoPath.toAbsolutePath().toString());
        repo.setGithubUrl(request.githubUrl());
        repo.setForSale(request.isForSale());
        repo.setSalePrice(request.salePrice());

        Repository saved = repoRepository.save(repo);
        return saved.toDTO();
    }

    @Transactional(readOnly = true)
    public List<RepoDTO> getRepositoriesForUser(Long userId) {
        return repoRepository.findByOwnerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(Repository::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<RepoDTO> getPublicRepositories(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return repoRepository.searchPublicRepos(search.trim(), pageable).map(Repository::toDTO);
        }
        return repoRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable).map(Repository::toDTO);
    }

    @Transactional(readOnly = true)
    public RepoDTO getRepositoryById(Long repoId) {
        return repoRepository.findById(repoId)
                .map(Repository::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));
    }

    /**
     * Traverses the repository tree using JGit and returns a hierarchical file tree.
     */
    @Transactional(readOnly = true)
    public List<FileNodeDTO> getFileTree(Long repoId) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        File gitDir = new File(repo.getDiskPath(), ".git");
        if (!gitDir.exists()) {
            return List.of();
        }

        List<FileNodeDTO> fileList = new ArrayList<>();

        try (Git git = Git.open(new File(repo.getDiskPath()))) {
            org.eclipse.jgit.lib.Repository repository = git.getRepository();
            Ref head = repository.exactRef("HEAD");
            if (head == null || head.getObjectId() == null) {
                return List.of();
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(head.getObjectId());
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(false);

                    while (treeWalk.next()) {
                        String name = treeWalk.getNameString();
                        String path = treeWalk.getPathString();
                        boolean isSubtree = treeWalk.isSubtree();

                        if (isSubtree) {
                            fileList.add(FileNodeDTO.directory(name, path, List.of()));
                        } else {
                            ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
                            fileList.add(FileNodeDTO.file(name, path, loader.getSize()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to read file tree for repo {}", repoId, e);
            throw new RuntimeException("Error reading repository tree: " + e.getMessage(), e);
        }

        return fileList;
    }

    /**
     * Reads a specific file's content from the repository.
     */
    @Transactional(readOnly = true)
    public FileContentDTO getFileContent(Long repoId, String filePath) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        File file = new File(repo.getDiskPath(), filePath);
        if (!file.exists() || !file.isFile()) {
            throw new ResourceNotFoundException("File", "path", filePath);
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            boolean isBinary = false;
            for (int i = 0; i < Math.min(bytes.length, 1024); i++) {
                if (bytes[i] == 0) {
                    isBinary = true;
                    break;
                }
            }
            String content = isBinary ? "" : new String(bytes, StandardCharsets.UTF_8);
            return new FileContentDTO(filePath, file.getName(), content, bytes.length, isBinary);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves commit history for the repository.
     */
    @Transactional(readOnly = true)
    public List<CommitDTO> getCommitHistory(Long repoId) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        File gitDir = new File(repo.getDiskPath(), ".git");
        if (!gitDir.exists()) {
            return List.of();
        }

        List<CommitDTO> commits = new ArrayList<>();

        try (Git git = Git.open(new File(repo.getDiskPath()))) {
            Iterable<RevCommit> revCommits = git.log().call();
            for (RevCommit rc : revCommits) {
                PersonIdent author = rc.getAuthorIdent();
                List<String> parents = new ArrayList<>();
                for (RevCommit p : rc.getParents()) {
                    parents.add(p.getName());
                }

                commits.add(new CommitDTO(
                        rc.getName(),
                        rc.getName().substring(0, Math.min(7, rc.getName().length())),
                        rc.getShortMessage(),
                        author != null ? author.getName() : "Unknown",
                        author != null ? author.getEmailAddress() : "",
                        parents,
                        author != null ? author.getWhen().toInstant() : Instant.now()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to read commit history for repo {}", repoId, e);
        }

        return commits;
    }

    /**
     * Inspects Git status (current branch, modified, untracked, staged files, branches).
     */
    @Transactional(readOnly = true)
    public GitStatusDTO getRepoGitStatus(Long repoId) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        File repoDir = new File(repo.getDiskPath());
        if (!repoDir.exists()) {
            return new GitStatusDTO("main", true, List.of(), List.of(), List.of(), List.of("main"));
        }

        try (Git git = Git.open(repoDir)) {
            String currentBranch = git.getRepository().getBranch();
            Status status = git.status().call();

            List<String> branches = new ArrayList<>();
            for (Ref ref : git.branchList().call()) {
                branches.add(ref.getName().replace("refs/heads/", ""));
            }
            if (branches.isEmpty()) branches.add("main");

            return new GitStatusDTO(
                    currentBranch != null ? currentBranch : "main",
                    status.isClean(),
                    new ArrayList<>(status.getModified()),
                    new ArrayList<>(status.getUntracked()),
                    new ArrayList<>(status.getChanged()),
                    branches
            );
        } catch (Exception e) {
            log.error("Failed to read git status for repo {}", repoId, e);
            return new GitStatusDTO("main", true, List.of(), List.of(), List.of(), List.of("main"));
        }
    }

    /**
     * Creates a new Git branch.
     */
    public GitStatusDTO createBranch(Long repoId, String branchName) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        try (Git git = Git.open(new File(repo.getDiskPath()))) {
            git.branchCreate().setName(branchName).call();
            return getRepoGitStatus(repoId);
        } catch (Exception e) {
            throw new BadRequestException("Could not create branch: " + e.getMessage());
        }
    }

    /**
     * Switches to an existing Git branch.
     */
    public GitStatusDTO checkoutBranch(Long repoId, String branchName) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        try (Git git = Git.open(new File(repo.getDiskPath()))) {
            git.checkout().setName(branchName).call();
            return getRepoGitStatus(repoId);
        } catch (Exception e) {
            throw new BadRequestException("Could not checkout branch: " + e.getMessage());
        }
    }

    /**
     * Stages all changes and creates a new Git commit.
     */
    public CommitDTO commitLocalChanges(Long repoId, Long userId, String message) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try (Git git = Git.open(new File(repo.getDiskPath()))) {
            git.add().addFilepattern(".").call();
            RevCommit commit = git.commit()
                    .setMessage(message != null && !message.isBlank() ? message : "Update files via ForKnight")
                    .setAuthor(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(), user.getEmail())
                    .setCommitter(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(), user.getEmail())
                    .call();

            return new CommitDTO(
                    commit.getName(),
                    commit.getName().substring(0, Math.min(7, commit.getName().length())),
                    commit.getShortMessage(),
                    user.getUsername(),
                    user.getEmail(),
                    List.of(),
                    Instant.now()
            );
        } catch (Exception e) {
            throw new BadRequestException("Commit failed: " + e.getMessage());
        }
    }

    /**
     * Stashes uncommitted working tree changes.
     */
    public GitStatusDTO stashLocalChanges(Long repoId) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        try (Git git = Git.open(new File(repo.getDiskPath()))) {
            git.stashCreate().call();
            return getRepoGitStatus(repoId);
        } catch (Exception e) {
            throw new BadRequestException("Stash failed: " + e.getMessage());
        }
    }
    public void uploadFile(Long userId, Long repoId, com.forknight.common.dto.repo.FileUploadRequest request) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));
        if (!repo.getOwner().getId().equals(userId)) {
            boolean hasWriteAccess = repoCollaboratorRepository.findByRepositoryIdAndUserId(repoId, userId)
                    .map(c -> c.getAccessLevel() == AccessLevel.WRITE || c.getAccessLevel() == AccessLevel.ADMIN)
                    .orElse(false);
            if (!hasWriteAccess) {
                throw new BadRequestException("You do not have permission to upload files to this repository.");
            }
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try {
            java.nio.file.Path repoPath = java.nio.file.Path.of(repo.getDiskPath());
            java.nio.file.Path targetFilePath = repoPath.resolve(request.path()).normalize();
            
            // Security check: ensure target is inside repo directory
            if (!targetFilePath.startsWith(repoPath)) {
                throw new BadRequestException("Invalid file path: " + request.path());
            }
            
            // Create parent directories if they don't exist
            if (targetFilePath.getParent() != null) {
                java.nio.file.Files.createDirectories(targetFilePath.getParent());
            }
            
            // Write decoded content
            byte[] fileBytes = java.util.Base64.getDecoder().decode(request.base64Content());
            java.nio.file.Files.write(targetFilePath, fileBytes);

            // JGit stage and commit so it is recorded in HEAD and visible in the file tree
            File repoDir = repoPath.toFile();
            File gitDir = new File(repoDir, ".git");
            if (!gitDir.exists()) {
                try (Git git = Git.init().setDirectory(repoDir).call()) {
                    // initialized
                }
            }
            try (Git git = Git.open(repoDir)) {
                git.add().addFilepattern(".").call();
                PersonIdent author = new PersonIdent(
                        user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                        user.getEmail() != null ? user.getEmail() : user.getUsername() + "@gmail.com"
                );
                git.commit()
                   .setMessage("Upload file: " + request.path())
                   .setAuthor(author)
                   .setCommitter(author)
                   .call();
            }
            log.info("File {} uploaded and committed to repo {} by user {}", request.path(), repoId, userId);
        } catch (Exception e) {
            log.error("File upload failed for repo {}", repoId, e);
            throw new BadRequestException("File upload failed: " + e.getMessage());
        }
    }
}



