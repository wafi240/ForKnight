package com.forknight.server.service;

import com.forknight.common.dto.diff.DiffEntryDTO;
import com.forknight.common.dto.diff.DiffLineDTO;
import com.forknight.server.model.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiffService {

    private static final Logger log = LoggerFactory.getLogger(DiffService.class);

    public List<DiffEntryDTO> computeDiff(Repository repo, String sourceBranch, String targetBranch) {
        File repoDir = new File(repo.getDiskPath());
        if (!repoDir.exists()) {
            return List.of();
        }

        try (Git git = Git.open(repoDir)) {
            org.eclipse.jgit.lib.Repository repository = git.getRepository();

            ObjectId sourceHead = repository.resolve(sourceBranch);
            ObjectId targetHead = repository.resolve(targetBranch);

            if (sourceHead == null || targetHead == null) {
                // If specific branch ref isn't resolved directly, try HEAD
                if (sourceHead == null) sourceHead = repository.resolve("HEAD");
                if (targetHead == null) targetHead = repository.resolve("HEAD~1");
            }

            if (sourceHead == null) {
                return List.of();
            }

            AbstractTreeIterator oldTreeParser = null;
            if (targetHead != null) {
                oldTreeParser = prepareTreeParser(repository, targetHead);
            }
            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, sourceHead);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DiffFormatter formatter = new DiffFormatter(out)) {
                formatter.setRepository(repository);
                List<DiffEntry> diffs = formatter.scan(oldTreeParser, newTreeParser);

                List<DiffEntryDTO> results = new ArrayList<>();
                for (DiffEntry diff : diffs) {
                    out.reset();
                    formatter.format(diff);
                    String patch = out.toString(StandardCharsets.UTF_8);

                    List<DiffLineDTO> lines = parsePatchLines(patch);
                    int additions = (int) lines.stream().filter(l -> l.type() == DiffLineDTO.LineType.ADD).count();
                    int deletions = (int) lines.stream().filter(l -> l.type() == DiffLineDTO.LineType.DELETE).count();

                    results.add(new DiffEntryDTO(
                            diff.getOldPath(),
                            diff.getNewPath(),
                            diff.getChangeType().name(),
                            additions,
                            deletions,
                            patch,
                            lines
                    ));
                }
                return results;
            }
        } catch (Exception e) {
            log.warn("Could not compute JGit diff for {}: {}", repo.getName(), e.getMessage());
            return List.of();
        }
    }

    public List<DiffLineDTO> parsePatchLines(String patch) {
        List<DiffLineDTO> lines = new ArrayList<>();
        if (patch == null || patch.isBlank()) return lines;

        String[] rawLines = patch.split("\r?\n");
        int oldLine = 0;
        int newLine = 0;

        for (String line : rawLines) {
            if (line.startsWith("@@")) {
                lines.add(new DiffLineDTO(DiffLineDTO.LineType.HEADER, line, null, null));
                // Parse @@ -1,4 +1,6 @@
                try {
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        String oldPart = parts[1].replace("-", "");
                        String newPart = parts[2].replace("+", "");
                        oldLine = Integer.parseInt(oldPart.split(",")[0]);
                        newLine = Integer.parseInt(newPart.split(",")[0]);
                    }
                } catch (Exception ignored) {}
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                lines.add(new DiffLineDTO(DiffLineDTO.LineType.ADD, line.substring(1), null, newLine++));
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                lines.add(new DiffLineDTO(DiffLineDTO.LineType.DELETE, line.substring(1), oldLine++, null));
            } else if (!line.startsWith("diff ") && !line.startsWith("index ") && !line.startsWith("---") && !line.startsWith("+++")) {
                lines.add(new DiffLineDTO(DiffLineDTO.LineType.CONTEXT, line.startsWith(" ") ? line.substring(1) : line, oldLine++, newLine++));
            }
        }
        return lines;
    }

    private AbstractTreeIterator prepareTreeParser(org.eclipse.jgit.lib.Repository repository, ObjectId commitId) throws Exception {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(commitId);
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }
}
