Team Members
 Austin
 Shreya
 Yitian Yu
 Bogdan Perlroth
 Joseph Sherrier
 Nehal Singhal

---

## Deliverables and Evidence (Sprint 0.5 & Sprint 1)

### 1) Use Case Diagram

`docs/Use_Case.png`.

Note: Use Case Diagram has an additional actor that has been correctly categorized: **External Auditor** (Supporting Actor). The External Auditor is a supporting actor that can view exported reports and audit logs but cannot modify user data.

### 2) Use Cases (6+ defined)

Below are six primary use cases with primary and supporting actors and a brief description of the interaction:

1. User: Create Account (Primary actor: Prospective User; Supporting: Firebase Auth)
	- The prospective user enters registration details; the system validates data and creates an account using Firebase Authentication.

2. User: Login (Primary actor: Registered User; Supporting: Firebase Auth)
	- Registered user supplies credentials; the system authenticates and navigates to the Home screen.

3. User: Log Expense (Primary actor: Registered User; Supporting: Local DB / Firebase)
	- User opens Expense Log, presses "+", provides expense details, and the expense is saved to storage.

4. User: Create Budget (Primary actor: Registered User; Supporting: Budget Repository)
	- User creates a budget item, sets limits and time period; the system stores and enforces budget alerts.

5. User: Join / Create Saving Circle (Primary actor: Registered User; Supporting: Payment/Group Service)
	- User creates or joins a savings circle, inviting other users; the system tracks contributions and schedules payouts.

6. User: Consult ChatBot (Primary actor: Registered User; Supporting: ChatBot Service)
	- User asks finance-related questions; the ChatBot returns guidance and links to app features.

Additional: Admin: Export Reports (Primary actor: External Auditor/Admin; Supporting: DB)
	- Admin or External Auditor requests exports; the system compiles and delivers reports (read-only access for External Auditor).

### 3) MVVM Architecture Screenshot

Placeholder: Add MVVM architecture screenshot at `docs/mvvm_architecture.png`.

### 4) Classes and XML files (categorized)

Below is an inventory of the Java/Kotlin classes and XML resources in the project and their categorization as Model, View, or ViewModel. If a category has no current files, a placeholder entry is left for future content.

Models (currently none - placeholders):
- (none yet) — placeholder for `model/*.java` or `model/*.kt`

ViewModels (currently none - placeholders):
- (none yet) — placeholder for `viewmodel/*.java` or `viewmodel/*.kt`

Views (Activities / Fragments / layout XML):
- `app/src/main/java/com/example/sprintproject/view/MainActivity.java` — View (Activity)
- `app/src/main/java/com/example/sprintproject/view/DashboardActivity.java` — View (Activity)
- `app/src/main/java/com/example/sprintproject/view/DashboardFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/ExpenseLogFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/BudgetFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/SavingsCirclesFragment.java` — View (Fragment)
- `app/src/main/java/com/example/sprintproject/view/ChatbotFragment.java` — View (Fragment)
- `app/src/test/java/com/example/sprintproject/ExampleUnitTest.java` — Test (not part of MVVM core)
- `app/src/androidTest/java/com/example/sprintproject/ExampleInstrumentedTest.java` — Test (not part of MVVM core)

Layout XML (Views):
- `app/src/main/res/layout/activity_main.xml` — View (layout)
- `app/src/main/res/layout/activity_dashboard.xml` — View (layout)
- `app/src/main/res/layout/activity_expense_log.xml` — View (layout)
- `app/src/main/res/layout/activity_budgets.xml` — View (layout)
- `app/src/main/res/layout/activity_savings_circles.xml` — View (layout)
- `app/src/main/res/layout/activity_chatbot.xml` — View (layout)
- `app/src/main/res/menu/bottom_nav_menu.xml` — View (menu)

Resource XML:
- `app/src/main/AndroidManifest.xml` — App descriptor (View/manifest)
- `app/src/main/res/values/strings.xml` — Resources
- `app/src/main/res/values/colors.xml` — Resources
- `app/src/main/res/values/themes.xml` — Resources
- `app/src/main/res/xml/data_extraction_rules.xml` — Config
- `app/src/main/res/xml/backup_rules.xml` — Config

Notes:
- No `model` or `viewmodel` packages/files detected in the current codebase; these should be added to implement MVVM proper separation.

### 5) Paragraph: MVVM Architecture (1 paragraph)

The Model-View-ViewModel (MVVM) architecture separates responsibilities into three layers: Models encapsulate application data and business logic (for example, User, Expense, Budget entities and repository classes that access Firebase or local storage); Views (Activities and Fragments) handle UI rendering and user interactions and remain thin by delegating logic; ViewModels act as the mediator between Models and Views, exposing observable state (LiveData) and commands so Views can react to data changes without knowing the underlying data sources. This separation improves testability, modularity, and maintainability: ViewModels hold UI state across configuration changes, Models isolate persistence and network concerns, and Views only subscribe to ViewModel data and perform navigation or display logic.

### 6) Screenshot: Project Management Tool (Sprint 0.5 and Sprint 1)

Placeholder: Add project management screenshot at `docs/sprint_board_screenshot.png` showing Sprint 0.5 and Sprint 1 boards.

### 7) Paragraph: Project Management Tool (1 paragraph)

We used a project management board (e.g., Jira / GitHub Projects) to track user stories and sprint tasks for Sprint 0.5 and Sprint 1. Each card includes the task title, description, acceptance criteria, assignee, and status (To Do / In Progress / Done). The board helps the team visualize work, prioritize sprint backlog items, and keep sprint ceremonies focused; links to PRs and commits were added to each ticket to provide traceability between code changes and project tasks.

### 8) Paragraph: Scrum Meetings for Sprint 1 (1 paragraph)

During Sprint 1 the team held daily standups and at least two sprint planning / grooming sessions: a Sprint Planning meeting to agree on Sprint 1 goals and assign tasks, daily 10–15 minute standups to report progress and blockers, and a Sprint Review / Retrospective at the end of the sprint to demonstrate completed work and capture improvement actions. These meetings ensured continuous communication, early detection of blockers (for example, Firebase setup), and coordinated ownership of deliverables across team members.

---

## Deliverables

- Use Case Diagram: `docs/Use_Case.png`
- Use Case Definitions: above (6+ use cases)
- MVVM Architecture Screenshot: `docs/mvvm_architecture.png` (placeholder)
- Project Management Board Screenshot: `docs/sprint_board_screenshot.png` (placeholder)
- Classes and XML inventory: included above
- CheckStyle report: `app/build/reports/checkstyle/checkstyle.html` (generated by Gradle)


