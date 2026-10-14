package com.forknight.server.service;

import com.forknight.common.dto.graph.CommitNode3D;
import com.forknight.common.dto.graph.Edge3D;
import com.forknight.common.dto.graph.GitGraphDataDTO;
import com.forknight.server.exception.ResourceNotFoundException;
import com.forknight.server.model.Repository;
import com.forknight.server.repository.RepoRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class GitGraphService {

    private static final Logger log = LoggerFactory.getLogger(GitGraphService.class);
    private final RepoRepository repoRepository;

    private static final String[] BRANCH_COLORS = {
            "#10b981", // Emerald (main)
            "#3b82f6", // Blue
            "#8b5cf6", // Purple
            "#f59e0b", // Amber
            "#ec4899", // Pink
            "#06b6d4", // Cyan
            "#f97316"  // Orange
    };

    public GitGraphService(RepoRepository repoRepository) {
        this.repoRepository = repoRepository;
    }

    /**
     * Calculates 3D DAG coordinates (x, y, z) and branch edges for all commits in repository.
     */
    public GitGraphDataDTO getGraph3D(Long repoId) {
        Repository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        File gitDir = new File(repo.getDiskPath(), ".git");
        if (!gitDir.exists()) {
            return new GitGraphDataDTO(repoId, List.of(), List.of());
        }

        List<CommitNode3D> nodes = new ArrayList<>();
        List<Edge3D> edges = new ArrayList<>();

        try (Git git = Git.open(new File(repo.getDiskPath()))) {
            // Find all branch references
            Map<String, String> commitToBranch = new HashMap<>();
            List<Ref> branches = git.branchList().call();
            for (Ref b : branches) {
                String branchName = b.getName().replace("refs/heads/", "");
                if (b.getObjectId() != null) {
                    commitToBranch.put(b.getObjectId().getName(), branchName);
                }
            }

            Iterable<RevCommit> commits = git.log().all().call();
            List<RevCommit> commitList = new ArrayList<>();
            for (RevCommit c : commits) {
                commitList.add(c);
            }

            // Sort chronologically (oldest first for x-axis sequence)
            commitList.sort(Comparator.comparingInt(RevCommit::getCommitTime));

            Map<String, Integer> branchLanes = new HashMap<>();
            branchLanes.put("main", 0);
            branchLanes.put("master", 0);
            int nextLane = 1;

            double spacingX = 40.0;
            double spacingY = 30.0;
            double spacingZ = 20.0;

            for (int i = 0; i < commitList.size(); i++) {
                RevCommit rc = commitList.get(i);
                String hash = rc.getName();
                String shortHash = hash.substring(0, Math.min(7, hash.length()));

                String branchName = commitToBranch.getOrDefault(hash, "main");
                if (!branchLanes.containsKey(branchName)) {
                    branchLanes.put(branchName, (nextLane % 2 == 1) ? (nextLane / 2 + 1) : -(nextLane / 2));
                    nextLane++;
                }

                int lane = branchLanes.getOrDefault(branchName, 0);
                int colorIndex = Math.abs(lane) % BRANCH_COLORS.length;
                String color = BRANCH_COLORS[colorIndex];

                // Coordinates:
                // X: Chronological progression
                // Y: Branch lane spacing
                // Z: Elevation based on merge status or branch offset
                double x = i * spacingX;
                double y = lane * spacingY;
                double z = (rc.getParentCount() > 1) ? 25.0 : (Math.sin(i * 0.5) * 5.0);

                List<String> parents = new ArrayList<>();
                for (RevCommit p : rc.getParents()) {
                    parents.add(p.getName());
                    edges.add(new Edge3D(p.getName(), hash, color));
                }

                PersonIdent author = rc.getAuthorIdent();
                Instant timestamp = author != null ? author.getWhen().toInstant() : Instant.now();

                nodes.add(new CommitNode3D(
                        hash,
                        shortHash,
                        rc.getShortMessage(),
                        author != null ? author.getName() : "Unknown",
                        author != null ? author.getEmailAddress() : "",
                        branchName,
                        color,
                        x,
                        y,
                        z,
                        parents,
                        timestamp
                ));
            }

        } catch (Exception e) {
            log.error("Failed to construct 3D commit graph for repo {}", repoId, e);
        }

        return new GitGraphDataDTO(repoId, nodes, edges);
    }
}
