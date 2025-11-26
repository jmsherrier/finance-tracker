package com.example.sprintproject.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.ChatConversation;
import com.example.sprintproject.model.ChatMessage;
import com.example.sprintproject.repository.ChatbotRepository;
import com.example.sprintproject.repository.FinancialContext;
import com.example.sprintproject.utils.ChatCommandParser;
import com.example.sprintproject.utils.FinancialDataAggregator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ViewModel for managing chatbot UI state and business logic.
 * Follows MVVM architecture pattern.
 */
public class ChatbotViewModel extends ViewModel {
    private static final String TAG = "ChatbotViewModel";
    
    // Constants for error messages and message roles
    private static final String ERROR_USER_NOT_AUTHENTICATED = "User not authenticated";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String ROLE_USER = "user";

    private final ChatbotRepository repository;
    private final MutableLiveData<List<ChatMessage>> messages;
    private final MutableLiveData<List<ChatConversation>> conversations;
    private final MutableLiveData<String> error;
    private final MutableLiveData<Boolean> loading;
    private final MutableLiveData<String> currentConversationId;
    private final MutableLiveData<Boolean> showContextDialog;

    private List<ChatMessage> currentMessages;
    private String currentUserId;
    private final Handler mainHandler;

    /**
     * Constructor initializes repository and LiveData.
     */
    public ChatbotViewModel() {
        this.repository = new ChatbotRepository();
        this.messages = new MutableLiveData<>(new ArrayList<>());
        this.conversations = new MutableLiveData<>(new ArrayList<>());
        this.error = new MutableLiveData<>();
        this.loading = new MutableLiveData<>(false);
        this.currentConversationId = new MutableLiveData<>();
        this.showContextDialog = new MutableLiveData<>(false);
        this.currentMessages = new ArrayList<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Sets the API key for the AI service.
     *
     * @param apiKey the API key
     */
    public void setApiKey(String apiKey) {
        repository.setApiKey(apiKey);
    }

    /**
     * Gets the current messages LiveData.
     *
     * @return LiveData for messages
     */
    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    /**
     * Gets the conversations LiveData.
     *
     * @return LiveData for conversations
     */
    public LiveData<List<ChatConversation>> getConversations() {
        return conversations;
    }

    /**
     * Gets the error LiveData.
     *
     * @return LiveData for error messages
     */
    public LiveData<String> getError() {
        return error;
    }

    /**
     * Gets the loading state LiveData.
     *
     * @return LiveData for loading state
     */
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * Gets the current conversation ID LiveData.
     *
     * @return LiveData for conversation ID
     */
    public LiveData<String> getCurrentConversationId() {
        return currentConversationId;
    }

    /**
     * Gets the show context dialog LiveData.
     *
     * @return LiveData for showing context dialog
     */
    public LiveData<Boolean> getShowContextDialog() {
        return showContextDialog;
    }

    /**
     * Sends a message to the AI and handles the response.
     *
     * @param messageText the user's message
     */
    public void sendMessage(String messageText) {
        try {
            if (!validateMessageInput(messageText)) {
                return;
            }

            String conversationId = ensureConversationExists();
            if (conversationId == null) {
                return;
            }

            String userId = getCurrentUserId();
            if (userId == null) {
                error.setValue(ERROR_USER_NOT_AUTHENTICATED);
                return;
            }

            final String finalConversationId = conversationId;
            ChatMessage userMessage = createAndAddUserMessage(finalConversationId, messageText.trim(), userId);
            saveUserMessageAndSendToAI(userMessage, messageText.trim(), finalConversationId, userId);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in sendMessage", e);
            loading.setValue(false);
            error.setValue("An unexpected error occurred. Please try again.");
        }
    }

    private boolean validateMessageInput(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            error.setValue("Message cannot be empty");
            return false;
        }
        return true;
    }

    private String ensureConversationExists() {
        String conversationId = currentConversationId.getValue();
        if (conversationId == null || conversationId.isEmpty()) {
            startNewConversation();
            conversationId = currentConversationId.getValue();
        }

        if (conversationId == null) {
            error.setValue("Failed to create conversation");
            return null;
        }
        return conversationId;
    }

    private ChatMessage createAndAddUserMessage(String conversationId, String messageText, String userId) {
        ChatMessage userMessage = new ChatMessage(conversationId, messageText, ROLE_USER, userId);
        ensureMessagesListInitialized();
        currentMessages.add(userMessage);
        messages.setValue(new ArrayList<>(currentMessages));
        return userMessage;
    }

    private void ensureMessagesListInitialized() {
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }
    }

    private void saveUserMessageAndSendToAI(ChatMessage userMessage, String messageText,
                                           String conversationId, String userId) {
        repository.saveMessage(userMessage, new ChatbotRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(true);
                error.setValue(null);
                fetchFinancialContextAndSendMessage(messageText, conversationId, userId);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue("Error saving message: " + errorMsg);
                Log.e(TAG, "Error saving user message: " + errorMsg);
            }
        });
    }

    private void fetchFinancialContextAndSendMessage(String messageText, String conversationId, String userId) {
        repository.fetchFinancialContext(userId, new ChatbotRepository.FinancialContextCallback() {
            @Override
            public void onSuccess(FinancialContext financialContext) {
                sendMessageToAI(messageText, conversationId, userId, financialContext);
            }

            @Override
            public void onError(String errorMsg) {
                Log.w(TAG, "Failed to fetch financial context: " + errorMsg);
                sendMessageToAI(messageText, conversationId, userId, null);
            }
        });
    }

    private void sendMessageToAI(String messageText, String conversationId, String userId,
                                 FinancialContext financialContext) {
        repository.sendMessage(messageText, conversationId, getContextMessages(), financialContext,
                new ChatbotRepository.MessageCallback() {
                    @Override
                    public void onSuccess(String response) {
                        mainHandler.post(() -> handleAIResponse(response, conversationId, userId));
                    }

                    @Override
                    public void onError(String errorMsg) {
                        mainHandler.post(() -> {
                            loading.setValue(false);
                            error.setValue(errorMsg);
                            Log.e(TAG, "Error sending message: " + errorMsg);
                        });
                    }
                });
    }

    private void handleAIResponse(String response, String conversationId, String userId) {
        try {
            loading.setValue(false);
            ChatMessage assistantMessage = new ChatMessage(conversationId, response, ROLE_ASSISTANT, userId);
            addAssistantMessage(assistantMessage);
            saveAssistantMessage(assistantMessage, conversationId);
        } catch (Exception e) {
            Log.e(TAG, "Error handling AI response", e);
            loading.setValue(false);
            error.setValue("Error processing AI response");
        }
    }

    private void addAssistantMessage(ChatMessage assistantMessage) {
        ensureMessagesListInitialized();
        currentMessages.add(assistantMessage);
        messages.setValue(new ArrayList<>(currentMessages));
    }

    private void saveAssistantMessage(ChatMessage assistantMessage, String conversationId) {
        repository.saveMessage(assistantMessage, new ChatbotRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                updateConversationTimestamp(conversationId);
                checkAndGenerateTitle(conversationId);
                checkAndGenerateSummary(conversationId);
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Error saving assistant message: " + errorMsg);
            }
        });
    }

    private void checkAndGenerateTitle(String conversationId) {
        if (currentMessages != null && currentMessages.size() == 2) {
            generateConversationTitle(conversationId);
        }
    }

    private void checkAndGenerateSummary(String conversationId) {
        if (currentMessages != null && currentMessages.size() % 10 == 0) {
            generateConversationSummary(conversationId);
        }
    }

    /**
     * Starts a new conversation.
     */
    public void startNewConversation() {
        try {
            // Check if there are past conversations to potentially import context
            List<ChatConversation> pastConversations = conversations.getValue();
            if (pastConversations != null && !pastConversations.isEmpty()) {
                showContextDialog.setValue(true);
            }

            String userId = getCurrentUserId();
            if (userId == null) {
                error.setValue(ERROR_USER_NOT_AUTHENTICATED);
                return;
            }

            String newConversationId = UUID.randomUUID().toString();
            ChatConversation newConversation = new ChatConversation(userId);
            newConversation.setId(newConversationId);
            newConversation.setTitle("New Conversation");

            repository.saveConversation(newConversation,
                    new ChatbotRepository.SaveCallback() {
                        @Override
                        public void onSuccess() {
                            currentConversationId.setValue(newConversationId);
                            currentMessages = new ArrayList<>();
                            messages.setValue(new ArrayList<>());
                            error.setValue(null);
                            loadConversations(); // Refresh list
                        }

                        @Override
                        public void onError(String errorMsg) {
                            error.setValue("Error creating conversation: " + errorMsg);
                            Log.e(TAG, "Error creating conversation: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in startNewConversation", e);
            error.setValue("Error starting conversation. Please try again.");
        }
    }

    /**
     * Loads all conversations for the current user.
     */
    public void loadConversations() {
        repository.loadConversations(
                new ChatbotRepository.ConversationsCallback() {
                    @Override
                    public void onSuccess(List<ChatConversation> loadedConversations) {
                        conversations.setValue(loadedConversations);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e(TAG, "Error loading conversations: " + errorMsg);
                        // Don't set error to LiveData as this is background operation
                    }
                });
    }

    /**
     * Loads a specific conversation and its messages.
     *
     * @param conversationId the conversation ID to load
     */
    public void loadConversation(String conversationId) {
        try {
            if (conversationId == null || conversationId.isEmpty()) {
                error.setValue("Invalid conversation ID");
                return;
            }

            loading.setValue(true);
            currentConversationId.setValue(conversationId);

            repository.loadMessages(conversationId,
                    new ChatbotRepository.MessagesCallback() {
                        @Override
                        public void onSuccess(List<ChatMessage> loadedMessages) {
                            loading.setValue(false);
                            if (loadedMessages != null) {
                                currentMessages = new ArrayList<>(loadedMessages);
                                messages.setValue(new ArrayList<>(currentMessages));
                            } else {
                                currentMessages = new ArrayList<>();
                                messages.setValue(new ArrayList<>());
                            }
                            error.setValue(null);
                        }

                        @Override
                        public void onError(String errorMsg) {
                            loading.setValue(false);
                            // Don't show error for empty conversations
                            if (errorMsg != null && !errorMsg.contains("not found")) {
                                error.setValue("Error loading conversation: " + errorMsg);
                            }
                            currentMessages = new ArrayList<>();
                            messages.setValue(new ArrayList<>());
                            Log.e(TAG, "Error loading messages: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in loadConversation", e);
            loading.setValue(false);
            error.setValue("Error loading conversation. Please try again.");
        }
    }

    /**
     * Imports context from selected previous conversations (Phase 4).
     *
     * @param conversationIds the list of conversation IDs to import from
     */
    public void importContextFromConversations(List<String> conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            showContextDialog.setValue(false);
            return;
        }

        // Set imported conversation IDs in repository
        repository.setImportedConversationIds(conversationIds);
        
        // Merge context from imported conversations
        repository.mergeImportedConversationContext(conversationIds,
                new ChatbotRepository.MergedContextCallback() {
                    @Override
                    public void onSuccess(List<ChatMessage> mergedMessages) {
                        // Add merged messages to current context
                        if (mergedMessages != null && !mergedMessages.isEmpty()) {
                            if (currentMessages == null) {
                                currentMessages = new ArrayList<>();
                            }
                            // Prepend imported context to current messages
                            List<ChatMessage> combined = new ArrayList<>(mergedMessages);
                            combined.addAll(currentMessages);
                            currentMessages = combined;
                            messages.setValue(new ArrayList<>(currentMessages));
                        }
                        showContextDialog.setValue(false);
                    }
                });
    }

    /**
     * Gets context messages for API calls (last N messages).
     *
     * @return list of context messages
     */
    private List<ChatMessage> getContextMessages() {
        if (currentMessages == null || currentMessages.isEmpty()) {
            return new ArrayList<>();
        }
        // Limit to last 15 messages to prevent API issues with large contexts
        // This matches MAX_CONTEXT_MESSAGES in ChatbotRepository
        int maxContext = 15;
        int startIndex = Math.max(0, currentMessages.size() - maxContext);
        List<ChatMessage> context = new ArrayList<>(currentMessages.subList(startIndex,
                currentMessages.size()));
        
        // Log context size for debugging
        if (context.size() > 10) {
            Log.d(TAG, "Using " + context.size() + " messages as context");
        }
        
        return context;
    }

    /**
     * Updates the conversation's timestamp.
     *
     * @param conversationId the conversation ID
     */
    private void updateConversationTimestamp(String conversationId) {
        List<ChatConversation> convs = conversations.getValue();
        if (convs != null) {
            for (ChatConversation conv : convs) {
                if (conversationId.equals(conv.getId())) {
                    conv.updateTimestamp();
                    repository.saveConversation(conv,
                            new ChatbotRepository.SaveCallback() {
                                @Override
                                public void onSuccess() {
                                    loadConversations(); // Refresh
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    Log.e(TAG, "Error updating timestamp: " + errorMsg);
                                }
                            });
                    break;
                }
            }
        }
    }

    /**
     * Clears the current error message.
     */
    public void clearError() {
        error.setValue(null);
    }

    /**
     * Generates a title for the current conversation.
     *
     * @param conversationId the conversation ID
     */
    private void generateConversationTitle(String conversationId) {
        if (!canGenerateTitle()) {
            return;
        }

        if (isTitleAlreadyGenerated(conversationId)) {
            return;
        }

        repository.generateConversationTitle(currentMessages,
                new ChatbotRepository.MessageCallback() {
                    @Override
                    public void onSuccess(String title) {
                        updateConversationWithTitle(conversationId, title);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e(TAG, "Error generating title: " + errorMsg);
                    }
                });
    }

    private boolean canGenerateTitle() {
        return currentMessages != null && currentMessages.size() >= 2;
    }

    private boolean isTitleAlreadyGenerated(String conversationId) {
        List<ChatConversation> convs = conversations.getValue();
        if (convs == null) {
            return false;
        }

        for (ChatConversation conv : convs) {
            if (conversationId.equals(conv.getId())) {
                return isTitleValid(conv.getTitle());
            }
        }
        return false;
    }

    private boolean isTitleValid(String existingTitle) {
        if (existingTitle == null || existingTitle.isEmpty()) {
            return false;
        }
        if (existingTitle.equals("New Conversation")) {
            return false;
        }
        String lowerTitle = existingTitle.toLowerCase();
        return !lowerTitle.contains("i'm here to help") 
                && !lowerTitle.contains("i am here to help");
    }

    private void updateConversationWithTitle(String conversationId, String title) {
        List<ChatConversation> convs = conversations.getValue();
        if (convs == null) {
            return;
        }

        for (ChatConversation conv : convs) {
            if (conversationId.equals(conv.getId())) {
                conv.setTitle(title);
                saveConversationTitle(conv, title);
                break;
            }
        }
    }

    private void saveConversationTitle(ChatConversation conv, String title) {
        repository.saveConversation(conv, new ChatbotRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                loadConversations();
                Log.d(TAG, "Title generated: " + title);
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Error saving title: " + errorMsg);
            }
        });
    }

    /**
     * Generates a summary for the current conversation.
     *
     * @param conversationId the conversation ID
     */
    private void generateConversationSummary(String conversationId) {
        if (currentMessages == null || currentMessages.isEmpty()) {
            return;
        }

        repository.generateConversationSummary(currentMessages,
                new ChatbotRepository.MessageCallback() {
                    @Override
                    public void onSuccess(String summary) {
                        // Update conversation with generated summary
                        List<ChatConversation> convs = conversations.getValue();
                        if (convs != null) {
                            for (ChatConversation conv : convs) {
                                if (conversationId.equals(conv.getId())) {
                                    conv.setSummary(summary);
                                    repository.saveConversation(conv,
                                            new ChatbotRepository.SaveCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    loadConversations(); // Refresh list
                                                }

                                                @Override
                                                public void onError(String errorMsg) {
                                                    Log.e(TAG, "Error saving summary: " + errorMsg);
                                                }
                                            });
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e(TAG, "Error generating summary: " + errorMsg);
                    }
                });
    }

    /**
     * Gets the current user ID.
     *
     * @return the user ID or null
     */
    private String getCurrentUserId() {
        if (currentUserId == null) {
            com.example.sprintproject.FirestoreManager firestoreManager =
                    com.example.sprintproject.FirestoreManager.getInstance();
            if (firestoreManager.getAuth().getCurrentUser() != null) {
                currentUserId = firestoreManager.getAuth()
                        .getCurrentUser().getUid();
            }
        }
        return currentUserId;
    }

    /**
     * Executes a custom command (Phase 6).
     *
     * @param commandType the type of command to execute
     */
    public void executeCommand(ChatCommandParser.CommandType commandType) {
        String userId = getCurrentUserId();
        if (userId == null) {
            error.setValue(ERROR_USER_NOT_AUTHENTICATED);
            return;
        }

        String conversationId = currentConversationId.getValue();
        if (conversationId == null || conversationId.isEmpty()) {
            startNewConversation();
            conversationId = currentConversationId.getValue();
        }

        if (conversationId == null) {
            error.setValue("Failed to create conversation");
            return;
        }

        final String finalConversationId = conversationId;
        loading.setValue(true);
        error.setValue(null);

        // Fetch financial context for command execution
        repository.fetchFinancialContext(userId,
                new ChatbotRepository.FinancialContextCallback() {
                    @Override
                    public void onSuccess(FinancialContext financialContext) {
                        String response = generateCommandResponse(commandType, financialContext);
                        
                        // Post to main thread
                        mainHandler.post(() -> {
                            try {
                                loading.setValue(false);

                                // Create assistant message with command response
                                ChatMessage assistantMessage = new ChatMessage(
                                        finalConversationId, response, ROLE_ASSISTANT, userId);

                                // Add assistant message
                                if (currentMessages == null) {
                                    currentMessages = new ArrayList<>();
                                }
                                currentMessages.add(assistantMessage);
                                messages.setValue(new ArrayList<>(currentMessages));

                                // Save assistant message
                                repository.saveMessage(assistantMessage,
                                        new ChatbotRepository.SaveCallback() {
                                            @Override
                                            public void onSuccess() {
                                                updateConversationTimestamp(finalConversationId);
                                            }

                                            @Override
                                            public void onError(String errorMsg) {
                                                Log.e(TAG, "Error saving command response: " + errorMsg);
                                            }
                                        });
                            } catch (Exception e) {
                                Log.e(TAG, "Error handling command response", e);
                                loading.setValue(false);
                                error.setValue("Error processing command");
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMsg) {
                        mainHandler.post(() -> {
                            loading.setValue(false);
                            error.setValue("Error fetching financial data: " + errorMsg);
                        });
                    }
                });
    }

    /**
     * Generates a response for a command based on financial context.
     *
     * @param commandType the command type
     * @param financialContext the financial context
     * @return the generated response
     */
    private String generateCommandResponse(ChatCommandParser.CommandType commandType,
                                           FinancialContext financialContext) {
        if (financialContext == null || !financialContext.hasData()) {
            return "I don't have access to your financial data yet. Please make sure you have expenses, budgets, or savings goals recorded.";
        }

        switch (commandType) {
            case SUMMARIZE_SPENDING:
                return generateSpendingSummary(financialContext);
            case COST_CUT_SUGGESTIONS:
                return generateCostCutSuggestions(financialContext);
            case MONTHLY_COMPARISON:
                return generateMonthlyComparison(financialContext);
            case BUDGET_STATUS:
                return generateBudgetStatus(financialContext);
            case SAVINGS_GOALS:
                return generateSavingsGoals(financialContext);
            default:
                return "Unknown command. Please try again.";
        }
    }

    /**
     * Generates spending summary response.
     */
    private String generateSpendingSummary(FinancialContext context) {
        if (hasNoExpenses(context)) {
            return "You haven't recorded any expenses yet. Start tracking your spending to see summaries!";
        }

        StringBuilder response = new StringBuilder();
        response.append("📊 **Spending Summary (Current Month)**%n%n");
        
        String summary = getSpendingSummaryText(context);
        response.append(summary).append("%n%n");
        
        addTopExpenses(response, context.getRecentExpenses());
        return response.toString();
    }

    private boolean hasNoExpenses(FinancialContext context) {
        return context.getRecentExpenses() == null || context.getRecentExpenses().isEmpty();
    }

    private String getSpendingSummaryText(FinancialContext context) {
        Date monthStart = FinancialDataAggregator.getCurrentMonthStart();
        return FinancialDataAggregator.getSpendingSummary(
                context.getRecentExpenses(), monthStart, new Date());
    }

    private void addTopExpenses(StringBuilder response, List<com.example.sprintproject.model.Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return;
        }
        
        response.append("**Top Expenses:**%n");
        int count = Math.min(5, expenses.size());
        for (int i = 0; i < count; i++) {
            com.example.sprintproject.model.Expense exp = expenses.get(i);
            if (exp != null) {
                response.append(formatExpenseEntry(exp));
            }
        }
    }

    private String formatExpenseEntry(com.example.sprintproject.model.Expense exp) {
        String name = exp.getName() != null ? exp.getName() : "Unnamed";
        String category = exp.getCategory() != null ? exp.getCategory() : "Uncategorized";
        return String.format("• %s: $%.2f (%s)%n", name, exp.getAmount(), category);
    }

    /**
     * Generates cost-cutting suggestions response.
     */
    private String generateCostCutSuggestions(FinancialContext context) {
        List<String> suggestions = FinancialDataAggregator.getCostCutSuggestions(
                context.getRecentExpenses(),
                context.getActiveBudgets());

        StringBuilder response = new StringBuilder();
        response.append("💰 **Ways to Save Money**\n\n");

        for (int i = 0; i < suggestions.size(); i++) {
            response.append(String.format("%d. %s%n", i + 1, suggestions.get(i)));
        }

        return response.toString();
    }

    /**
     * Generates monthly comparison response.
     */
    private String generateMonthlyComparison(FinancialContext context) {
        if (context.getRecentExpenses() == null || context.getRecentExpenses().isEmpty()) {
            return "You don't have enough expense data to compare months. Start tracking expenses to see comparisons!";
        }

        Date currentMonthStart = FinancialDataAggregator.getCurrentMonthStart();
        Date lastMonthStart = FinancialDataAggregator.getLastMonthStart();
        Date lastMonthEnd = FinancialDataAggregator.getLastMonthEnd();

        List<com.example.sprintproject.model.Expense> currentMonthExpenses =
                FinancialDataAggregator.filterExpensesByDate(
                        context.getRecentExpenses(), currentMonthStart, new Date());
        List<com.example.sprintproject.model.Expense> lastMonthExpenses =
                FinancialDataAggregator.filterExpensesByDate(
                        context.getRecentExpenses(), lastMonthStart, lastMonthEnd);

        String comparison = FinancialDataAggregator.getMonthlyComparison(
                currentMonthExpenses, lastMonthExpenses);

        StringBuilder response = new StringBuilder();
        response.append("📈 **Monthly Spending Comparison**\n\n");
        response.append(comparison);

        return response.toString();
    }

    /**
     * Generates budget status response.
     */
    private String generateBudgetStatus(FinancialContext context) {
        if (context.getActiveBudgets() == null || context.getActiveBudgets().isEmpty()) {
            return "You don't have any active budgets. Create budgets to track your spending goals!";
        }

        StringBuilder response = new StringBuilder();
        response.append("📋 **Budget Status**\n\n");

        for (com.example.sprintproject.model.Budget budget : context.getActiveBudgets()) {
            if (budget != null) {
                double utilization = budget.getTotalAmount() > 0
                        ? (budget.getSpentAmount() / budget.getTotalAmount()) * 100 : 0;
                double remaining = budget.getTotalAmount() - budget.getSpentAmount();
                
                String status = getBudgetStatusText(utilization);

                response.append(String.format("**%s**\n", budget.getCategory()));
                response.append(String.format("Budget: $%.2f | Spent: $%.2f | Remaining: $%.2f%n",
                        budget.getTotalAmount(), budget.getSpentAmount(), remaining));
                response.append(String.format("Utilization: %.1f%% %s%n%n",
                        utilization, status));
            }
        }

        return response.toString();
    }

    /**
     * Generates savings goals response.
     */
    private String generateSavingsGoals(FinancialContext context) {
        if (context.getActiveSavingsCircles() == null
                || context.getActiveSavingsCircles().isEmpty()) {
            return "You don't have any active savings goals. Create savings circles to track your progress!";
        }

        StringBuilder response = new StringBuilder();
        response.append("🎯 **Active Savings Goals**\n\n");

        for (com.example.sprintproject.model.SavingsCircle circle
                : context.getActiveSavingsCircles()) {
            if (circle != null) {
                response.append(String.format("**%s**%n", circle.getChallengeTitle()));
                response.append(String.format("Goal: $%.2f%n", circle.getGoalAmount()));
                response.append(String.format("Frequency: %s%n%n", circle.getFrequency()));
            }
        }

        return response.toString();
    }

    /**
     * Gets the budget status text based on utilization percentage.
     *
     * @param utilization the utilization percentage
     * @return the status text
     */
    private String getBudgetStatusText(double utilization) {
        if (utilization >= 100) {
            return "⚠️ Over budget";
        } else if (utilization >= 80) {
            return "⚠️ Close to limit";
        } else {
            return "✅ On track";
        }
    }
}

