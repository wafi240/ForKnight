# ⚔️ ForKnight

### An Interactive Developer Platform for Modern Version Control

**ForKnight** is a modern developer platform that combines **version control, interactive Git visualization, collaboration, marketplace features, AI-powered tools, and security scanning** into a single environment.

It is designed to make Git-based development more **visual, interactive, secure, and accessible**.

---

## ✨ Key Features

### 🔀 Version Control

* Repository management
* Branch management
* Commit history
* Merge tracking
* Git operations through an integrated interface
* Repository exploration

### 🌌 Interactive Git Visualization

* Interactive **2D Git Graph**
* Interactive **3D Git Graph**
* Commit and branch relationships
* Merge visualization
* Zoom, pan, and rotation
* Detailed commit inspection

### 🛒 Developer Marketplace

A dedicated marketplace for discovering and extending the platform with:

* Developer tools
* Extensions
* Plugins
* Utilities
* Development resources

### 🤖 AI-Powered Tools

An integrated AI module for developer assistance, including:

* Code analysis
* Repository understanding
* Commit analysis
* AI-assisted development workflows

### 🔐 Secret Scanner

Detect potentially exposed sensitive information such as:

* API keys
* Access tokens
* Credentials
* Secret configuration values

### 🔄 Pull Requests & Collaboration

Support for collaborative development workflows through:

* Pull requests
* Repository collaboration
* Access levels
* PR status tracking

### 📊 Repository Insights

Explore repository activity and understand:

* Commits
* Branches
* Contributors
* Project history
* Repository structure

---

# 🏗️ Architecture

ForKnight follows a **multi-layer client-server architecture** consisting of a JavaFX desktop client, Spring Boot server, shared common module, and persistence layer.

```text
┌───────────────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                          │
│                      forknight-client                                         │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │ JavaFX 21 Desktop GUI (FXML & CSS)                                      │  │
│  └───────────────────────────────┬─────────────────────────────────────────┘  │
│                                  │                                            │
│  ┌───────────────────────────────▼─────────────────────────────────────────┐  │
│  │ Custom Views: GitGraph3D • DiffViewer • AiCard                         │  │
│  └───────────────────────────────┬─────────────────────────────────────────┘  │
│                                  │                                            │
│  ┌───────────────────────────────▼─────────────────────────────────────────┐  │
│  │ HTTP REST Client (HttpClient & CompletableFuture)                      │  │
│  └───────────────────────────────┬─────────────────────────────────────────┘  │
└──────────────────────────────────┼────────────────────────────────────────────┘
                                   │
                              JSON / HTTP REST
                                   │
                                   ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                         SERVER LAYER                                           │
│                      forknight-server                                          │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │ Spring Security + JWT Authentication Filter                             │  │
│  └───────────────────────────────┬─────────────────────────────────────────┘  │
│                                  │                                            │
│  ┌───────────────────────────────▼─────────────────────────────────────────┐  │
│  │ REST Controllers: Repository • Marketplace • PR • Secret               │  │
│  └───────────────────────────────┬─────────────────────────────────────────┘  │
│                                  │                                            │
│  ┌───────────────────────────────▼─────────────────────────────────────────┐  │
│  │ Business Logic: RepoService • AiService                                │  │
│  └──────────────┬────────────────┬──────────────────────┬──────────────────┘  │
│                 │                │                      │                     │
│        ┌────────▼────────┐ ┌─────▼────────────┐ ┌─────▼──────────────┐       │
│        │ JGit Engine     │ │ Secret Scanner   │ │ Spring Data JPA    │       │
│        │ Embedded VCS    │ │ Engine           │ │ / Hibernate        │       │
│        └─────────────────┘ └──────────────────┘ └─────────┬──────────┘       │
└───────────────────────────────────────────────────────────┼───────────────────┘
                                                            │
                 ┌──────────────────────────────────────────┴──────────────┐
                 │                                                         │
┌────────────────▼──────────────────┐                 ┌────────────────────▼─────┐
│ COMMON LAYER                     │                 │ PERSISTENCE LAYER          │
│ forknight-common                 │                 │                            │
│                                  │                 │ ┌────────────────────────┐ │
│ ┌──────────────┐ ┌─────────────┐ │                 │ │ H2 Database            │ │
│ │ JsonUtil &   │ │ DTOs        │ │                 │ │ In-Memory / File-based │ │
│ │ Data Contracts│ │ RepoDTO     │ │                 │ └────────────────────────┘ │
│ └──────────────┘ │ Transaction │ │                 │                            │
│                  │ DTO, etc.   │ │                 │ ┌────────────────────────┐ │
│ ┌──────────────┐ └─────────────┘ │                 │ │ Disk Storage            │ │
│ │ Enums        │                 │                 │ │ .forknight/repos/      │ │
│ │ AccessLevel  │                 │                 │ └────────────────────────┘ │
│ │ PRStatus     │                 │                 │                            │
│ └──────────────┘                 │                 └────────────────────────────┘
└──────────────────────────────────┘
```

---

# 🧩 Main Components

### Client — `forknight-client`

The desktop application is built with **JavaFX 21**, using:

* FXML
* CSS
* Custom JavaFX views
* `HttpClient`
* `CompletableFuture`

Important custom views include:

* `GitGraph3D`
* `DiffViewer`
* `AiCard`

The client communicates with the server through **JSON-based HTTP REST APIs**.

---

### Server — `forknight-server`

The backend is built around a Spring-based architecture.

It provides:

* JWT authentication
* Spring Security
* REST APIs
* Repository services
* Marketplace services
* Pull request functionality
* Secret scanning
* AI services

The main business logic is organized into services such as:

* `RepoService`
* `AiService`

---

### 🔧 Git Engine

ForKnight uses **JGit** as its embedded Git/VCS engine.

This allows the server to work directly with Git repositories and perform repository-related operations without depending entirely on external Git commands.

---

### 🔐 Security Engine

The **Secret Scanner Engine** analyzes repository content for potentially exposed secrets.

Authentication is handled through:

**Spring Security + JWT**

---

### 📦 Common Layer — `forknight-common`

The common module contains shared structures used across the application.

It includes:

* JSON utilities
* Data contracts
* DTOs
* Repository DTOs
* Transaction DTOs
* Shared enums

Examples:

```text
AccessLevel
PRStatus
RepoDTO
TransactionDTO
```

This helps maintain consistent data communication between different parts of the platform.

---

### 💾 Persistence Layer

ForKnight uses **Spring Data JPA / Hibernate** for database operations.

The project supports:

* **H2 Database**
* In-memory/file-based database storage
* Disk-based repository storage

Repositories are stored under:

```text
.forknight/repos/
```

---

# 🛠️ Technology Stack

| Layer            | Technologies                 |
| ---------------- | ---------------------------- |
| Desktop Client   | JavaFX 21, FXML, CSS         |
| Backend          | Spring Boot, Spring Security |
| Authentication   | JWT                          |
| Git Engine       | JGit                         |
| Database         | H2                           |
| ORM              | JPA / Hibernate              |
| Communication    | HTTP REST / JSON             |
| Async Processing | CompletableFuture            |
| Visualization    | JavaFX 2D / 3D               |
| AI               | AI Integration               |

---

# 🎯 Project Goals

ForKnight aims to provide a single environment where developers can:

**🔀 Control** — Manage Git repositories and development history.

**🌌 Visualize** — Explore Git history through interactive 2D and 3D graphs.

**🤖 Assist** — Use AI-powered developer tools.

**🔐 Protect** — Detect potentially exposed secrets.

**🛒 Extend** — Discover tools through the developer marketplace.

**🤝 Collaborate** — Work with branches, pull requests, and repository access.

---

# 👨‍💻 Developer

**Samun Sadab Wafi**
**Roll:** 2307003
**Department:** Computer Science & Engineering
**KUET**

---

## ⚔️ ForKnight

> **Code. Control. Visualize. Create.**
