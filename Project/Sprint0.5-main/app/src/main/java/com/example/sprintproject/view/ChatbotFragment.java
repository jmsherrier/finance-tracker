package com.example.sprintproject.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.adapter.ChatMessageAdapter;
import com.example.sprintproject.adapter.ConversationListAdapter;
import com.example.sprintproject.model.ChatConversation;
import com.example.sprintproject.model.ChatMessage;
import com.example.sprintproject.viewmodel.ChatbotViewModel;
import com.example.sprintproject.viewmodel.TimeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment handling chatbot UI and user interactions.
 * Displays chat messages and handles sending messages to AI.
 */
public class ChatbotFragment extends Fragment {

    private static final String TAG = "ChatbotFragment";

    private ChatbotViewModel chatbotViewModel;
    private TimeViewModel timeViewModel;
    private ChatMessageAdapter messageAdapter;
    private RecyclerView recyclerMessages;
    private TextInputEditText editMessageInput;
    private FloatingActionButton fabSend;
    private LinearLayout layoutEmptyState;
    private TextView textEmptyMessage;
    private TextView textError;
    private ProgressBar progressLoading;
    private MaterialButton btnConversations;
    private MaterialButton btnImportContext;
    private MaterialButton btnCmdSummarize;
    private MaterialButton btnCmdSaveMoney;
    private MaterialButton btnCmdCompare;
    private MaterialButton btnCmdBudget;
    
    // Conversation list views
    private View conversationListView;
    private RecyclerView recyclerConversationsMain;
    private MaterialButton btnNewConversationMain;
    private LinearLayout layoutEmptyConversations;
    private ConversationListAdapter conversationListAdapter;
    
    // Chat view (existing)
    private View chatView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Create a FrameLayout container
        android.widget.FrameLayout rootView = new android.widget.FrameLayout(requireContext());
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Inflate conversation list view
        conversationListView = inflater.inflate(R.layout.fragment_conversation_list, rootView, false);
        
        // Inflate chat view
        chatView = inflater.inflate(R.layout.activity_chatbot, rootView, false);
        
        // Add both views to container
        rootView.addView(conversationListView);
        rootView.addView(chatView);
        
        // Initially show conversation list, hide chat view
        conversationListView.setVisibility(View.VISIBLE);
        chatView.setVisibility(View.GONE);

        View view = rootView;

        // Initialize ViewModels
        chatbotViewModel = new ViewModelProvider(requireActivity())
                .get(ChatbotViewModel.class);

        timeViewModel = new ViewModelProvider(requireActivity())
                .get(TimeViewModel.class);

        // Set Gemini API key from BuildConfig (loaded from local.properties)
        String apiKey = com.example.sprintproject.BuildConfig.GEMINI_API_KEY;
        if (apiKey != null && !apiKey.isEmpty()) {
            chatbotViewModel.setApiKey(apiKey);
            Log.d(TAG, "Gemini API key configured from BuildConfig");
        } else {
            Log.w(TAG, "Gemini API key not found in BuildConfig. "
                    + "Please add GEMINI_API_KEY to local.properties");
        }

        // Initialize views for both conversation list and chat
        initializeConversationListView(conversationListView);
        initializeChatView(chatView);

        // Setup RecyclerView for chat messages
        setupRecyclerView();

        // Setup observers
        setupObservers();

        // Setup click listeners
        setupClickListeners();

        // Load conversations on start
        chatbotViewModel.loadConversations();

        return view;
    }

    /**
     * Initializes conversation list view references.
     *
     * @param view the conversation list view
     */
    private void initializeConversationListView(View view) {
        recyclerConversationsMain = view.findViewById(R.id.recycler_conversations_main);
        btnNewConversationMain = view.findViewById(R.id.btn_new_conversation_main);
        layoutEmptyConversations = view.findViewById(R.id.layout_empty_conversations);
        
        // Setup conversation list RecyclerView
        conversationListAdapter = new ConversationListAdapter();
        conversationListAdapter.setOnConversationClickListener(conversation -> {
            if (conversation != null && conversation.getId() != null) {
                // Switch to chat view and load conversation
                showChatView(conversation.getId());
            }
        });
        
        recyclerConversationsMain.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConversationsMain.setAdapter(conversationListAdapter);
        
        // Setup new conversation button
        btnNewConversationMain.setOnClickListener(v -> {
            chatbotViewModel.startNewConversation();
        });
    }

    /**
     * Initializes chat view references.
     *
     * @param view the chat view
     */
    private void initializeChatView(View view) {
        recyclerMessages = view.findViewById(R.id.recycler_messages);
        editMessageInput = view.findViewById(R.id.edit_message_input);
        fabSend = view.findViewById(R.id.fab_send);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        textEmptyMessage = view.findViewById(R.id.text_empty_message);
        textError = view.findViewById(R.id.text_error);
        progressLoading = view.findViewById(R.id.progress_loading);
        btnConversations = view.findViewById(R.id.btn_conversations);
        btnImportContext = view.findViewById(R.id.btn_import_context);
        btnCmdSummarize = view.findViewById(R.id.btn_cmd_summarize);
        btnCmdSaveMoney = view.findViewById(R.id.btn_cmd_save_money);
        btnCmdCompare = view.findViewById(R.id.btn_cmd_compare);
        btnCmdBudget = view.findViewById(R.id.btn_cmd_budget);
    }

    /**
     * Sets up the RecyclerView for messages.
     */
    private void setupRecyclerView() {
        messageAdapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Start from bottom
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(messageAdapter);
    }

    /**
     * Sets up LiveData observers.
     */
    private void setupObservers() {
        // Observe messages
        chatbotViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                messageAdapter.updateMessages(messages);
                updateUI();
                scrollToBottom();
            }
        });

        // Observe loading state
        chatbotViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                fabSend.setEnabled(!isLoading);
            }
        });

        // Observe errors
        chatbotViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                if (textError != null) {
                    textError.setText(error);
                    textError.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error displayed: " + error);
                    // Auto-hide error after 8 seconds (longer for API errors)
                    textError.postDelayed(() -> {
                        if (textError != null) {
                            textError.setVisibility(View.GONE);
                        }
                    }, 8000);
                }
            } else {
                if (textError != null) {
                    textError.setVisibility(View.GONE);
                }
            }
        });

        // Observe date changes (for future use)
        timeViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt =
                    new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Log.d(TAG, "Date changed to " + fmt.format(date));
            // Date change handling can be added here if needed
        });

        // Observe conversations to update the list
        chatbotViewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            if (conversationListAdapter != null) {
                conversationListAdapter.updateConversations(conversations);
            }
            
            // Show/hide empty state
            if (layoutEmptyConversations != null && recyclerConversationsMain != null) {
                if (conversations == null || conversations.isEmpty()) {
                    layoutEmptyConversations.setVisibility(View.VISIBLE);
                    recyclerConversationsMain.setVisibility(View.GONE);
                } else {
                    layoutEmptyConversations.setVisibility(View.GONE);
                    recyclerConversationsMain.setVisibility(View.VISIBLE);
                }
            }
        });

        // Observe conversation ID to switch to chat view when a new conversation is created
        chatbotViewModel.getCurrentConversationId().observe(getViewLifecycleOwner(), conversationId -> {
            // Only switch if we're currently on the list view and a conversation was just created
            if (conversationId != null && !conversationId.isEmpty()
                    && conversationListView != null
                    && conversationListView.getVisibility() == View.VISIBLE) {
                // Check if this is a new conversation (not just loading an existing one)
                // We'll switch to chat view
                showChatView(conversationId);
            }
        });
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private void setupClickListeners() {
        // Send button click
        fabSend.setOnClickListener(v -> sendMessage());

        // Send on Enter key (when multiline input)
        editMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && !event.isShiftPressed()) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Conversation history button - switch back to list view
        btnConversations.setOnClickListener(v -> showConversationListView());

        // Import context button (Phase 4)
        if (btnImportContext != null) {
            btnImportContext.setOnClickListener(v -> showContextImportDialog());
        }

        // Command buttons (Phase 6)
        if (btnCmdSummarize != null) {
            btnCmdSummarize.setOnClickListener(v -> {
                chatbotViewModel.executeCommand(
                        com.example.sprintproject.utils.ChatCommandParser.CommandType.SUMMARIZE_SPENDING);
            });
        }

        if (btnCmdSaveMoney != null) {
            btnCmdSaveMoney.setOnClickListener(v -> {
                chatbotViewModel.executeCommand(
                        com.example.sprintproject.utils.ChatCommandParser.CommandType.COST_CUT_SUGGESTIONS);
            });
        }

        if (btnCmdCompare != null) {
            btnCmdCompare.setOnClickListener(v -> {
                chatbotViewModel.executeCommand(
                        com.example.sprintproject.utils.ChatCommandParser.CommandType.MONTHLY_COMPARISON);
            });
        }

        if (btnCmdBudget != null) {
            btnCmdBudget.setOnClickListener(v -> {
                chatbotViewModel.executeCommand(
                        com.example.sprintproject.utils.ChatCommandParser.CommandType.BUDGET_STATUS);
            });
        }
    }

    /**
     * Sends the current message to the AI.
     */
    private void sendMessage() {
        try {
            String messageText = editMessageInput.getText() != null
                    ? editMessageInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(messageText)) {
                return; // Don't send empty messages
            }

            // Clear input
            editMessageInput.setText("");

            // Send message via ViewModel (with error handling)
            chatbotViewModel.sendMessage(messageText);

            // Clear any previous errors
            chatbotViewModel.clearError();
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            if (textError != null) {
                textError.setText("Error sending message. Please try again.");
                textError.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Updates UI visibility based on message state.
     */
    private void updateUI() {
        List<ChatMessage> messages = chatbotViewModel.getMessages().getValue();
        boolean hasMessages = messages != null && !messages.isEmpty();

        if (hasMessages) {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerMessages.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerMessages.setVisibility(View.GONE);
        }
    }

    /**
     * Scrolls the RecyclerView to the bottom to show latest message.
     */
    private void scrollToBottom() {
        if (recyclerMessages != null && messageAdapter != null) {
            int itemCount = messageAdapter.getItemCount();
            if (itemCount > 0) {
                recyclerMessages.post(() -> {
                    if (recyclerMessages != null) {
                        recyclerMessages.smoothScrollToPosition(itemCount - 1);
                    }
                });
            }
        }
    }

    /**
     * Shows the conversation list view and hides the chat view.
     */
    private void showConversationListView() {
        if (conversationListView != null && chatView != null) {
            conversationListView.setVisibility(View.VISIBLE);
            chatView.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the chat view and loads a conversation.
     *
     * @param conversationId the conversation ID to load
     */
    private void showChatView(String conversationId) {
        if (conversationListView != null && chatView != null) {
            conversationListView.setVisibility(View.GONE);
            chatView.setVisibility(View.VISIBLE);
            
            // Load the conversation
            if (conversationId != null && !conversationId.isEmpty()) {
                chatbotViewModel.loadConversation(conversationId);
            }
        }
    }

    /**
     * Shows a dialog with conversation history (kept for backward compatibility).
     */
    private void showConversationHistoryDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_conversation_list, null);

        RecyclerView recyclerConversations = dialogView.findViewById(R.id.recycler_conversations);
        MaterialButton btnNewConversation = dialogView.findViewById(R.id.btn_new_conversation);
        MaterialButton btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true);
        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();

        ConversationListAdapter adapter = new ConversationListAdapter();
        adapter.setOnConversationClickListener(conversation -> {
            if (conversation != null && conversation.getId() != null) {
                dialog.dismiss();
                showChatView(conversation.getId());
            }
        });

        recyclerConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConversations.setAdapter(adapter);

        // Observe conversations and update adapter
        chatbotViewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null) {
                adapter.updateConversations(conversations);
            }
        });

        btnNewConversation.setOnClickListener(v -> {
            dialog.dismiss();
            chatbotViewModel.startNewConversation();
            String newConversationId = chatbotViewModel.getCurrentConversationId().getValue();
            if (newConversationId != null) {
                showChatView(newConversationId);
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Shows dialog to import context from previous conversations (Phase 4).
     */
    private void showContextImportDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_context_selection, null);

        RecyclerView recyclerContextConversations = dialogView.findViewById(R.id.recycler_context_conversations);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_context);
        MaterialButton btnImport = dialogView.findViewById(R.id.btn_import_context);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle("Import Context")
                .setCancelable(true);
        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();

        // Use ConversationListAdapter with selection capability
        ConversationListAdapter adapter = new ConversationListAdapter();
        recyclerContextConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerContextConversations.setAdapter(adapter);

        // Observe conversations and update adapter
        chatbotViewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null) {
                // Filter out current conversation
                String currentId = chatbotViewModel.getCurrentConversationId().getValue();
                List<ChatConversation> filtered = new ArrayList<>();
                for (ChatConversation conv : conversations) {
                    if (conv != null && !conv.getId().equals(currentId)) {
                        filtered.add(conv);
                    }
                }
                adapter.updateConversations(filtered);
            }
        });

        // Track selected conversation IDs
        List<String> selectedIds = new ArrayList<>();
        adapter.setOnConversationClickListener(conversation -> {
            if (conversation != null && conversation.getId() != null) {
                // Toggle selection
                if (selectedIds.contains(conversation.getId())) {
                    selectedIds.remove(conversation.getId());
                } else {
                    selectedIds.add(conversation.getId());
                }
                // Update UI to show selection (you can enhance this with visual feedback)
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnImport.setOnClickListener(v -> {
            if (!selectedIds.isEmpty()) {
                chatbotViewModel.importContextFromConversations(selectedIds);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
