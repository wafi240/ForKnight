# ForKnight — Version Control & Code Marketplace

A desktop-based Version Control and Code Marketplace application built with Java 21, JavaFX, Spring Boot 3.2, and PostgreSQL.

---

## 🏛️ Project Architecture

The application is structured as a Maven multi-module project:

```
ForKnight/
├── pom.xml                                    # Parent POM (Aggregator & Dependency Management)
├── forknight-common/                          # Shared module (DTO records, Enums, JsonUtil)
├── forknight-server/                          # Spring Boot REST API (Port 8080)
│   ├── Auth & JWT Security (Access & Refresh tokens)
│   ├── User entity & Spring Data JPA repository
│   ├── Flyway DB migrations (PostgreSQL)
│   └── Dev profile support with in-memory H2 (PostgreSQL compatibility mode)
├── forknight-client/                          # JavaFX Desktop Client
│   ├── Modern Dark Theme (CSS)
│   ├── Login, Registration, and Main Dashboard FXML Views
│   ├── Async API client with background thread pools & UI dispatch
│   └── Session persistence via user preferences
├── setup-env.bat                              # Environment diagnostic & setup helper
├── build-all.bat                              # Project build script
├── run-server.bat                             # Server launch script
└── run-client.bat                             # Client desktop launch script
```

---

## 🚀 Phase 1 Implementation Summary

### 1. `forknight-common`
- **DTOs (Java 21 Records)**:
  - `LoginRequest(username, password)`
  - `RegisterRequest(username, email, password, displayName)`
  - `LoginResponse(userId, username, email, displayName, avatarUrl, role, accessToken, refreshToken)`
  - `UserDTO(id, username, email, displayName, avatarUrl, bio, role, createdAt)`
  - `ApiError(status, error, message, timestamp)` with factory method `ApiError.of(...)`
  - `PagedResponse<T>(content, page, size, totalElements, totalPages, last)`
- **Enums**: `UserRole` (`USER`, `ADMIN`, `MODERATOR`)
- **JSON Utility**: `JsonUtil` wrapping Jackson with Java 8/21 date-time (`jsr310`) module support.

### 2. `forknight-server`
- **Spring Boot 3.2.5** application entry (`ForKnightServerApp.java`).
- **Security & JWT**:
  - `JwtTokenProvider` using HMAC-SHA512 (`jjwt 0.12.5`).
  - `JwtAuthFilter` reading Bearer tokens and authenticating requests via `UserPrincipal`.
  - `SecurityConfig` configuring stateless session management and route protection.
  - BCrypt password hashing.
- **REST Endpoints**:
  - `POST /api/auth/register` — Create new account, returns tokens & user details (201 Created).
  - `POST /api/auth/login` — Authenticate credentials, returns tokens (200 OK).
  - `POST /api/auth/refresh` — Refresh expired access token with valid refresh token (200 OK).
- **Data Persistence**:
  - `User` JPA Entity mapped to `users` table.
  - `UserRepository` with query methods (`findByUsername`, `existsByUsername`, etc.).
  - Flyway migration script `V1__create_users_table.sql`.
  - Standalone `dev` profile with in-memory H2 for instant local development.

### 3. `forknight-client`
- **JavaFX 21** application entry (`ForKnightApp.java` and `Main.java` launcher).
- **Modern Dark UI**:
  - Dark GitHub/VS Code aesthetic (`theme-dark.css` and `components.css`).
  - Centered cards, custom input fields, smooth button hover animations, badges, and tabs.
- **Views & Controllers**:
  - `login.fxml` + `LoginController`: Username/password inputs, spinner, error messages, register link.
  - `register.fxml` + `RegisterController`: Username, email, display name, password confirmation, validation.
  - `dashboard.fxml` + `DashboardController`: User profile header with dynamic role badge, logout, "My Repos" & "Explore" tabs.
- **Concurrency & Networking**:
  - `ApiClient` backed by `java.net.http.HttpClient` on daemon thread pool (`ThreadPools.IO_POOL`).
  - UI updates marshaled safely to JavaFX Application Thread via `Platform.runLater()`.
  - Seamless session persistence in `SessionManager`.

---

## 🚀 Phase 2 Implementation Summary

### 1. Repository Management & JGit Integration
- **Database Schema**: `V2__create_repositories_and_secrets.sql` with full table constraints and indexes.
- **JGit Engine**:
  - Automatic Git repository initialization on disk (`Git.init()`) with initial `README.md` and commit.
  - File tree traversal (`TreeWalk` via `RevTree`) into hierarchical `FileNodeDTO`.
  - Commit history log retrieval (`CommitDTO`) with hash, author, timestamp, and message.
- **REST Endpoints**:
  - `POST /api/repos` — Initialize new repository on server.
  - `GET /api/repos` — Fetch user's own repositories.
  - `GET /api/repos/explore` — Search & browse public repositories with pagination.
  - `GET /api/repos/{id}/tree` — Read repository file hierarchy.
  - `GET /api/repos/{id}/files` — Read file contents.
  - `GET /api/repos/{id}/commits` — Read commit history.

### 2. Single-File Downloader (Bypassing Repo Cloning)
- **Problem Solved**: GitHub does not provide a native CLI command to download an isolated single file without cloning the entire repository.
- **Implementation**:
  - `GitHubApiService` queries the GitHub REST API (`/repos/{owner}/{repo}/contents/{path}`) with `Accept: application/vnd.github.raw` to stream the raw file directly.
  - Automatic fallback to raw CDN (`raw.githubusercontent.com/{owner}/{repo}/{branch}/{path}`) when unauthenticated.
  - `FileDownloadTask`: A JavaFX `Task<Path>` providing real-time progress updates, saving directly to local disk without creating a temporary local repository clone.

### 3. Secret Leakage Prevention & Scanner
- **Detection Engine (`SecretScanner`)**:
  - **Regex Rules (`SecretPattern`)**: Detects AWS Access Keys, AWS Secret Keys, GitHub PATs, Slack Tokens, Private Key blocks (RSA/OpenSSH/PGP), Database Connection URIs with credentials, and hardcoded passwords.
  - **Shannon Entropy Analysis**: Scans arbitrary string tokens for high randomness (>4.5 entropy) to detect unclassified hardcoded API keys.
  - **Data Protection**: Automatically masks detected credentials (`AKIA****************3XYZ`) to prevent leakage in scan reports or logs.
- **Persistence & API**:
  - `SecretScanResult` table and `SecretScanController` for scanning content or scanning entire repositories.
  - Pre-commit scanning endpoint (`POST /api/secrets/scan-content`) for blocking secret commits.

### 4. Interactive JavaFX UI Views
- `my_repos.fxml` + `MyReposController`: Card-based repository list, repository creation modal with license selector and visibility controls.
- `explore.fxml` + `ExploreController`: Search bar, pagination controls, and "+ Fast Single-File Download" modal.
- `repo_detail.fxml` + `RepoDetailController`:
  - **Files Tab**: TreeView explorer with custom icons (`📁`, `📄`), code editor preview, and "⬇ Download This File" button.
  - **Commits Tab**: Formatted commit timeline.
  - **Secret Scanner Tab**: "🛡 Run Secret Scanner" button with real-time analysis banner and findings table.
  - Dark GitHub/VS Code aesthetic (`theme-dark.css` and `components.css`).
  - Centered cards, custom input fields, smooth button hover animations, badges, and tabs.
- **Views & Controllers**:
  - `login.fxml` + `LoginController`: Username/password inputs, spinner, error messages, register link.
  - `register.fxml` + `RegisterController`: Username, email, display name, password confirmation, validation.
  - `dashboard.fxml` + `DashboardController`: User profile header with dynamic role badge, logout, "My Repos" & "Explore" tabs.
- **Concurrency & Networking**:
  - `ApiClient` backed by `java.net.http.HttpClient` on daemon thread pool (`ThreadPools.IO_POOL`).
  - UI updates marshaled safely to JavaFX Application Thread via `Platform.runLater()`.
  - Seamless session persistence in `SessionManager`.

---

## 🚀 Phase 3 Implementation Summary

### 1. Public Comments & Code Suggestions
- **Threaded Commenting Engine**: Supports top-level comments and nested replies on repositories.
- **Code Context Pinning**: Comments can be flagged as `is_suggestion` and optionally reference a specific `file_path` and `line_number`.
- **Database Schema**: `comments` table with hierarchical parent-child relationships (`ON DELETE CASCADE`).
- **REST Endpoints**:
  - `POST /api/comments` — Post a public comment or suggestion.
  - `GET /api/comments/repo/{repoId}` — Fetch threaded repository comments.

### 2. Contribution Access Requests & Collaborator Roles
- **Workflow**:
  1. Any user can request contributor access to a repository (`POST /api/contrib/requests`).
  2. The repository owner views pending requests in the **Requests Inbox**.
  3. The owner approves or denies the request with an assigned permission level (`READ`, `WRITE`, `ADMIN`).
  4. On approval, the user is added to `repo_collaborators` and immediately notified.
- **REST Endpoints**:
  - `POST /api/contrib/requests` — Submit contribution request.
  - `POST /api/contrib/requests/{id}/decision` — Approve / Deny request with role assignment.
  - `GET /api/contrib/incoming` — Fetch owner's incoming contribution requests.
  - `GET /api/contrib/my-requests` — Fetch user's submitted requests.
  - `GET /api/contrib/repo/{repoId}/collaborators` — List approved collaborators.

### 3. Copy / Fork Licensing Workflow (Free vs. Paid)
- **Problem Solved**: Code marketplaces and open-source ecosystems need a standardized mechanism for authors to grant free (open-source) or paid (commercial) fork/copy rights.
- **Workflow**:
  1. Requester submits a copy request describing intended usage (`POST /api/licensing/requests`).
  2. Owner can approve as **Free (Open Source)** or **Paid (Commercial License)** by setting a custom license fee in USD.
  3. Decision is recorded in `copy_license_requests` and the requester is notified.
- **REST Endpoints**:
  - `POST /api/licensing/requests` — Request copy permission.
  - `POST /api/licensing/requests/{id}/decision` — Owner decision (`APPROVED_FREE`, `APPROVED_PAID`, `DENIED`).
  - `GET /api/licensing/incoming` — Owner incoming licensing requests.
  - `GET /api/licensing/my-requests` — Requester status tracking.

### 4. Real-Time Notification System
- **Notification Engine**: Automatically alerts users for comments, replies, contribution requests, collaborator approvals, and licensing decisions.
- **Client Component (`NotificationBell`)**:
  - Displays a bell icon in the dashboard navigation header.
  - Red badge showing live unread count.
  - Interactive notification panel modal allowing users to read, dismiss, and "Mark all as read".

---

## 🚀 Phase 4 Implementation Summary

### 1. Interactive 3D Git Graph Visualization (`GitGraph3DView`)
- **Mathematical Layout Engine (`GitGraphService`)**:
  - Parses Git commit DAG using JGit RevWalk.
  - Computes 3D coordinates $(X, Y, Z)$ for commits based on branch depth and commit chronological index.
  - Generates directed 3D cylindrical connection edges from parent commits to child commits.
- **JavaFX 3D Hardware Accelerated SubScene**:
  - Built with `PerspectiveCamera`, ambient and directional lighting (`PointLight`).
  - Interactive **Orbit Controls**: Drag mouse with left-click to orbit around $(X, Y)$ axes, scroll wheel to zoom in/out with bounded limits.
  - **Click-to-Inspect**: Clicking on any commit sphere highlights the node and populates the **Commit Inspector** sidebar with full hash, branch, author, date, and commit message.
  - Smooth animation timeline to dynamically orient branches.

### 2. Code Snippet Marketplace
- **Database Schema**: `V4__create_marketplace_and_orders_tables.sql` providing `snippets`, `orders`, and `snippet_ratings` tables.
- **Storefront & Catalog (`MarketplaceController`)**:
  - Search code snippets by keyword, filter by programming language (Java, Python, JS, TS, Rust, Go, C++, SQL), and price tier (Free, <$10, <$50).
  - List snippets with pagination, star rating averages, review counts, sales counts, and author badges.
- **Publishing & Listing Engine**:
  - Interactive modal dialog to publish new algorithms or utility snippets with syntax-highlighted code editor, price setting, language tag, and descriptions.
- **Purchasing & Unlocking Flow**:
  - Immediate simulated checkout with idempotency checks and order tracking (`OrderRepository`).
  - Unlocked code viewer with one-click copy and rating review modal.
- **5-Star Community Review System**:
  - Submit ratings (1 to 5 stars) and reviews with instant average rating calculation.

---

## 🚀 Phase 5 Verification & Architecture Polish

### 1. Concurrency & UI Thread Safety
- Strict separation between daemon worker threads (`ThreadPools.IO_POOL`, `COMPUTE_POOL`) and the JavaFX Application Thread.
- All HTTP API calls (`ApiClient`, `CompletableFuture`) safely update UI components via `Platform.runLater()`.
- Thread-safe 3D graph scene graph assembly preventing any threading deadlocks or race conditions.

### 2. Dual-Mode Database Support
- Production profile connects to PostgreSQL with Flyway automated migrations (`V1` through `V5`).
- Standalone developer profile (`application-dev.yml`) runs embedded H2 database in PostgreSQL mode, enabling zero-config out-of-the-box local testing without installing Postgres.

---

## 🚀 Phase 6: Pull Requests, Git Command Palette & AI-Powered Code Reviewer

### 1. Interactive Git Command Palette
- **Access**: Launch via header `⚡ Git Palette` or shortcut.
- **Local Working Tree Operations**:
  - Live Git status reporting: current branch, clean working tree vs. modified/untracked files count.
  - **Commit**: Stage working tree (`git add .`) and commit with custom message and pre-commit checks.
  - **Branch Management**: Create new branches (`git branch <name>`) and switch branches (`git checkout <name>`).
  - **Stash**: Stash uncommitted changes (`git stash`).

### 2. Pull Request & Branch Merging Engine
- **Database Schema**: `V5__create_pull_requests_and_ai_reviews.sql` adding `pull_requests` and `ai_reviews` tables.
- **Workflow**:
  1. Open a pull request between any source and target branch (`POST /api/repos/{id}/prs`).
  2. Server calculates additions, deletions, and changed files via JGit `DiffFormatter`.
  3. Automatically schedules and attaches an initial AI code review to the PR.
  4. Repository owner can inspect the unified diff, review AI suggestions, and execute a merge (`POST /api/prs/{id}/merge`).

### 3. Syntax-Highlighted Code Diff Viewer (`DiffViewerComponent`)
- Color-coded unified diff viewer:
  - Additions highlighted in green (`+` line prefix, `#86efac` text, subtle background tint).
  - Deletions highlighted in red (`-` line prefix, `#fca5a5` text).
  - Chunk headers with line ranges (`@@ -1,4 +1,6 @@`) highlighted in blue.
  - Line number gutter matching original and new files.

### 4. AI-Powered Code Reviewer (`AiReviewService`)
- **Dual-Engine Review Pipeline**:
  - **Intelligent Heuristic / AST Static Analyzer**:
    - **Security**: Detects SQL injection concatenations, unescaped OS execution (`Runtime.exec`), and hardcoded credentials.
    - **Bugs & Reliability**: Detects unclosed I/O streams / sockets outside try-with-resources, swallowed exceptions (`catch (Exception e) {}`), and unsafe `Optional.get()`.
    - **Performance**: Flags repeated string concatenations (`+=`) in loops.
  - **External LLM Integration**: Optionally connects to OpenAI / Claude models when user provides an API key.
- **Actionable Reporting (`AiReviewCardComponent`)**:
  - Code Quality Score (1 to 10 scale).
  - Categorized findings with severity tags (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
  - One-click **"Copy Fix"** suggestions.
  - Standalone AI Reviewer tab for instant arbitrary code snippet inspections.

---

## 🛠️ Prerequisites & Setup

### Requirements
- **Java Development Kit (JDK)**: Version 21 (or 17+)
- **Maven**: Version 3.9+
- **PostgreSQL**: Version 15+ (Optional for quick testing; dev profile uses embedded H2)

### Automated Setup (Windows)
If you don't have JDK 21 or Maven installed yet:

```cmd
winget install Microsoft.OpenJDK.21
choco install maven -y
```

After installing, open a new command prompt and verify:
```cmd
java -version
mvn -version
```

---

## 🏃 Running the Application

### 1. Build the Modules
```cmd
build-all.bat
```
Or with Maven directly:
```cmd
mvn clean install -DskipTests
```

### 2. Start the Backend Server
```cmd
run-server.bat
```
Or directly:
```cmd
# Using Standalone In-Memory Database (No PostgreSQL required)
mvn -pl forknight-server spring-boot:run -Dspring-boot.run.profiles=dev

# Using Local PostgreSQL (requires database 'forknight' on port 5432)
mvn -pl forknight-server spring-boot:run
```

### 3. Start the JavaFX Desktop Client
In another terminal:
```cmd
run-client.bat
```
Or directly:
```cmd
mvn -pl forknight-client javafx:run
```
