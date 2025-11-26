# AI Chatbot Integration - File Breakdown

## Files to CREATE (New Files)

### Models (2 files)
1. **`app/src/main/java/com/example/sprintproject/model/ChatMessage.java`**
   - Represents individual chat messages (user/assistant)
   - Fields: id, conversationId, content, role, timestamp, userId

2. **`app/src/main/java/com/example/sprintproject/model/ChatConversation.java`**
   - Represents a conversation session
   - Fields: id, title, summary, createdAt, updatedAt, userId, messageIds

### Repository (2 files)
3. **`app/src/main/java/com/example/sprintproject/repository/ChatbotRepository.java`**
   - Handles AI API calls, Firestore operations, conversation management
   - Methods: sendMessage, generateTitle, generateSummary, saveConversation, loadConversations, etc.

4. **`app/src/main/java/com/example/sprintproject/repository/FinancialContext.java`**
   - Helper class to aggregate financial data for AI context
   - Contains: expenses summary, budgets, savings circles data

### Utilities (2 files)
5. **`app/src/main/java/com/example/sprintproject/utils/ChatCommandParser.java`**
   - Detects and identifies custom commands in user messages
   - Methods: isSpendingSummaryCommand, isCostCutCommand, isComparisonCommand, etc.

6. **`app/src/main/java/com/example/sprintproject/utils/FinancialDataAggregator.java`**
   - Aggregates financial data for custom commands
   - Methods: getSpendingSummary, getMonthlyComparison, getCostCutSuggestions

### Adapters (2 files)
7. **`app/src/main/java/com/example/sprintproject/adapter/ChatMessageAdapter.java`**
   - RecyclerView adapter for displaying chat messages
   - Handles user vs assistant message styling

8. **`app/src/main/java/com/example/sprintproject/adapter/ConversationContextAdapter.java`**
   - RecyclerView adapter for conversation selection dialog
   - Shows conversation titles with checkboxes for selection

### Layout Files (3 files)
9. **`app/src/main/res/layout/item_message_user.xml`**
   - Layout for user message bubble (right-aligned)

10. **`app/src/main/res/layout/item_message_assistant.xml`**
    - Layout for assistant message bubble (left-aligned)

11. **`app/src/main/res/layout/dialog_context_selection.xml`**
    - Dialog layout for selecting previous conversations to import context

---

## Files to MODIFY (Existing Files)

### ViewModel (1 file)
1. **`app/src/main/java/com/example/sprintproject/viewmodel/ChatbotViewModel.java`**
   - **Current State**: Basic skeleton with only date handling
   - **Changes**:
     - Add LiveData for messages, conversations, errors, loading, currentConversationId
     - Add methods: sendMessage(), startNewConversation(), loadConversations(), loadConversation(), importContextFromConversations()
     - Add custom command handling logic
     - Integrate with ChatbotRepository

### View (1 file)
2. **`app/src/main/java/com/example/sprintproject/view/ChatbotFragment.java`**
   - **Current State**: Basic skeleton with date observation
   - **Changes**:
     - Add RecyclerView setup with ChatMessageAdapter
     - Add message input handling and send button
     - Add LiveData observers for messages, errors, loading
     - Add context selection dialog trigger and handling
     - Add placeholder/error message display
     - Add auto-scroll functionality
     - Remove/update date observation logic if not needed

### Layout (1 file)
3. **`app/src/main/res/layout/activity_chatbot.xml`**
   - **Current State**: Only has header with title
   - **Changes**:
     - Add RecyclerView for messages
     - Add TextInputEditText for message input
     - Add send button (Button or FloatingActionButton)
     - Add ProgressBar for loading indicator
     - Add TextView for error/placeholder messages
     - Maintain existing header

### Build Configuration (1 file)
4. **`app/build.gradle`**
   - **Current State**: Has Firebase, Material, Lifecycle dependencies
   - **Changes**:
     - Add `implementation 'com.squareup.okhttp3:okhttp:4.12.0'` for HTTP client
     - Add `implementation 'com.google.code.gson:gson:2.10.1'` for JSON parsing (if not already present)

---

## Summary

### Total Files
- **New Files**: 11 files (2 models + 2 repository + 2 utils + 2 adapters + 3 layouts)
- **Modified Files**: 4 files (1 ViewModel + 1 View + 1 Layout + 1 Build config)
- **Grand Total**: 15 files

### File Categories
- **Java Classes**: 8 new, 2 modified = 10 Java files
- **XML Layouts**: 3 new, 1 modified = 4 layout files
- **Build Files**: 1 modified = 1 build file

---

## Implementation Checklist

### Phase 1: Foundation
- [ ] Add OkHttp dependency to build.gradle
- [ ] Create ChatMessage.java
- [ ] Create ChatConversation.java
- [ ] Create ChatbotRepository.java (basic structure)
- [ ] Update ChatbotViewModel.java (add LiveData)

### Phase 2: UI
- [ ] Update activity_chatbot.xml
- [ ] Create item_message_user.xml
- [ ] Create item_message_assistant.xml
- [ ] Create ChatMessageAdapter.java
- [ ] Update ChatbotFragment.java (basic UI)

### Phase 3: Core Functionality
- [ ] Complete ChatbotRepository.java (API integration)
- [ ] Complete ChatbotViewModel.java (all methods)
- [ ] Test API connection

### Phase 4: Conversation Management
- [ ] Add conversation save/load in ChatbotRepository
- [ ] Add title/summary generation
- [ ] Update ChatbotViewModel with conversation methods

### Phase 5: Context Import
- [ ] Create dialog_context_selection.xml
- [ ] Create ConversationContextAdapter.java
- [ ] Add context merging in ChatbotRepository
- [ ] Add dialog trigger in ChatbotFragment

### Phase 6: Data Integration
- [ ] Create FinancialContext.java
- [ ] Create FinancialDataAggregator.java
- [ ] Integrate financial data in ChatbotRepository

### Phase 7: Custom Commands
- [ ] Create ChatCommandParser.java
- [ ] Integrate command detection in ChatbotViewModel
- [ ] Test all custom commands

### Phase 8: Polish
- [ ] Add comprehensive error handling
- [ ] Add loading states
- [ ] Add input validation
- [ ] Run checkstyle validation
- [ ] Final testing

---

## Code Smell Prevention Checklist

For each new/modified file, ensure:
- [ ] Methods are < 150 lines (checkstyle requirement)
- [ ] Methods have < 7 parameters
- [ ] Lines are < 100 characters (except imports)
- [ ] Public methods have Javadoc
- [ ] No magic numbers/strings (use constants)
- [ ] Proper null checks
- [ ] Try-catch for all API calls
- [ ] No duplicate code
- [ ] Single Responsibility Principle followed
- [ ] Proper error handling (no silent failures)


