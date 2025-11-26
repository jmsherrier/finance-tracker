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
            if (messageText == null || messageText.trim().isEmpty()) {
                error.setValue("Message cannot be empty");
                return;
            }

            String conversationId = currentConversationId.getValue();
            if (conversationId == null || conversationId.isEmpty()) {
                // Start new conversation
                startNewConversation();
                conversationId = currentConversationId.getValue();
            }

            if (conversationId == null) {
                error.setValue("Failed to create conversation");
                return;
            }

            final String finalConversationId = conversationId;
            String userId = getCurrentUserId();
            if (userId == null) {
                error.setValue("User not authenticated");
                return;
            }

            // Create user message
            ChatMessage userMessage = new ChatMessage(
                    finalConversationId, messageText.trim(), "user", userId);

            // Add user message to list immediately
            if (currentMessages == null) {
                currentMessages = new ArrayList<>();
            }
            currentMessages.add(userMessage);
            messages.setValue(new ArrayList<>(currentMessages));

            // Save user message
            repository.saveMessage(userMessage, new ChatbotRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                // Message saved, now fetch financial context and send to AI (Phase 5)
                loading.setValue(true);
                error.setValue(null);

                // Fetch financial context for personalized responses
                repository.fetchFinancialContext(userId,
                        new ChatbotRepository.FinancialContextCallback() {
                            @Override
                            public void onSuccess(FinancialContext financialContext) {
                                // Send message with financial context
                                repository.sendMessage(messageText.trim(), finalConversationId,
                                        getContextMessages(), financialContext,
                                        new ChatbotRepository.MessageCallback() {
                            @Override
                            public void onSuccess(String response) {
                                // Post to main thread for thread safety
                                mainHandler.post(() -> {
                                    try {
                                        loading.setValue(false);

                                        // Create assistant message
                                        ChatMessage assistantMessage = new ChatMessage(
                                                finalConversationId, response, "assistant", userId);

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
                                                        // Update conversation timestamp
                                                        updateConversationTimestamp(finalConversationId);
                                                        
                                                        // Generate title after exactly 2 messages (user + assistant)
                                                        if (currentMessages.size() == 2) {
                                                            generateConversationTitle(finalConversationId);
                                                        }
                                                        
                                                        // Generate summary periodically (every 10 messages)
                                                        if (currentMessages.size() % 10 == 0) {
                                                            generateConversationSummary(finalConversationId);
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(String errorMsg) {
                                                        Log.e(TAG, "Error saving assistant message: "
                                                                + errorMsg);
                                                    }
                                                });
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error handling AI response", e);
                                        loading.setValue(false);
                                        error.setValue("Error processing AI response");
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMsg) {
                                // Post to main thread for thread safety
                                mainHandler.post(() -> {
                                    loading.setValue(false);
                                    error.setValue(errorMsg);
                                    Log.e(TAG, "Error sending message: " + errorMsg);
                                });
                            }
                        });
                            }

                            @Override
                            public void onError(String errorMsg) {
                                // If financial context fetch fails, send message without it
                                Log.w(TAG, "Failed to fetch financial context: " + errorMsg);
                                repository.sendMessage(messageText.trim(), finalConversationId,
                                        getContextMessages(), null,
                                        new ChatbotRepository.MessageCallback() {
                                            @Override
                                            public void onSuccess(String response) {
                                                // Post to main thread for thread safety
                                                mainHandler.post(() -> {
                                                    try {
                                                        loading.setValue(false);

                                                        // Create assistant message
                                                        ChatMessage assistantMessage = new ChatMessage(
                                                                finalConversationId, response, "assistant", userId);

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
                                                                        // Update conversation timestamp
                                                                        updateConversationTimestamp(finalConversationId);
                                                                        
                                                                        // Generate title after exactly 2 messages (user + assistant)
                                                                        if (currentMessages.size() == 2) {
                                                                            generateConversationTitle(finalConversationId);
                                                                        }
                                                                        
                                                                        // Generate summary periodically (every 10 messages)
                                                                        if (currentMessages.size() % 10 == 0) {
                                                                            generateConversationSummary(finalConversationId);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onError(String errorMsg) {
                                                                        Log.e(TAG, "Error saving assistant message: "
                                                                                + errorMsg);
                                                                    }
                                                                });
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "Error handling AI response", e);
                                                        loading.setValue(false);
                                                        error.setValue("Error processing AI response");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(String errorMsg) {
                                                // Post to main thread for thread safety
                                                mainHandler.post(() -> {
                                                    loading.setValue(false);
                                                    error.setValue("Error: " + errorMsg);
                                                });
                                            }
                                        });
                            }
                        });
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue("Error saving message: " + errorMsg);
                Log.e(TAG, "Error saving user message: " + errorMsg);
            }
        });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in sendMessage", e);
            loading.setValue(false);
            error.setValue("An unexpected error occurred. Please try again.");
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
                error.setValue("User not authenticated");
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
        if (currentMessages == null || currentMessages.size() < 2) {
            return; // Need at least 2 messages (user + assistant)
        }

        // Check if title is already generated (not "New Conversation")
        List<ChatConversation> convs = conversations.getValue();
        if (convs != null) {
            for (ChatConversation conv : convs) {
                if (conversationId.equals(conv.getId())) {
                    String existingTitle = conv.getTitle();
                    if (existingTitle != null && !existingTitle.isEmpty()
                            && !existingTitle.equals("New Conversation")
                            && !existingTitle.toLowerCase().contains("i'm here to help")
                            && !existingTitle.toLowerCase().contains("i am here to help")) {
                        // Title already generated, skip
                        return;
                    }
                    break;
                }
            }
        }

        repository.generateConversationTitle(currentMessages,
                new ChatbotRepository.MessageCallback() {
                    @Override
                    public void onSuccess(String title) {
                        // Update conversation with generated title
                        List<ChatConversation> convs = conversations.getValue();
                        if (convs != null) {
                            for (ChatConversation conv : convs) {
                                if (conversationId.equals(conv.getId())) {
                                    conv.setTitle(title);
                                    repository.saveConversation(conv,
                                            new ChatbotRepository.SaveCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    loadConversations(); // Refresh list
                                                    Log.d(TAG, "Title generated: " + title);
                                                }

                                                @Override
                                                public void onError(String errorMsg) {
                                                    Log.e(TAG, "Error saving title: " + errorMsg);
                                                }
                                            });
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e(TAG, "Error generating title: " + errorMsg);
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
}

