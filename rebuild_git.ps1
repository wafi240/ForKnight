Start-Sleep -Seconds 22
cd C:\Users\HP\.forknight\repos\wafi1314\Complex-Microservice
Remove-Item -Path * -Recurse -Force
Remove-Item -Path .git -Recurse -Force -ErrorAction SilentlyContinue

git init
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
echo "Initial setup" > setup.txt
git add .
git commit -m "Initialize project structure"

# Create a dev branch
git checkout -b dev
echo "Dev config" > dev.txt
git add .
git commit -m "Add development configuration"

# Create feature 1 from dev (Farhan's work)
git checkout -b feature/auth
git config user.name "Farhan"
git config user.email "farhan12@gmail.com"
echo "Auth module" > auth.java
git add .
git commit -m "Implement basic JWT authentication"
echo "Auth fixes" >> auth.java
git add .
git commit -m "Fix token expiration bug"

# Back to dev, another feature (Raju's work)
git checkout dev
git checkout -b feature/database
git config user.name "Raju"
git config user.email "raju14@gmail.com"
echo "DB Connection" > db.java
git add .
git commit -m "Setup database connection pool"
echo "Optimizations" >> db.java
git add .
git commit -m "Optimize query performance"

# Merge feature/auth into dev
git checkout dev
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
git merge feature/auth --no-ff -m "Merge pull request #1 from feature/auth"

# Hotfix from main (Prantor's work)
git checkout main
git checkout -b hotfix/readme-typo
git config user.name "Prantor"
git config user.email "prantor06@gmail.com"
echo "Fixed typo" >> README.md
git add .
git commit -m "Fix typo in documentation"
git checkout main
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
git merge hotfix/readme-typo --no-ff -m "Merge hotfix/readme-typo"

# Merge dev into main
git checkout main
git merge dev --no-ff -m "Release v1.1.0: Merge dev into main"

# Continue on feature/database
git checkout feature/database
git config user.name "Raju"
git config user.email "raju14@gmail.com"
echo "Indexing" >> db.java
git add .
git commit -m "Add database indexes"

# Back to main, Wafi does some more work
git checkout main
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
echo "Docker" > Dockerfile
git add .
git commit -m "Add Dockerfile"

# Finally merge database into dev, then dev into main
git checkout dev
git merge feature/database --no-ff -m "Merge feature/database"
git checkout main
git merge dev --no-ff -m "Release v1.2.0: Merge dev into main"

# Leave a dangling branch for visual interest
git checkout -b experiment/ai-module
git config user.name "Rezwan"
git config user.email "rezwan13@gmail.com"
echo "AI stuff" > ai.py
git add .
git commit -m "Start experimental AI integration"

git checkout main
cd C:\Users\HP\.forknight\repos\wafi1314\Ultimate-Graph
Remove-Item -Path * -Recurse -Force
Remove-Item -Path .git -Recurse -Force -ErrorAction SilentlyContinue

git init
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
echo "Project Ultimate" > readme.md
git add .
git commit -m "Initial commit by Wafi"

# Branch 1: Farhan's UI
git checkout -b feature/ui
git config user.name "Farhan"
git config user.email "farhan12@gmail.com"
echo "UI Layer" > ui.js
git add .
git commit -m "Start UI layer"

# Branch 2: Raju's Backend
git checkout main
git checkout -b feature/backend
git config user.name "Raju"
git config user.email "raju14@gmail.com"
echo "Backend Layer" > server.js
git add .
git commit -m "Start Backend server"

# Wafi merges UI
git checkout main
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
git merge feature/ui --no-ff -m "Merge UI into main"

# Branch 3: Prantor's Bugfix
git checkout -b bugfix/critical
git config user.name "Prantor"
git config user.email "prantor06@gmail.com"
echo "Bug fixed" >> ui.js
git add .
git commit -m "Fix UI critical bug"

# Raju continues on backend
git checkout feature/backend
git config user.name "Raju"
git config user.email "raju14@gmail.com"
echo "API" >> server.js
git add .
git commit -m "Add REST API"

# Branch 4: Rezwan's AI
git checkout main
git checkout -b experiment/ai
git config user.name "Rezwan"
git config user.email "rezwan13@gmail.com"
echo "import tensorflow" > ai.py
git add .
git commit -m "Experiment with AI models"

# Wafi merges bugfix
git checkout main
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
git merge bugfix/critical --no-ff -m "Merge bugfix/critical"

# Raju branches off Prantor's bugfix for some reason
git checkout bugfix/critical
git checkout -b feature/backend-sync
git config user.name "Raju"
git config user.email "raju14@gmail.com"
echo "Sync" >> server.js
git add .
git commit -m "Sync backend with UI fixes"

# Wafi merges Backend
git checkout main
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
git merge feature/backend --no-ff -m "Merge feature/backend"

# Rezwan continues AI
git checkout experiment/ai
git config user.name "Rezwan"
git config user.email "rezwan13@gmail.com"
echo "train_model()" >> ai.py
git add .
git commit -m "Train initial AI model"

# Farhan creates a new component
git checkout main
git checkout -b feature/dashboard
git config user.name "Farhan"
git config user.email "farhan12@gmail.com"
echo "Dashboard" > dashboard.js
git add .
git commit -m "Create dashboard component"

# Wafi merges everything together
git checkout main
git config user.name "Wafi"
git config user.email "wafi1314@gmail.com"
git merge experiment/ai --no-ff -m "Merge AI experiment"
git merge feature/dashboard --no-ff -m "Merge feature/dashboard"
git merge feature/backend-sync --no-ff -m "Merge backend sync branch"

# Final polish by Wafi
echo "Production ready" >> readme.md
git add .
git commit -m "Prepare for v1.0 release"

