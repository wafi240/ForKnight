package com.forknight.server.config;

import com.forknight.common.dto.contrib.ContribCreateRequest;
import com.forknight.common.dto.contrib.ContribDecisionRequest;
import com.forknight.common.dto.contrib.ContribRequestDTO;
import com.forknight.common.enums.RequestStatus;
import com.forknight.common.enums.AccessLevel;
import com.forknight.common.dto.auth.LoginRequest;
import com.forknight.common.dto.auth.LoginResponse;
import com.forknight.common.dto.auth.RegisterRequest;
import com.forknight.common.dto.repo.RepoCreateRequest;
import com.forknight.common.dto.comment.CommentCreateRequest;
import com.forknight.common.dto.market.SnippetCreateRequest;
import com.forknight.common.dto.market.RatingCreateRequest;
import com.forknight.common.dto.licensing.CopyLicenseCreateRequest;
import com.forknight.common.dto.licensing.CopyLicenseDecisionRequest;
import com.forknight.common.dto.pr.PRCreateRequest;
import com.forknight.common.enums.NotificationType;
import com.forknight.server.model.User;
import com.forknight.server.repository.UserRepository;
import com.forknight.server.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Arrays;
import java.util.stream.Stream;

@Component
public class DatabaseSeeder implements CommandLineRunner {

        private final AuthService authService;
    private final RepoService repoService;
    private final MarketplaceService marketplaceService;
    private final CommentService commentService;
    private final CopyLicenseService copyLicenseService;
    private final PullRequestService prService;
    private final NotificationService notificationService;
    private final ContribService contribService;
    private final UserRepository userRepository;

    public DatabaseSeeder(AuthService authService, RepoService repoService, MarketplaceService marketplaceService, 
                          CommentService commentService, CopyLicenseService copyLicenseService, PullRequestService prService, 
                          NotificationService notificationService, ContribService contribService, UserRepository userRepository) {
        this.authService = authService;
        this.repoService = repoService;
        this.marketplaceService = marketplaceService;
        this.commentService = commentService;
        this.copyLicenseService = copyLicenseService;
        this.prService = prService;
        this.notificationService = notificationService;
        this.contribService = contribService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if already seeded
            authService.login(new LoginRequest("Farhan12", "password"));
            System.out.println("Database already seeded with new demo data.");
        } catch (Exception e) {
            System.out.println("Seeding database with new ForKn8 demo data...");

            // 1. Create Users
            LoginResponse u1 = authService.register(new RegisterRequest("Farhan12", "farhan@gmail.com", "password", "Farhan"));
            LoginResponse u2 = authService.register(new RegisterRequest("Raju14", "raju@gmail.com", "password", "Raju"));
            LoginResponse u3 = authService.register(new RegisterRequest("Prantor06", "prantor@gmail.com", "password", "Prantor"));
            LoginResponse u4 = authService.register(new RegisterRequest("Rezwan13", "rezwan@gmail.com", "password", "Rezwan"));
            LoginResponse u5 = authService.register(new RegisterRequest("wafi1314", "wafi1314@gmail.com", "password", "Wafi"));

            User user1 = userRepository.findById(u1.userId()).get();
            User user2 = userRepository.findById(u2.userId()).get();
            User user3 = userRepository.findById(u3.userId()).get();
            User user4 = userRepository.findById(u4.userId()).get();
            User user5 = userRepository.findById(u5.userId()).get();

            user1.setBalance(new BigDecimal("100.00"));
            user5.setBalance(new BigDecimal("100.00"));
            userRepository.saveAll(Arrays.asList(user1, user5));

            // 2. Create Repositories
            var r1 = repoService.createRepository(u1.userId(), new RepoCreateRequest("NextGenAI", "Advanced AI models", true, "MIT", null, false, null));
            var r2 = repoService.createRepository(u1.userId(), new RepoCreateRequest("UI-Library", "React component library", true, "GPL", null, true, new BigDecimal("49.99")));
            var r3 = repoService.createRepository(u2.userId(), new RepoCreateRequest("Micro-Auth", "Microservice auth", true, "Apache 2.0", null, true, new BigDecimal("19.99")));
            var r4 = repoService.createRepository(u2.userId(), new RepoCreateRequest("Data-Viz", "D3.js wrappers", true, "MIT", null, false, null));
            var r5 = repoService.createRepository(u3.userId(), new RepoCreateRequest("Game-Engine", "2D Java Engine", true, "MIT", null, false, null));
            var r6 = repoService.createRepository(u3.userId(), new RepoCreateRequest("Physics-Sim", "Rigid body physics", true, "GPL", null, true, new BigDecimal("29.99")));
            var r7 = repoService.createRepository(u4.userId(), new RepoCreateRequest("Blockchain-Node", "Simple blockchain node implementation", true, "MIT", null, false, null));
            var r8 = repoService.createRepository(u4.userId(), new RepoCreateRequest("Crypto-Wallet", "Wallet UI", true, "MIT", null, false, null));
            var r9 = repoService.createRepository(u5.userId(), new RepoCreateRequest("Secure-Chat", "E2E encrypted chat", true, "Apache 2.0", null, true, new BigDecimal("99.99")));
            var r10 = repoService.createRepository(u5.userId(), new RepoCreateRequest("Dev-Tools", "CLI utilities", true, "MIT", null, false, null));
            var r11 = repoService.createRepository(u5.userId(), new RepoCreateRequest("Complex-Microservice", "A highly collaborative microservice", true, "MIT", null, false, null));
            var r12 = repoService.createRepository(u5.userId(), new RepoCreateRequest("Ultimate-Graph", "A massively parallel project showing a complex git graph", true, "MIT", null, false, null));

            // Populate Ultimate-Graph with contributions from Wafi, Farhan, Raju, Prantor, and Rezwan
            File ultimateRepoDir = new File(System.getProperty("user.home"), ".forknight/repos/wafi1314/Ultimate-Graph");
            seedUltimateGraphGitRepo(ultimateRepoDir);

            // 3. Create Marketplace Snippets
            var s1 = marketplaceService.createSnippet(u1.userId(), new SnippetCreateRequest("JWT Validator", "Validates JWT tokens", "function validate() {}", "JavaScript", new BigDecimal("4.99"), Arrays.asList("auth", "jwt")));
            var s2 = marketplaceService.createSnippet(u2.userId(), new SnippetCreateRequest("React Hook", "useFetch hook", "const useFetch = () => {}", "TypeScript", new BigDecimal("2.99"), Arrays.asList("react", "hooks")));
            var s3 = marketplaceService.createSnippet(u3.userId(), new SnippetCreateRequest("CSS Reset", "Modern CSS reset", "* { margin: 0; }", "CSS", new BigDecimal("0.00"), Arrays.asList("css", "web")));
            var s4 = marketplaceService.createSnippet(u4.userId(), new SnippetCreateRequest("Python Scraper", "BeautifulSoup scraper", "import bs4", "Python", new BigDecimal("9.99"), Arrays.asList("python", "scraping")));
            var s5 = marketplaceService.createSnippet(u5.userId(), new SnippetCreateRequest("Bash Script", "Server backup", "#!/bin/bash", "Bash", new BigDecimal("1.99"), Arrays.asList("bash", "devops")));
            var s6 = marketplaceService.createSnippet(u1.userId(), new SnippetCreateRequest("Docker Compose", "LEMP stack", "version: '3'", "YAML", new BigDecimal("5.99"), Arrays.asList("docker", "devops")));
            var s7 = marketplaceService.createSnippet(u2.userId(), new SnippetCreateRequest("SQL Query", "Complex join", "SELECT * FROM users", "SQL", new BigDecimal("3.99"), Arrays.asList("sql", "database")));
            var s8 = marketplaceService.createSnippet(u3.userId(), new SnippetCreateRequest("Java Utils", "String helpers", "public class StringUtils {}", "Java", new BigDecimal("2.99"), Arrays.asList("java", "utils")));
            var s9 = marketplaceService.createSnippet(u4.userId(), new SnippetCreateRequest("Rust Struct", "Memory safe struct", "struct Safe {}", "Rust", new BigDecimal("6.99"), Arrays.asList("rust", "memory")));
            var s10 = marketplaceService.createSnippet(u5.userId(), new SnippetCreateRequest("Go Routine", "Concurrency pattern", "go func() {}", "Go", new BigDecimal("4.99"), Arrays.asList("go", "concurrency")));
            var s11 = marketplaceService.createSnippet(u1.userId(), new SnippetCreateRequest("Swift View", "SwiftUI card", "struct CardView: View {}", "Swift", new BigDecimal("8.99"), Arrays.asList("swift", "ios")));
            var s12 = marketplaceService.createSnippet(u2.userId(), new SnippetCreateRequest("Kotlin Ext", "Kotlin extension functions", "fun String.toSlug() {}", "Kotlin", new BigDecimal("3.99"), Arrays.asList("kotlin", "android")));

            marketplaceService.rateSnippet(u2.userId(), s1.id(), new RatingCreateRequest(5, "Works perfectly!"));
            marketplaceService.rateSnippet(u3.userId(), s1.id(), new RatingCreateRequest(4, "Good but needs more docs."));
            marketplaceService.rateSnippet(u4.userId(), s2.id(), new RatingCreateRequest(5, "Saved me hours."));
            marketplaceService.rateSnippet(u5.userId(), s4.id(), new RatingCreateRequest(3, "A bit outdated."));
            marketplaceService.rateSnippet(u1.userId(), s5.id(), new RatingCreateRequest(5, "Essential script."));
            
            // Removed purchases as they require balance setup

            // 4. Copy License Requests
            var req1 = copyLicenseService.createRequest(u2.userId(), new CopyLicenseCreateRequest(r2.id(), "Need this for my startup"));
            var req2 = copyLicenseService.createRequest(u3.userId(), new CopyLicenseCreateRequest(r2.id(), "Want to fork for an open source project"));
            var req3 = copyLicenseService.createRequest(u4.userId(), new CopyLicenseCreateRequest(r3.id(), "Integrating with my backend"));
            var req4 = copyLicenseService.createRequest(u5.userId(), new CopyLicenseCreateRequest(r3.id(), "Educational purposes"));
            var req5 = copyLicenseService.createRequest(u1.userId(), new CopyLicenseCreateRequest(r6.id(), "Using in my game"));
            var req6 = copyLicenseService.createRequest(u2.userId(), new CopyLicenseCreateRequest(r6.id(), "Testing physics engine"));
            var req7 = copyLicenseService.createRequest(u3.userId(), new CopyLicenseCreateRequest(r9.id(), "Building a secure app"));
            var req8 = copyLicenseService.createRequest(u4.userId(), new CopyLicenseCreateRequest(r9.id(), "Reviewing encryption"));
            var req9 = copyLicenseService.createRequest(u1.userId(), new CopyLicenseCreateRequest(r9.id(), "Forking for a client"));
            var req10 = copyLicenseService.createRequest(u2.userId(), new CopyLicenseCreateRequest(r2.id(), "Testing another use case"));

            copyLicenseService.decideRequest(u1.userId(), req1.id(), new CopyLicenseDecisionRequest(com.forknight.common.enums.CopyLicenseStatus.APPROVED_FREE, null, "Approved!"));
            copyLicenseService.decideRequest(u1.userId(), req2.id(), new CopyLicenseDecisionRequest(com.forknight.common.enums.CopyLicenseStatus.DENIED, null, "Not allowing forks right now."));
            copyLicenseService.decideRequest(u2.userId(), req3.id(), new CopyLicenseDecisionRequest(com.forknight.common.enums.CopyLicenseStatus.APPROVED_PAID, new BigDecimal("50.00"), "Sure, go ahead."));
            copyLicenseService.decideRequest(u5.userId(), req7.id(), new CopyLicenseDecisionRequest(com.forknight.common.enums.CopyLicenseStatus.APPROVED_FREE, null, "Enjoy the chat app base."));

            // 5. Comments & Suggestions
            commentService.createComment(u2.userId(), new CommentCreateRequest(r1.id(), null, "Great AI project!", false, null, null));
            commentService.createComment(u3.userId(), new CommentCreateRequest(r1.id(), null, "Looking forward to updates.", false, null, null));
            commentService.createComment(u4.userId(), new CommentCreateRequest(r2.id(), null, "Can you add a DatePicker?", false, null, null));
            commentService.createComment(u5.userId(), new CommentCreateRequest(r3.id(), null, "Is this production ready?", false, null, null));
            var c1 = commentService.createComment(u1.userId(), new CommentCreateRequest(r4.id(), null, "Awesome visualizations.", false, null, null));
            commentService.createComment(u2.userId(), new CommentCreateRequest(r4.id(), c1.id(), "I agree, very clean.", false, null, null));
            commentService.createComment(u3.userId(), new CommentCreateRequest(r5.id(), null, "What rendering engine does this use?", false, null, null));
            
            commentService.createComment(u2.userId(), new CommentCreateRequest(r1.id(), null, "Consider using a newer ML framework.", true, "src/main.py", 10));
            commentService.createComment(u4.userId(), new CommentCreateRequest(r5.id(), null, "Optimize this loop for better FPS.", true, "src/GameLoop.java", 45));
            commentService.createComment(u1.userId(), new CommentCreateRequest(r9.id(), null, "Use AES-GCM here instead of CBC.", true, "src/crypto.js", 120));

            // 6. Pull Requests
            prService.createPullRequest(u2.userId(), new PRCreateRequest(r1.id(), "Update dependencies", "Bumped versions in requirements.txt", "deps-update", "main"));
            prService.createPullRequest(u3.userId(), new PRCreateRequest(r2.id(), "Add DatePicker component", "Implemented the requested DatePicker", "feature/datepicker", "main"));
            prService.createPullRequest(u4.userId(), new PRCreateRequest(r3.id(), "Fix JWT bug", "Resolved token expiration issue", "bugfix/jwt", "main"));
            prService.createPullRequest(u5.userId(), new PRCreateRequest(r5.id(), "Add sprite sheet support", "Can now load spritesheets", "feature/sprites", "main"));
            prService.createPullRequest(u1.userId(), new PRCreateRequest(r9.id(), "Upgrade encryption algorithm", "Switched to AES-GCM", "security/aes-gcm", "main"));

            // 7. Notifications
            notificationService.createNotification(user1, NotificationType.SYSTEM, "Welcome to ForKn8", "Enjoy the platform!", null, null);
            notificationService.createNotification(user2, NotificationType.SYSTEM, "Welcome to ForKn8", "Enjoy the platform!", null, null);
            notificationService.createNotification(user3, NotificationType.SYSTEM, "Welcome to ForKn8", "Enjoy the platform!", null, null);
            notificationService.createNotification(user4, NotificationType.SYSTEM, "Welcome to ForKn8", "Enjoy the platform!", null, null);
            notificationService.createNotification(user5, NotificationType.SYSTEM, "Welcome to ForKn8", "Enjoy the platform!", null, null);

                        // 8. Contributions
            ContribRequestDTO cr1 = contribService.createRequest(u5.userId(), new ContribCreateRequest(r1.id(), "I would like to help with UI-Library!"));
            contribService.decideRequest(u1.userId(), cr1.id(), new ContribDecisionRequest(RequestStatus.APPROVED, AccessLevel.WRITE, "Welcome aboard, Wafi!"));

            System.out.println("Database seeding complete.");
        }
    }

    private void seedUltimateGraphGitRepo(File repoDir) {
        try {
            if (repoDir.exists()) {
                try (Stream<Path> walk = Files.walk(repoDir.toPath())) {
                    walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                }
            }
            repoDir.mkdirs();

            try (Git git = Git.init().setDirectory(repoDir).call()) {
                PersonIdent wafi = new PersonIdent("Wafi", "wafi1314@gmail.com");
                PersonIdent farhan = new PersonIdent("Farhan", "farhan12@gmail.com");
                PersonIdent raju = new PersonIdent("Raju", "raju14@gmail.com");
                PersonIdent prantor = new PersonIdent("Prantor", "prantor06@gmail.com");
                PersonIdent rezwan = new PersonIdent("Rezwan", "rezwan13@gmail.com");

                // Initial commit on main by Wafi
                File readme = new File(repoDir, "readme.md");
                Files.writeString(readme.toPath(), "Project Ultimate\n");
                git.add().addFilepattern("readme.md").call();
                git.commit().setMessage("Initial commit by Wafi").setAuthor(wafi).setCommitter(wafi).call();

                // Branch 1: feature/ui by Farhan
                git.checkout().setCreateBranch(true).setName("feature/ui").call();
                File uiFile = new File(repoDir, "ui.js");
                Files.writeString(uiFile.toPath(), "console.log('UI Layer');\n");
                git.add().addFilepattern("ui.js").call();
                git.commit().setMessage("Start UI layer").setAuthor(farhan).setCommitter(farhan).call();

                // Branch 2: feature/backend by Raju
                git.checkout().setName("main").call();
                git.checkout().setCreateBranch(true).setName("feature/backend").call();
                File serverFile = new File(repoDir, "server.js");
                Files.writeString(serverFile.toPath(), "console.log('Server Layer');\n");
                git.add().addFilepattern("server.js").call();
                git.commit().setMessage("Start Backend server").setAuthor(raju).setCommitter(raju).call();

                // Wafi merges feature/ui into main
                git.checkout().setName("main").call();
                git.merge().include(git.getRepository().findRef("feature/ui")).setMessage("Merge UI into main").call();

                // Branch 3: bugfix/critical by Prantor
                git.checkout().setCreateBranch(true).setName("bugfix/critical").call();
                Files.writeString(uiFile.toPath(), "console.log('UI Layer Fixed');\n");
                git.add().addFilepattern("ui.js").call();
                git.commit().setMessage("Fix UI critical bug").setAuthor(prantor).setCommitter(prantor).call();

                // Raju continues on feature/backend
                git.checkout().setName("feature/backend").call();
                Files.writeString(serverFile.toPath(), "console.log('Server REST API');\n");
                git.add().addFilepattern("server.js").call();
                git.commit().setMessage("Add REST API").setAuthor(raju).setCommitter(raju).call();

                // Branch 4: experiment/ai by Rezwan
                git.checkout().setName("main").call();
                git.checkout().setCreateBranch(true).setName("experiment/ai").call();
                File aiFile = new File(repoDir, "ai.py");
                Files.writeString(aiFile.toPath(), "import tensorflow as tf\n");
                git.add().addFilepattern("ai.py").call();
                git.commit().setMessage("Experiment with AI models").setAuthor(rezwan).setCommitter(rezwan).call();

                // Wafi merges bugfix/critical
                git.checkout().setName("main").call();
                git.merge().include(git.getRepository().findRef("bugfix/critical")).setMessage("Merge bugfix/critical").call();

                // Raju branches feature/backend-sync
                git.checkout().setName("bugfix/critical").call();
                git.checkout().setCreateBranch(true).setName("feature/backend-sync").call();
                File syncFile = new File(repoDir, "sync.js");
                Files.writeString(syncFile.toPath(), "console.log('Sync');\n");
                git.add().addFilepattern("sync.js").call();
                git.commit().setMessage("Sync backend with UI fixes").setAuthor(raju).setCommitter(raju).call();

                // Rezwan continues experiment/ai
                git.checkout().setName("experiment/ai").call();
                Files.writeString(aiFile.toPath(), "import tensorflow as tf\nprint('training')\n");
                git.add().addFilepattern("ai.py").call();
                git.commit().setMessage("Train initial AI model").setAuthor(rezwan).setCommitter(rezwan).call();

                // Farhan creates feature/dashboard
                git.checkout().setName("main").call();
                git.checkout().setCreateBranch(true).setName("feature/dashboard").call();
                File dashFile = new File(repoDir, "dashboard.js");
                Files.writeString(dashFile.toPath(), "console.log('Dashboard');\n");
                git.add().addFilepattern("dashboard.js").call();
                git.commit().setMessage("Create dashboard component").setAuthor(farhan).setCommitter(farhan).call();

                // Wafi merges everything into main
                git.checkout().setName("main").call();
                git.merge().include(git.getRepository().findRef("experiment/ai")).setMessage("Merge AI experiment").call();
                git.merge().include(git.getRepository().findRef("feature/dashboard")).setMessage("Merge feature/dashboard").call();
                git.merge().include(git.getRepository().findRef("feature/backend-sync")).setMessage("Merge backend sync branch").call();

                // Final release commit by Wafi
                Files.writeString(readme.toPath(), "Project Ultimate v1.0 Ready\n");
                git.add().addFilepattern("readme.md").call();
                git.commit().setMessage("Prepare for v1.0 release").setAuthor(wafi).setCommitter(wafi).call();
            }
        } catch (Exception e) {
            System.err.println("Failed to build Ultimate-Graph topology: " + e.getMessage());
        }
    }
}




