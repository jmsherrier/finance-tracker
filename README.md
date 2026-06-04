# FinTrack

![Android](https://img.shields.io/badge/Android-API%2026+-3DDC84?style=flat-square&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-Android-007396?style=flat-square&logo=openjdk&logoColor=white)
![Architecture](https://img.shields.io/badge/architecture-MVVM-2496ED?style=flat-square)
![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%26%20Auth-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Mistral-7B](https://img.shields.io/badge/AI-Mistral--7B-FFD21E?style=flat-square&logo=huggingface&logoColor=black)
![Material Design 3](https://img.shields.io/badge/Material%20Design-3-757575?style=flat-square&logo=materialdesign&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-analyzed-4E9BCD?style=flat-square&logo=sonarqube&logoColor=white)
![Georgia Tech](https://img.shields.io/badge/Georgia%20Tech-CS2340-B3A369?style=flat-square)

An Android personal finance app featuring expense tracking, budget management, collaborative savings circles, and an AI-powered financial advisor chatbot.

Built by a 6-person team at Georgia Institute of Technology using MVVM architecture, Firebase, and the Hugging Face Inference API.

---

## Features

- **Authentication** — Secure register/login via Firebase Authentication
- **Expense Logging** — Track and categorize expenses with date-aware management and reminders
- **Budget Management** — Per-category budgets with real-time threshold alerts and warning dialogs
- **Savings Circles** — Collaborative group savings goals: invite members, log contributions, track progress toward a shared target
- **AI Financial Advisor** — Chatbot powered by Mistral-7B (Hugging Face Inference API):
  - Persistent conversation history stored in Firestore
  - Custom financial commands: spending summaries, cost-cutting suggestions, month-over-month comparisons
  - Context injection from previous conversations
- **Dashboard** — Unified financial overview with live data from all modules

---

## Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (Java, API 26+) |
| Architecture | MVVM + Repository pattern |
| Backend | Firebase Firestore, Firebase Authentication |
| AI | Hugging Face Inference API (Mistral-7B-Instruct) |
| HTTP | OkHttp |
| UI | Material Design 3 |
| Code quality | Checkstyle, SOLID/GRASP principles |

---

## Architecture

```
app/
├── model/          # POJOs and business entities
├── repository/     # Data access (Firestore + AI API)
├── viewmodel/      # LiveData-based state and business logic
├── view/           # Activities and Fragments (thin UI layer)
├── adapter/        # RecyclerView adapters
├── factory/        # ViewModel factories
├── strategy/       # Strategy pattern implementations
└── utils/          # Shared utilities
```

MVVM keeps views decoupled from data. Repositories abstract Firebase and external APIs behind a clean interface that ViewModels consume via LiveData, surviving configuration changes and simplifying testing.

---

## Design

### Use Case Diagram

![Use Case Diagram](Project/Sprint0.5-main/docs/Use_Case.png)

### Domain Model

![Domain Model](Project/Sprint0.5-main/docs/Domain_Model.png)

### Sequence Diagram — Log Expense

![Log Expense Sequence](Project/Sprint0.5-main/docs/sequence/log_expense_sequence.png)

### Further Documentation

Supporting design and code-quality artifacts live in [`docs/`](docs/):

- [SOLID Principles write-up](docs/SOLID-Principles-Writeup.pdf) — applying the five SOLID principles to the codebase
- [SOLID & GRASP analysis](docs/SOLID_GRASP/) — class diagram, write-up, and a reference Java implementation
- [Code smells analysis](docs/Code_Smells/) — identified smells and refactorings
- [Group Savings sequence diagram](docs/SequenceDiagram_GroupSavings.drawio) — editable `.drawio` source

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Firebase project with Firestore and Authentication enabled
- Hugging Face account (free tier works)

### Setup

1. Clone the repo and open `Project/Sprint0.5-main/` in Android Studio.
2. Add your Firebase `google-services.json` to `app/`.
3. Add your Hugging Face API key to `Project/Sprint0.5-main/local.properties`:
   ```
   HUGGING_FACE_API_KEY=hf_your_key_here
   ```
4. Build and run on a device or emulator (API 26+).

See [API_KEY_SETUP.md](Project/Sprint0.5-main/API_KEY_SETUP.md) for full Hugging Face setup instructions.

---

## Team

Austin · Shreya · Yitian Yu · Bogdan Perlroth · Joseph Sherrier · Nehal Singhal
