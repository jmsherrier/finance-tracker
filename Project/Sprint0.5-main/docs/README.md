Team Members
- Austin
- Shreya
- Yitian Yu
- Bogdan Perlroth
- Joseph Sherrier
- Nehal Singhal

---

## Deliverables and Evidence (Sprint 0.5 & Sprint 1)

This document records the sprint artifacts and recent repository updates (MVVM template added, merge conflicts resolved, and .gitignore updated to ignore Gradle caches).

### 1) Use Case Diagram

Files (placeholders):
- `docs/Use_Case.png`  (use case diagram placeholder)

Note: The use case diagram includes an additional actor categorized as **External Auditor** (Supporting Actor) — read-only access to exports / audit logs.

### 2) Use Cases (6+ defined)

Primary use cases (brief):
1. Create Account — Prospective User (support: Firebase Auth)
2. Login — Registered User (support: Firebase Auth)
3. Log Expense — Registered User (support: Local DB / Firebase)
4. Create Budget — Registered User (support: Budget repository)
5. Join/Create Saving Circle — Registered User (support: Group service)
6. Chat with ChatBot — Registered User (support: ChatBot service)

Additional: Export Reports — Admin / External Auditor (support: DB)

### 3) MVVM Architecture Screenshot

Files (placeholder):
- `docs/MVVM.png` (add MVVM diagram here)

NOTE: A minimal MVVM template was added to the project (see section 4). This includes model and ViewModel skeletons and wiring in `MainActivity` to demonstrate ViewModel usage.

### 4) Classes and XML files (categorized)

The inventory below reflects current files (newly added items are shown). If a category is empty it is marked as a placeholder.

Models:
- `app/src/main/java/com/example/sprintproject/model/User.java` — Model (POJO)
- `app/src/main/java/com/example/sprintproject/model/UserRepository.java` — Model / Repository (placeholder for auth persistence)

ViewModels:
- `app/src/main/java/com/example/sprintproject/viewmodel/AuthViewModel.java` — ViewModel (exposes LiveData for user + errors)

Views (Activities / Fragments / layout XML):
- `app/src/main/java/com/example/sprintproject/view/MainActivity.java` — View (Activity). Updated to observe `AuthViewModel` and handles Start/Quit actions.
- `app/src/main/java/com/example/sprintproject/view/DashboardActivity.java` — View (Activity)
- `app/src/main/java/com/example/sprintproject/view/DashboardFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/ExpenseLogFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/BudgetFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/SavingsCirclesFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/ChatbotFragment.java` — View (Fragment)

Layout XML (Views):
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/activity_dashboard.xml`
- `app/src/main/res/layout/activity_expense_log.xml`
- `app/src/main/res/layout/activity_budgets.xml`
- `app/src/main/res/layout/activity_savings_circles.xml`
- `app/src/main/res/layout/activity_chatbot.xml`
- `app/src/main/res/menu/bottom_nav_menu.xml`

Resource XML:
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `app/src/main/res/xml/backup_rules.xml`

Notes:
- MVVM skeleton files were added: `model/User`, `model/UserRepository`, `viewmodel/AuthViewModel` and `MainActivity` was wired to demonstrate usage. These are templates and should be extended to use Firebase/local DB as needed.

### 5) Paragraph: MVVM Architecture (1 paragraph)

The Model-View-ViewModel (MVVM) architecture separates responsibilities into three layers: Models encapsulate application data and business logic; Views (Activities and Fragments) handle UI rendering and user interactions and remain thin; ViewModels mediate between Models and Views by exposing observable state (LiveData) and commands so Views can react to data changes without direct coupling to data sources. This pattern improves testability, simplifies lifecycle handling (ViewModels survive configuration changes), and keeps UI code separated from persistence and business rules.

### 6) Screenshot: Project Management Tool (Sprint 0.5 and Sprint 1)

Files (placeholders):
- `docs/Jira.png` — placeholder screenshot for sprint boards

### 7) Paragraph: Project Management Tool (1 paragraph)

We used a project management board (Jira / GitHub Projects) to track user stories and sprint tasks for Sprint 0.5 and Sprint 1. Each ticket contains the title, assignee, acceptance criteria, and links to related PRs and commits; the board was used to prioritize the sprint backlog, track progress during daily standups, and document completed work during the sprint review.

### 8) Paragraph: Scrum Meetings for Sprint 1 (1 paragraph)

During Sprint 1 the team held sprint planning to define work for the sprint, daily standups to report progress and blockers, and a sprint review and retrospective at the end to demo work and capture improvements. Standups were short (10–15 minutes) and focused on what was done, what will be done, and any impediments.

---

## Repository housekeeping & recent changes

- `.gitignore` updated to ignore `.gradle/` caches; previously tracked Gradle cache files were removed from the index.
- CheckStyle configured (see `config/checkstyle/checkstyle.xml`) and can be run with `./gradlew checkstyle`.
- MVVM template files added (models, repository, ViewModel) and `MainActivity` wired to `AuthViewModel` as an example.
- Merge conflicts in `MainActivity.java` were resolved (kept joseph branch changes) and merged with `main`; branch `joseph` pushed to origin.

## Files to add (if you want full evidence)

Add or replace these images in `docs/` to complete the deliverables:
- `docs/Use_Case.png` (use case diagram)
- `docs/MVVM.png` (architecture diagram)
- `docs/Jira.png` (project management screenshot)

## How to run CheckStyle locally

```powershell
cd "c:\Users\jmshe\Documents\GitHub\CS2340_F25_Team3\Project\Sprint0.5-main"
.\gradlew.bat checkstyle
```

View the HTML report at `app/build/reports/checkstyle/checkstyle.html` after the task completes.

---
