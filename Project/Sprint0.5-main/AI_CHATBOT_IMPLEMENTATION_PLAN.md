# AI Chatbot Integration - Implementation Plan

## Overview
This document outlines the implementation plan for the AI Chatbot Integration feature as specified in the requirements document.

---

## 1. API Selection

### Recommended: Hugging Face Inference API (Free Tier)
- **API**: Hugging Face Inference API
- **Model**: `mistralai/Mistral-7B-Instruct-v0.2` or `meta-llama/Llama-2-7b-chat-hf` (free tier compatible)
- **Endpoint**: `https://api-inference.huggingface.co/models/{model_name}`
- **Authentication**: API Key (free tier available)
- **Rate Limits**: Free tier allows reasonable usage
- **Alternative**: OpenAI API (has free tier but more limited)

### API Integration Details
- **HTTP Client**: Use `OkHttp` or `Retrofit` for API calls
- **Dependency**: Add `com.squareup.okhttp3:okhttp:4.12.0` to `build.gradle`
- **API Key Storage**: Store in `local.properties` (not committed) or use Android Keystore for production

---

## 2. Data Models

### 2.1 ChatMessage Model
**File**: `app/src/main/java/com/example/sprintproject/model/ChatMessage.java`

**Fields**:
- `String id` - Unique message ID
- `String conversationId` - Reference to parent conversation
- `String content` - Message text
- `String role` - "user" or "assistant"
- `Date timestamp` - When message was sent
- `String userId` - User who sent/received the message

### 2.2 ChatConversation Model
**File**: `app/src/main/java/com/example/sprintproject/model/ChatConversation.java`

**Fields**:
- `String id` - Unique conversation ID
- `String title` - AI-generated conversation title
- `String summary` - AI-generated conversation summary
- `Date createdAt` - When conversation started
- `Date updatedAt` - Last message timestamp
- `String userId` - Owner of the conversation
- `List<String> messageIds` - References to messages (minimal storage)

---

## 3. Repository Layer

### 3.1 ChatbotRepository
**File**: `app/src/main/java/com/example/sprintproject/repository/ChatbotRepository.java`

**Responsibilities**:
- Send messages to AI API
- Handle API responses and errors
- Manage conversation context
- Generate conversation summaries using AI
- Generate conversation titles using AI
- Store/retrieve conversations from Firestore
- Store/retrieve messages from Firestore (minimal per-session)

**Methods**:
- `sendMessage(String message, String conversationId, List<ChatMessage> context) -> Single<String>`
- `generateConversationTitle(List<ChatMessage> messages) -> Single<String>`
- `generateConversationSummary(List<ChatMessage> messages) -> Single<String>`
- `saveConversation(ChatConversation conversation) -> Completable`
- `loadConversations(String userId) -> Single<List<ChatConversation>>`
- `loadMessages(String conversationId) -> Single<List<ChatMessage>>`
- `getUserFinancialData(String userId) -> Single<FinancialContext>` (for contextual insights)

### 3.2 FinancialContext Helper Class
**File**: `app/src/main/java/com/example/sprintproject/repository/FinancialContext.java`

**Purpose**: Aggregate user's financial data for AI context
- Expenses (total, by category, recent)
- Budgets (active, utilization)
- Savings Circles (goals, progress)

---

## 4. ViewModel Layer

### 4.1 ChatbotViewModel (Update Existing)
**File**: `app/src/main/java/com/example/sprintproject/viewmodel/ChatbotViewModel.java`

**LiveData Exposures**:
- `LiveData<List<ChatMessage>> messages` - Current conversation messages
- `LiveData<List<ChatConversation>> conversations` - User's past conversations
- `LiveData<String> error` - Error messages
- `LiveData<Boolean> loading` - Loading state
- `LiveData<String> currentConversationId` - Active conversation ID
- `LiveData<Boolean> showContextDialog` - Trigger for context selection dialog

**Methods**:
- `sendMessage(String message)`
- `startNewConversation()`
- `loadConversations()`
- `loadConversation(String conversationId)`
- `importContextFromConversations(List<String> conversationIds)`
- `handleCustomCommand(String message)` - Detect and handle special commands

**Custom Command Detection**:
- Spending summaries: "show spending summary", "what did I spend this month?"
- Cost-cut suggestions: "suggest ways to save money", "how can I reduce expenses?"
- Monthly comparisons: "compare this month to last month", "monthly spending comparison"

---

## 5. UI/UX Implementation

### 5.1 Layout Files

#### activity_chatbot.xml (Update Existing)
**Components**:
- `RecyclerView` - Scrollable chat messages
- `LinearLayout` or `ConstraintLayout` - Message bubbles container
- `TextInputEditText` - Message input field
- `Button` or `FloatingActionButton` - Send button
- `ProgressBar` - Loading indicator
- `TextView` - Error/placeholder messages

#### item_message_user.xml
- User message bubble (right-aligned, different color)
- Timestamp

#### item_message_assistant.xml
- Assistant message bubble (left-aligned, different color)
- Timestamp

#### dialog_context_selection.xml
- Dialog for selecting previous conversations to import context
- `RecyclerView` with checkboxes for conversation selection
- "Cancel" and "Import" buttons

### 5.2 Adapters

#### ChatMessageAdapter
**File**: `app/src/main/java/com/example/sprintproject/adapter/ChatMessageAdapter.java`
- Handles user vs assistant message display
- Formats timestamps
- Manages message bubble styling

#### ConversationContextAdapter
**File**: `app/src/main/java/com/example/sprintproject/adapter/ConversationContextAdapter.java`
- For context selection dialog
- Shows conversation titles and dates
- Checkbox selection

### 5.3 ChatbotFragment (Update Existing)
**File**: `app/src/main/java/com/example/sprintproject/view/ChatbotFragment.java`

**Functionality**:
- Initialize ViewModel
- Observe LiveData for messages, errors, loading
- Setup RecyclerView with ChatMessageAdapter
- Handle send button click
- Show context selection dialog when starting new conversation (if past conversations exist)
- Display placeholder when no messages
- Display error messages
- Auto-scroll to bottom on new messages

---

## 6. Custom Command Handling

### 6.1 Command Parser
**File**: `app/src/main/java/com/example/sprintproject/utils/ChatCommandParser.java`

**Detects**:
- Spending summary requests
- Cost-cutting suggestions
- Monthly comparisons
- Budget inquiries
- Goal progress queries

### 6.2 Data Aggregation
**File**: `app/src/main/java/com/example/sprintproject/utils/FinancialDataAggregator.java`

**Methods**:
- `getSpendingSummary(String userId, Date startDate, Date endDate) -> SpendingSummary`
- `getMonthlyComparison(String userId) -> MonthlyComparison`
- `getCostCutSuggestions(List<Expense> expenses, List<Budget> budgets) -> List<String>`

**Integration**: These methods fetch data and format it for AI context injection

---

## 7. Database Structure (Firestore)

### Collections:
1. **conversations** (or `users/{userId}/conversations`)
   - Document ID: conversation ID
   - Fields: title, summary, createdAt, updatedAt, userId

2. **messages** (or `conversations/{conversationId}/messages`)
   - Document ID: message ID
   - Fields: conversationId, content, role, timestamp, userId
   - **Note**: Store minimal per-session (e.g., last 20-30 messages per conversation)

---

## 8. Implementation Order

### Phase 1: Foundation (Core Infrastructure)
1. ✅ Add HTTP client dependency (OkHttp) to `build.gradle`
2. Create `ChatMessage` model
3. Create `ChatConversation` model
4. Create `ChatbotRepository` with basic API integration
5. Update `ChatbotViewModel` with LiveData and basic methods
6. Test API connection

### Phase 2: UI Implementation
7. Update `activity_chatbot.xml` layout
8. Create `item_message_user.xml` and `item_message_assistant.xml`
9. Create `ChatMessageAdapter`
10. Update `ChatbotFragment` with RecyclerView and input handling
11. Test basic chat flow

### Phase 3: Conversation Management
12. Implement conversation saving in `ChatbotRepository`
13. Implement conversation loading
14. Add conversation title generation (AI API)
15. Add conversation summary generation (AI API)
16. Update UI to show conversation history (optional sidebar/list)

### Phase 4: Context Import Feature
17. Create `dialog_context_selection.xml`
18. Create `ConversationContextAdapter`
19. Implement context merging logic in `ChatbotRepository`
20. Add dialog trigger in `ChatbotFragment`
21. Test context import flow

### Phase 5: Data-Driven Insights
22. Create `FinancialContext` helper class
23. Create `FinancialDataAggregator` utility
24. Integrate financial data fetching in `ChatbotRepository`
25. Update AI prompt to include financial context
26. Test contextual responses

### Phase 6: Custom Commands
27. Create `ChatCommandParser`
28. Implement command detection in `ChatbotViewModel`
29. Implement spending summary command
30. Implement cost-cut suggestions command
31. Implement monthly comparison command
32. Test all custom commands

### Phase 7: Polish & Error Handling
33. Add comprehensive error handling
34. Add loading states and placeholders
35. Add input validation
36. Optimize message storage (minimal per-session)
37. Add conversation cleanup (optional: delete old conversations)
38. Final testing and bug fixes

---

## 9. API Integration Details

### Hugging Face API Example Request:
```java
POST https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2
Headers:
  Authorization: Bearer {API_KEY}
  Content-Type: application/json

Body:
{
  "inputs": "User: What did I spend this month?\nAssistant:",
  "parameters": {
    "max_new_tokens": 200,
    "temperature": 0.7
  }
}
```

### Prompt Engineering:
- Include system prompt about being a financial assistant
- Inject financial context when available
- Format conversation history properly
- Handle special commands with structured responses

---

## 10. Dependencies to Add

### build.gradle additions:
```gradle
dependencies {
    // HTTP client for API calls
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // JSON parsing (if not already included)
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // RxJava for async operations (optional, can use callbacks)
    implementation 'io.reactivex.rxjava3:rxjava:3.1.8'
    implementation 'io.reactivex.rxjava3:rxandroid:3.0.2'
}
```

---

## 11. Testing Strategy

### Unit Tests:
- `ChatCommandParser` - Command detection
- `FinancialDataAggregator` - Data aggregation logic
- `ChatbotRepository` - API mocking and data operations

### Integration Tests:
- API integration (with mock responses)
- Firestore operations
- ViewModel state management

### UI Tests:
- Message sending and receiving
- Context dialog flow
- Error handling display

---

## 12. Security Considerations

1. **API Key Storage**: Use `local.properties` for development, Android Keystore for production
2. **User Data**: Ensure all conversations are user-scoped in Firestore
3. **Input Validation**: Sanitize user inputs before sending to API
4. **Rate Limiting**: Implement client-side rate limiting to avoid API abuse

---

## 13. Future Enhancements (Out of Scope)

- Voice input/output
- Conversation search
- Export conversations
- Multi-language support
- Advanced analytics integration

---

## 14. Rubric Requirements Coverage

### ✅ Requirement 1: Free AI API + Budgeting Tips + Error Handling (10 pts)
**Coverage**:
- ✅ **Free AI API**: Using Hugging Face Inference API (free tier) - Section 1
- ✅ **Budgeting Tips/Financial Advice**: AI prompt includes financial context, generates advice - Sections 3.1, 5.5
- ✅ **Graceful Error Handling**: 
  - Try-catch blocks around all API calls
  - Error LiveData in ViewModel
  - User-friendly error messages in UI
  - No crashes on API failure - handled in Repository and ViewModel
  - Network timeout handling
  - Fallback responses when API unavailable

### ✅ Requirement 2: Custom Commands (5 pts)
**Coverage**:
- ✅ **Custom Command Detection**: `ChatCommandParser` utility - Section 6.1
- ✅ **Command Examples**: 
  - "summarize my spending" → Spending summary
  - "suggest ways to save money" → Cost-cut suggestions
  - "compare this month to last month" → Monthly comparison
- ✅ **Implementation**: Commands detected in ViewModel, data aggregated, sent to AI with context - Section 6

### ✅ Requirement 3: Conversation Storage + Previous Chat Popup (8 pts)
**Coverage**:
- ✅ **Conversation Summaries**: Generated using AI API, stored in Firestore - Section 3.1
- ✅ **Conversation Titles**: Generated using AI API, stored in Firestore - Section 3.1
- ✅ **Previous Chat Reference Popup**: 
  - `dialog_context_selection.xml` layout - Section 5.1
  - `ConversationContextAdapter` for selection - Section 5.2
  - Dialog triggered when starting new conversation - Section 5.3
  - Context merging logic in Repository - Section 3.1

### ✅ Requirement 4: No Crashes, Logic Errors, Broken UI (3 pts)
**Coverage**:
- ✅ **Error Handling**: Comprehensive try-catch, null checks, validation
- ✅ **UI Validation**: Input validation, empty state handling
- ✅ **Testing**: Unit tests, integration tests, UI tests - Section 11
- ✅ **Code Review**: Following checkstyle rules - Section 15

### ✅ Requirement 5: MVVM Architecture Maintained (2 pts)
**Coverage**:
- ✅ **Model Layer**: `ChatMessage`, `ChatConversation` models - Section 2
- ✅ **View Layer**: `ChatbotFragment` (thin, UI only) - Section 5.3
- ✅ **ViewModel Layer**: `ChatbotViewModel` with LiveData - Section 4
- ✅ **Repository Layer**: `ChatbotRepository` (data operations) - Section 3
- ✅ **Separation of Concerns**: Clear boundaries between layers

---

## 15. Code Smell Prevention Guidelines

### 15.1 Checkstyle Compliance
Following project's `checkstyle.xml` configuration:
- ✅ **Line Length**: Max 100 characters (except package/import)
- ✅ **Method Length**: Max 150 lines
- ✅ **Parameter Count**: Max 7 parameters per method
- ✅ **File Length**: Max 2000 lines
- ✅ **Naming Conventions**: camelCase for variables/methods, PascalCase for classes
- ✅ **Javadoc**: Required for public methods, classes, variables
- ✅ **Indentation**: 4 spaces (no tabs)
- ✅ **Whitespace**: Proper spacing around operators, braces

### 15.2 Code Smells to Avoid

#### Long Method (Code Smell)
- **Prevention**: Break down methods > 50 lines into smaller, focused methods
- **Example**: `sendMessage()` will delegate to helper methods for context building, API call, error handling

#### God Class (Code Smell)
- **Prevention**: Single Responsibility Principle - each class has one clear purpose
- **Repository**: Only handles data operations
- **ViewModel**: Only handles UI state and business logic coordination
- **Fragment**: Only handles UI rendering and user input

#### Duplicate Code (Code Smell)
- **Prevention**: Extract common logic into utility methods
- **Example**: Date formatting, error message formatting in utility classes

#### Magic Numbers/Strings (Code Smell)
- **Prevention**: Use constants for API endpoints, error messages, field names
- **Example**: `private static final String API_BASE_URL = "https://api-inference.huggingface.co"`

#### Deep Nesting (Code Smell)
- **Prevention**: Early returns, guard clauses, extract methods
- **Example**: Use early returns in error handling instead of nested if-else

#### Feature Envy (Code Smell)
- **Prevention**: Methods should operate on their own class's data
- **Example**: Financial data aggregation in `FinancialDataAggregator`, not in ViewModel

#### Data Clumps (Code Smell)
- **Prevention**: Group related data into objects
- **Example**: `FinancialContext` class groups expenses, budgets, goals

#### Primitive Obsession (Code Smell)
- **Prevention**: Use value objects for related primitives
- **Example**: `Date` objects instead of long timestamps where appropriate

### 15.3 SOLID Principles Adherence

- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Extensible through interfaces (e.g., command parser can be extended)
- **Liskov Substitution**: Proper inheritance if used
- **Interface Segregation**: Small, focused interfaces
- **Dependency Inversion**: Depend on abstractions (Repository pattern)

### 15.4 Error Handling Best Practices

- ✅ **Never swallow exceptions silently** - Always log or show to user
- ✅ **Specific exception types** - Catch specific exceptions, not generic `Exception`
- ✅ **User-friendly messages** - Technical errors translated to user-friendly messages
- ✅ **Graceful degradation** - App continues to function even if API fails
- ✅ **Null safety** - Null checks before accessing objects

---

## Notes

- The implementation follows MVVM architecture already established in the project
- Uses Firebase Firestore for persistence (consistent with existing codebase)
- Follows existing code patterns (FirestoreManager, ViewModel structure)
- Free API tier should be sufficient for development and initial testing
- Consider API costs if scaling to production
- All code will pass checkstyle validation
- Error handling ensures no crashes on API failures

