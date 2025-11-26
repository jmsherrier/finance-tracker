# Building a Personal Finance Management App: A Journey Through Software Development Lifecycle

## Introduction

As part of CS2340 (Software Engineering), our team of six developers embarked on an ambitious project to build a comprehensive personal finance management Android application. This journey took us through the entire Software Development Lifecycle (SDLC), from initial requirements gathering to deployment, while working in an Agile environment with sprints, daily standups, and continuous integration. This article shares our experiences, the technologies we mastered, and the valuable lessons learned along the way.

---

## The Product: A Personal Finance Management Application

Our application is a feature-rich Android app designed to help users take control of their financial lives. The core functionality includes:

### Key Features

**1. User Authentication & Account Management**
- Secure user registration and login using Firebase Authentication
- User profile management with persistent data storage

**2. Expense Tracking**
- Log daily expenses with categories, amounts, and dates
- View expense history with filtering and search capabilities
- Visual expense analytics with charts and graphs

**3. Budget Management**
- Create and manage multiple budgets for different categories
- Set spending limits and receive real-time budget warnings
- Track budget utilization with progress indicators
- Threshold notifications when approaching budget limits

**4. Savings Circles**
- Create or join savings circles (group savings goals)
- Collaborative financial goal tracking
- Progress visualization for group savings objectives
- Social features for financial accountability

**5. AI-Powered Financial Assistant**
- Interactive chatbot for financial advice and budgeting tips
- Custom commands for spending summaries and cost-cutting suggestions
- Monthly spending comparisons
- Context-aware conversations with conversation history management

**6. Dashboard & Analytics**
- Comprehensive financial overview
- Visual charts (pie charts, bar charts) for expense breakdown
- Real-time financial health indicators
- Dark mode support for better user experience

---

## Software Development Lifecycle: Our Approach

### Phase 1: Requirements Gathering & Planning (Sprint 0.5)

We began with thorough requirements analysis, creating:
- **Use Case Diagrams**: Identified primary actors (Registered Users, Admins, External Auditors) and their interactions
- **Use Cases**: Defined 6+ primary use cases including account creation, expense logging, budget creation, savings circles, and chatbot interactions
- **Domain Model**: Designed the core entities and their relationships
- **Architecture Planning**: Decided on MVVM architecture pattern early in the process

### Phase 2: Design & Architecture

**MVVM Architecture Implementation**
We adopted the Model-View-ViewModel (MVVM) architecture pattern, which proved invaluable for:
- **Separation of Concerns**: Models encapsulate data and business logic, Views handle UI rendering, ViewModels mediate between them
- **Testability**: Business logic in ViewModels can be unit tested independently
- **Lifecycle Management**: ViewModels survive configuration changes, preventing data loss
- **Reactive Programming**: Using LiveData for observable state management

**Design Patterns Applied**
- **Repository Pattern**: Abstracted data sources (Firebase Firestore, local storage) behind repository interfaces
- **Strategy Pattern**: Used for progress calculation strategies in savings circles
- **Observer Pattern**: Leveraged LiveData and ViewModel observers for reactive UI updates
- **Adapter Pattern**: RecyclerView adapters for efficient list rendering

### Phase 3: Development (Sprint 1 & Beyond)

**Code Quality Standards**
- **Checkstyle Integration**: Enforced code style with automated checks
  - Maximum line length: 100 characters
  - Maximum method length: 150 lines
  - Maximum parameters: 7 per method
  - Comprehensive Javadoc requirements
- **SOLID Principles**: Applied throughout the codebase
  - **Single Responsibility**: Each class has one clear purpose
  - **Open/Closed**: Extensible through interfaces
  - **Liskov Substitution**: Proper inheritance hierarchies
  - **Interface Segregation**: Small, focused interfaces
  - **Dependency Inversion**: Depend on abstractions, not concretions
- **GRASP Principles**: Applied for better object-oriented design
- **Code Smell Prevention**: Actively avoided long methods, god classes, duplicate code, and magic numbers

### Phase 4: Testing & Quality Assurance

- **Unit Tests**: Comprehensive test coverage for business logic
- **Integration Tests**: Testing repository and ViewModel interactions
- **UI Tests**: Automated testing for critical user flows
- **Error Handling**: Graceful degradation and user-friendly error messages

### Phase 5: Deployment & Maintenance

- Version control with Git
- Branching strategy (main, feature branches)
- Continuous integration practices
- Code review processes

---

## Tech Stack: Technologies We Mastered

### Frontend & UI

**Android Development**
- **Language**: Java 8
- **SDK**: Android API 24-34 (targeting modern Android devices)
- **UI Framework**: Material Design Components
- **Layout**: ConstraintLayout, LinearLayout, RecyclerView
- **Data Binding**: Android Data Binding for declarative UI
- **Charts**: MPAndroidChart library for data visualization

### Backend & Data Management

**Firebase Services**
- **Firebase Authentication**: Secure user authentication
- **Cloud Firestore**: NoSQL database for real-time data synchronization
- **FirestoreManager**: Custom abstraction layer for database operations

### Architecture Components

**Android Architecture Components**
- **ViewModel**: Lifecycle-aware components for UI-related data
- **LiveData**: Observable data holders
- **Lifecycle**: Managing Android lifecycle components

### Third-Party Integrations

**AI/ML Services**
- **Hugging Face Inference API**: Free-tier AI model integration for chatbot
- **OkHttp**: HTTP client for API communication
- **Gson**: JSON parsing and serialization

### Development Tools

**Build & Quality**
- **Gradle**: Build automation and dependency management
- **Checkstyle**: Code quality enforcement
- **Git**: Version control and collaboration
- **Jira**: Project management and sprint tracking

---

## Agile Methodology: Working in Sprints

### Sprint Structure

**Sprint 0.5: Foundation**
- Team formation and role assignment
- Project setup and repository initialization
- Architecture decisions and MVVM template creation
- Initial use case and domain modeling

**Sprint 1: Core Features**
- User authentication implementation
- Expense logging functionality
- Basic dashboard creation
- Budget management foundation

**Subsequent Sprints: Feature Development**
- Savings circles feature
- AI chatbot integration
- Advanced analytics and visualizations
- Notification system
- UI/UX improvements

### Agile Practices We Followed

**1. Sprint Planning**
- Defined sprint goals and user stories
- Estimated story points
- Assigned tasks to team members
- Created acceptance criteria for each story

**2. Daily Standups**
- 10-15 minute meetings
- Three questions: What did I do? What will I do? Any blockers?
- Kept the team aligned and aware of progress

**3. Sprint Review**
- Demo of completed features
- Stakeholder feedback collection
- Celebration of achievements

**4. Sprint Retrospective**
- What went well?
- What could be improved?
- Action items for next sprint

**5. Project Management with Jira**
- User stories with detailed acceptance criteria
- Task assignment and tracking
- Sprint board visualization
- Link between tickets, PRs, and commits
- Progress tracking and velocity measurement

### Branching Strategy

- **main branch**: Stable, tested code only
- **Feature branches**: Individual developer branches (e.g., `nehal`, `austin`, `shreya`)
- **Pull Request workflow**: Code review before merging
- **Merge conflicts**: Regular resolution and communication

---

## Team Collaboration & Communication

### Team Composition
Our team consisted of six members:
- Austin
- Shreya
- Yitian Yu
- Bogdan Perlroth
- Joseph Sherrier
- Nehal Singhal

### Collaboration Tools
- **Git/GitHub**: Version control and code collaboration
- **Jira**: Project management and sprint tracking
- **Communication**: Regular meetings and async communication channels

### Challenges & Solutions

**Challenge 1: Merge Conflicts**
- **Solution**: Regular pulls from main, frequent communication, clear ownership of files

**Challenge 2: Code Integration**
- **Solution**: MVVM architecture provided clear boundaries, making integration smoother

**Challenge 3: Feature Dependencies**
- **Solution**: Careful sprint planning to identify dependencies early

**Challenge 4: Code Quality Consistency**
- **Solution**: Checkstyle enforcement, code review process, shared coding standards

---

## Key Learnings & Takeaways

### Technical Skills Gained

1. **Android Development**: Deep understanding of Android SDK, lifecycle management, and Material Design
2. **MVVM Architecture**: Mastered the pattern and its benefits for maintainable code
3. **Firebase Integration**: Learned cloud-based authentication and database management
4. **API Integration**: Gained experience integrating third-party APIs (Hugging Face)
5. **Testing**: Developed skills in unit testing, integration testing, and UI testing
6. **Code Quality**: Understood the importance of code style, documentation, and maintainability

### Soft Skills Developed

1. **Agile Methodology**: Hands-on experience with sprints, standups, and retrospectives
2. **Team Collaboration**: Working effectively in a distributed team environment
3. **Project Management**: Using Jira for tracking and organizing work
4. **Code Review**: Giving and receiving constructive feedback
5. **Problem Solving**: Debugging complex issues and finding creative solutions
6. **Communication**: Clear documentation and effective team communication

### Best Practices Discovered

1. **Architecture First**: Deciding on MVVM early saved significant refactoring time
2. **Code Quality from Day One**: Enforcing Checkstyle from the start prevented technical debt
3. **Incremental Development**: Building features incrementally allowed for early feedback
4. **Documentation**: Maintaining documentation helped with onboarding and knowledge transfer
5. **Testing Early**: Writing tests alongside code caught bugs early
6. **Regular Communication**: Daily standups prevented blockers from stalling progress

---

## Project Highlights & Achievements

### Technical Achievements

- **48+ files** created and modified across multiple sprints
- **2,300+ lines** of code added in major feature implementations
- **Zero crashes** in production-ready features through comprehensive error handling
- **100% Checkstyle compliance** maintained throughout development
- **Comprehensive test coverage** for critical business logic

### Feature Highlights

1. **AI Chatbot Integration**: Successfully integrated free-tier AI API with custom command parsing
2. **Real-time Synchronization**: Firebase Firestore enabled seamless multi-device data sync
3. **Advanced Visualizations**: Interactive charts and graphs for financial insights
4. **Notification System**: Budget warnings and threshold notifications
5. **Dark Mode Support**: Enhanced user experience with theme switching

### Process Achievements

- **Consistent Sprint Delivery**: Met sprint goals consistently
- **Effective Code Reviews**: Maintained code quality through peer review
- **Successful Merges**: Resolved complex merge conflicts efficiently
- **Team Alignment**: Maintained clear communication and shared understanding

---

## Reflection: What We'd Do Differently

### What Worked Well

1. **Early Architecture Decision**: Choosing MVVM from the start
2. **Checkstyle Enforcement**: Automated code quality checks
3. **Jira Integration**: Clear task tracking and sprint management
4. **Incremental Development**: Building features one at a time
5. **Regular Standups**: Kept everyone aligned and aware

### Areas for Improvement

1. **Earlier Testing**: Could have started writing tests even earlier
2. **More Documentation**: Some implementation details could have been better documented
3. **API Design**: Could have designed API contracts earlier
4. **Performance Testing**: More focus on performance optimization earlier
5. **User Testing**: Earlier user feedback collection

---

## Conclusion

This project provided an invaluable hands-on experience with the complete Software Development Lifecycle. From initial requirements gathering to deployment, we learned not just how to write code, but how to build software as a team. The Agile methodology, combined with modern Android development practices and a focus on code quality, resulted in a functional, maintainable application that we're proud to have built.

The experience taught us that software development is as much about process, communication, and collaboration as it is about writing code. The MVVM architecture, SOLID principles, and Agile practices we applied weren't just theoretical concepts—they were practical tools that helped us deliver a quality product.

For anyone embarking on a similar journey, our advice is:
- **Start with architecture**: Make architectural decisions early
- **Enforce code quality**: Use tools like Checkstyle from day one
- **Communicate frequently**: Daily standups and regular communication prevent issues
- **Test continuously**: Don't wait until the end to write tests
- **Embrace Agile**: The iterative approach allows for learning and adaptation
- **Document as you go**: Future you (and your teammates) will thank you

This project was more than just a class assignment—it was a real-world software development experience that prepared us for careers in software engineering.

---

## Technical Specifications Summary

- **Platform**: Android (API 24-34)
- **Language**: Java 8
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Firebase Cloud Firestore
- **Authentication**: Firebase Authentication
- **UI Framework**: Material Design Components
- **Build Tool**: Gradle
- **Version Control**: Git/GitHub
- **Project Management**: Jira
- **Code Quality**: Checkstyle
- **Testing**: JUnit, Mockito, Robolectric
- **Charts**: MPAndroidChart
- **HTTP Client**: OkHttp
- **JSON Parsing**: Gson
- **AI Integration**: Hugging Face Inference API

---

*This article reflects the collaborative work of Team 3 in CS2340 Fall 2025. The project demonstrates practical application of software engineering principles, Agile methodology, and modern Android development practices.*

