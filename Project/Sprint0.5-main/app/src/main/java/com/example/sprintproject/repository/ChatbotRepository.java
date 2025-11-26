package com.example.sprintproject.repository;

import android.util.Log;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.ChatConversation;
import com.example.sprintproject.model.ChatMessage;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.SavingsCircle;
import com.example.sprintproject.repository.FinancialContext;
import com.example.sprintproject.utils.FinancialDataAggregator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Repository for managing chatbot interactions with AI API and Firestore.
 * Handles API calls, conversation management, and data persistence.
 */
public class ChatbotRepository {
    private static final String TAG = "ChatbotRepository";
    // Gemini API endpoint - using v1beta (most stable for current models)
    private static final String API_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models";
    // Using Gemini 2.5 Flash - latest model, fastest, most reliable
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final int MAX_CONTEXT_MESSAGES = 15; // Reduced to prevent API issues with large contexts
    private static final int API_TIMEOUT_SECONDS = 30;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private String apiKey;
    private List<String> importedConversationIds; // For Phase 4: context import

    /**
     * Constructor initializes Firestore and HTTP client.
     */
    public ChatbotRepository() {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        this.db = firestoreManager.getDb();
        this.auth = firestoreManager.getAuth();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(API_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(API_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(API_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Sets the API key for Gemini API.
     *
     * @param apiKey the API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Sets conversation IDs to import context from (Phase 4).
     *
     * @param conversationIds list of conversation IDs
     */
    public void setImportedConversationIds(List<String> conversationIds) {
        this.importedConversationIds = conversationIds != null
                ? new ArrayList<>(conversationIds) : new ArrayList<>();
    }

    /**
     * Sends a message to the AI API and returns the response.
     *
     * @param message the user's message
     * @param conversationId the conversation ID
     * @param contextMessages previous messages for context
     * @param callback callback to handle the response or error
     */
    public void sendMessage(String message, String conversationId,
                            List<ChatMessage> contextMessages,
                            MessageCallback callback) {
        sendMessage(message, conversationId, contextMessages, null, callback);
    }

    /**
     * Sends a message to the AI chatbot with optional financial context.
     *
     * @param message the user's message
     * @param conversationId the conversation ID
     * @param contextMessages previous messages for context
     * @param financialContext optional financial context (Phase 5)
     * @param callback callback to handle the response or error
     */
    public void sendMessage(String message, String conversationId,
                            List<ChatMessage> contextMessages,
                            FinancialContext financialContext,
                            MessageCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key not configured. Please set API key.");
            return;
        }

        try {
            String requestBody = buildGeminiRequestBody(message, contextMessages, financialContext);

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/" + DEFAULT_MODEL + ":generateContent?key=" + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    callback.onError("Failed to connect to AI service: "
                            + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {
                    if (!response.isSuccessful()) {
                        String errorMsg = "API error: " + response.code();
                        String responseBody = "";
                        if (response.body() != null) {
                            try {
                                responseBody = response.body().string();
                                errorMsg += " - " + responseBody;
                            } catch (Exception e) {
                                errorMsg += " - Unable to read error response";
                            }
                        }
                        
                        // Provide user-friendly error messages
                        if (response.code() == 503) {
                            errorMsg = "AI service is temporarily unavailable. Please try again in a moment.";
                        } else if (response.code() == 429) {
                            errorMsg = "Rate limit exceeded. Please try again in a moment.";
                        } else if (response.code() == 401 || response.code() == 403) {
                            errorMsg = "API key is invalid or unauthorized. Please check your API key.";
                        } else if (response.code() == 400) {
                            errorMsg = "Invalid request. Please try rephrasing your message.";
                        } else if (response.code() == 404) {
                            // Try alternative model or API version
                            Log.w(TAG, "Model not found (404), trying alternative models");
                            // Note: financialContext is not available in this callback context
                            // Fallback models will work without financial context
                            tryAlternativeGeminiModel(message, conversationId, contextMessages, null, callback);
                            return;
                        }
                        
                        Log.e(TAG, "API error response: " + responseBody);
                        callback.onError(errorMsg);
                        return;
                    }

                    try {
                        String responseBody = response.body() != null
                                ? response.body().string() : "";
                        
                        // Log response for debugging (truncated to avoid spam)
                        if (responseBody != null && !responseBody.isEmpty()) {
                            String preview = responseBody.length() > 300 
                                    ? responseBody.substring(0, 300) + "..." 
                                    : responseBody;
                            Log.d(TAG, "API response preview: " + preview);
                        }
                        
                        String aiResponse = parseGeminiResponse(responseBody);
                        
                        // Check if we got a fallback response indicating parsing issues
                        if (aiResponse != null && 
                                (aiResponse.contains("unexpected response format") 
                                 || aiResponse.contains("I received an unexpected"))) {
                            Log.w(TAG, "Received fallback response - API may have returned unexpected format");
                            Log.w(TAG, "Full response body length: " + 
                                    (responseBody != null ? responseBody.length() : 0));
                            // Log full response for debugging (but truncate if too long)
                            if (responseBody != null) {
                                String fullLog = responseBody.length() > 1000 
                                        ? responseBody.substring(0, 1000) + "...[truncated]" 
                                        : responseBody;
                                Log.w(TAG, "Full response: " + fullLog);
                            }
                        }
                        
                        callback.onSuccess(aiResponse);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing API response", e);
                        // Try to get response body for error logging (but it may already be consumed)
                        callback.onError("Error processing AI response: "
                                + e.getMessage() + ". Please try again.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            callback.onError("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Fetches financial context for the current user (Phase 5).
     *
     * @param userId the user ID
     * @param callback callback to return FinancialContext
     */
    public void fetchFinancialContext(String userId,
                                       FinancialContextCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("User ID is required");
            return;
        }

        FinancialContext context = new FinancialContext();
        final int[] completedTasks = {0};
        final int totalTasks = 3;
        final String[] errorMessage = {null};

        // Fetch expenses - try both possible Firestore structures
        // First try: expenses collection with userId field (used by ExpenseLogFragment)
        // Use final containers to allow modification in nested lambdas
        final List<Expense> expenses = new ArrayList<>();
        final Map<String, Double> expensesByCategory = new HashMap<>();
        final double[] totalExpenses = {0.0};
        
        db.collection("expenses")
                .whereEqualTo("userId", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(expenseSnapshots -> {
                    for (DocumentSnapshot doc : expenseSnapshots.getDocuments()) {
                        Expense expense = doc.toObject(Expense.class);
                        if (expense != null) {
                            expense.setId(doc.getId());
                            expenses.add(expense);
                            totalExpenses[0] += expense.getAmount();
                            String category = expense.getCategory() != null
                                    ? expense.getCategory() : "Uncategorized";
                            expensesByCategory.put(category,
                                    expensesByCategory.getOrDefault(category, 0.0)
                                            + expense.getAmount());
                        }
                    }

                    // If no expenses found in top-level collection, try subcollection
                    if (expenses.isEmpty()) {
                        db.collection("users").document(userId).collection("expenses")
                                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(50)
                                .get()
                                .addOnSuccessListener(subExpenseSnapshots -> {
                                    for (DocumentSnapshot doc : subExpenseSnapshots.getDocuments()) {
                                        Expense expense = doc.toObject(Expense.class);
                                        if (expense != null) {
                                            expense.setId(doc.getId());
                                            expenses.add(expense);
                                            totalExpenses[0] += expense.getAmount();
                                            String category = expense.getCategory() != null
                                                    ? expense.getCategory() : "Uncategorized";
                                            expensesByCategory.put(category,
                                                    expensesByCategory.getOrDefault(category, 0.0)
                                                            + expense.getAmount());
                                        }
                                    }
                                    
                                    context.setRecentExpenses(expenses);
                                    context.setExpensesByCategory(expensesByCategory);
                                    context.setTotalExpenses(totalExpenses[0]);
                                    
                                    Log.d(TAG, "Fetched " + expenses.size() + " expenses from subcollection");
                                    
                                    completedTasks[0]++;
                                    if (completedTasks[0] == totalTasks) {
                                        callback.onSuccess(context);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error fetching expenses from subcollection", e);
                                    // Continue with empty expenses list
                                    context.setRecentExpenses(expenses);
                                    context.setExpensesByCategory(expensesByCategory);
                                    context.setTotalExpenses(totalExpenses[0]);
                                    completedTasks[0]++;
                                    if (completedTasks[0] == totalTasks) {
                                        callback.onSuccess(context);
                                    }
                                });
                    } else {
                        context.setRecentExpenses(expenses);
                        context.setExpensesByCategory(expensesByCategory);
                        context.setTotalExpenses(totalExpenses[0]);
                        
                        Log.d(TAG, "Fetched " + expenses.size() + " expenses from top-level collection");
                        
                        completedTasks[0]++;
                        if (completedTasks[0] == totalTasks) {
                            callback.onSuccess(context);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching expenses from top-level collection", e);
                    // Try subcollection as fallback
                    db.collection("users").document(userId).collection("expenses")
                            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(50)
                            .get()
                            .addOnSuccessListener(subExpenseSnapshots -> {
                                for (DocumentSnapshot doc : subExpenseSnapshots.getDocuments()) {
                                    Expense expense = doc.toObject(Expense.class);
                                    if (expense != null) {
                                        expense.setId(doc.getId());
                                        expenses.add(expense);
                                        totalExpenses[0] += expense.getAmount();
                                        String category = expense.getCategory() != null
                                                ? expense.getCategory() : "Uncategorized";
                                        expensesByCategory.put(category,
                                                expensesByCategory.getOrDefault(category, 0.0)
                                                        + expense.getAmount());
                                    }
                                }
                                
                                context.setRecentExpenses(expenses);
                                context.setExpensesByCategory(expensesByCategory);
                                context.setTotalExpenses(totalExpenses[0]);
                                
                                completedTasks[0]++;
                                if (completedTasks[0] == totalTasks) {
                                    callback.onSuccess(context);
                                }
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Error fetching expenses from subcollection", e2);
                                errorMessage[0] = "Error fetching expenses";
                                completedTasks[0]++;
                                if (completedTasks[0] == totalTasks) {
                                    if (errorMessage[0] != null) {
                                        callback.onError(errorMessage[0]);
                                    } else {
                                        callback.onSuccess(context);
                                    }
                                }
                            });
                });

        // Fetch budgets
        db.collection("users").document(userId).collection("budgets")
                .get()
                .addOnSuccessListener(budgetSnapshots -> {
                    List<Budget> budgets = new ArrayList<>();
                    Map<String, Double> budgetUtilization = new HashMap<>();

                    for (DocumentSnapshot doc : budgetSnapshots.getDocuments()) {
                        Budget budget = doc.toObject(Budget.class);
                        if (budget != null) {
                            budget.setId(doc.getId());
                            budgets.add(budget);
                            if (budget.getTotalAmount() > 0) {
                                double utilization = (budget.getSpentAmount()
                                        / budget.getTotalAmount()) * 100;
                                budgetUtilization.put(budget.getCategory(), utilization);
                            }
                        }
                    }

                    context.setActiveBudgets(budgets);
                    context.setBudgetUtilization(budgetUtilization);

                    completedTasks[0]++;
                    if (completedTasks[0] == totalTasks) {
                        callback.onSuccess(context);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching budgets", e);
                    errorMessage[0] = "Error fetching budgets";
                    completedTasks[0]++;
                    if (completedTasks[0] == totalTasks) {
                        if (errorMessage[0] != null) {
                            callback.onError(errorMessage[0]);
                        } else {
                            callback.onSuccess(context);
                        }
                    }
                });

        // Fetch savings circles
        db.collection("savingsCircles")
                .whereEqualTo("creatorId", userId)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(circleSnapshots -> {
                    List<SavingsCircle> circles = new ArrayList<>();
                    double totalSavingsGoals = 0.0;

                    for (DocumentSnapshot doc : circleSnapshots.getDocuments()) {
                        SavingsCircle circle = doc.toObject(SavingsCircle.class);
                        if (circle != null) {
                            circle.setId(doc.getId());
                            circles.add(circle);
                            totalSavingsGoals += circle.getGoalAmount();
                        }
                    }

                    context.setActiveSavingsCircles(circles);
                    context.setTotalSavingsGoals(totalSavingsGoals);

                    completedTasks[0]++;
                    if (completedTasks[0] == totalTasks) {
                        callback.onSuccess(context);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching savings circles", e);
                    errorMessage[0] = "Error fetching savings circles";
                    completedTasks[0]++;
                    if (completedTasks[0] == totalTasks) {
                        if (errorMessage[0] != null) {
                            callback.onError(errorMessage[0]);
                        } else {
                            callback.onSuccess(context);
                        }
                    }
                });
    }

    /**
     * Merges context from imported conversations (Phase 4).
     *
     * @param conversationIds list of conversation IDs to import
     * @param callback callback to return merged messages
     */
    public void mergeImportedConversationContext(List<String> conversationIds,
                                                   MergedContextCallback callback) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<ChatMessage> mergedMessages = new ArrayList<>();
        final int[] completedTasks = {0};
        final int totalTasks = conversationIds.size();

        for (String conversationId : conversationIds) {
            loadMessages(conversationId, new MessagesCallback() {
                @Override
                public void onSuccess(List<ChatMessage> messages) {
                    if (messages != null) {
                        mergedMessages.addAll(messages);
                    }
                    completedTasks[0]++;
                    if (completedTasks[0] == totalTasks) {
                        // Sort by timestamp
                        Collections.sort(mergedMessages, (m1, m2) -> {
                            if (m1.getTimestamp() == null || m2.getTimestamp() == null) {
                                return 0;
                            }
                            return m1.getTimestamp().compareTo(m2.getTimestamp());
                        });
                        callback.onSuccess(mergedMessages);
                    }
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "Error loading messages from conversation " + conversationId, null);
                    completedTasks[0]++;
                    if (completedTasks[0] == totalTasks) {
                        callback.onSuccess(mergedMessages);
                    }
                }
            });
        }
    }

    /**
     * Builds the Gemini API request body with context messages (overload without financial context).
     *
     * @param message the current user message
     * @param contextMessages previous messages for context
     * @return JSON request body string
     */
    private String buildGeminiRequestBody(String message, List<ChatMessage> contextMessages) {
        return buildGeminiRequestBody(message, contextMessages, null);
    }
    
    /**
     * Builds the Gemini API request body with context messages.
     *
     * @param message the current user message
     * @param contextMessages previous messages for context
     * @param financialContext optional financial context (Phase 5)
     * @return JSON request body string
     */
    private String buildGeminiRequestBody(String message, List<ChatMessage> contextMessages,
                                          FinancialContext financialContext) {
        try {
            JSONObject requestJson = new JSONObject();
            JSONArray contents = new JSONArray();
            
            // Add context messages (convert "assistant" to "model" for Gemini)
            if (contextMessages != null && !contextMessages.isEmpty()) {
                int startIndex = Math.max(0,
                        contextMessages.size() - MAX_CONTEXT_MESSAGES);
                for (int i = startIndex; i < contextMessages.size(); i++) {
                    ChatMessage msg = contextMessages.get(i);
                    if (msg != null && msg.getRole() != null && msg.getContent() != null) {
                        JSONObject content = new JSONObject();
                        JSONArray parts = new JSONArray();
                        JSONObject part = new JSONObject();
                        part.put("text", msg.getContent());
                        parts.put(part);
                        content.put("parts", parts);
                        // Convert "assistant" to "model" for Gemini API
                        String role = "assistant".equalsIgnoreCase(msg.getRole()) 
                                ? "model" : msg.getRole();
                        content.put("role", role);
                        contents.put(content);
                    }
                }
            }
            
            // Build financial context summary (Phase 5)
            StringBuilder financialContextSummary = new StringBuilder();
            if (financialContext != null && financialContext.hasData()) {
                financialContextSummary.append("\n\n=== USER'S FINANCIAL CONTEXT ===\n");
                
                // Spending summary and individual expenses
                List<Expense> recentExpenses = financialContext.getRecentExpenses();
                Log.d(TAG, "Building financial context. Expenses count: " + 
                        (recentExpenses != null ? recentExpenses.size() : 0));
                
                if (recentExpenses != null && !recentExpenses.isEmpty()) {
                    Date monthStart = FinancialDataAggregator.getCurrentMonthStart();
                    String spendingSummary = FinancialDataAggregator.getSpendingSummary(
                            recentExpenses, monthStart, new Date());
                    financialContextSummary.append("Recent Spending: ").append(spendingSummary).append("\n");
                    
                    // Add individual expense details (top 20 most recent)
                    financialContextSummary.append("\nRecent Expenses (most recent first):\n");
                    int expenseCount = Math.min(20, recentExpenses.size());
                    for (int i = 0; i < expenseCount; i++) {
                        Expense expense = recentExpenses.get(i);
                        if (expense != null) {
                            String expenseName = expense.getName() != null ? expense.getName() : "Unnamed";
                            String category = expense.getCategory() != null ? expense.getCategory() : "Uncategorized";
                            double amount = expense.getAmount();
                            String dateStr = expense.getDate() != null 
                                    ? new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                                            .format(expense.getDate())
                                    : "Unknown date";
                            
                            financialContextSummary.append(String.format(
                                    "- %s: $%.2f (%s) on %s\n",
                                    expenseName, amount, category, dateStr));
                        }
                    }
                    if (recentExpenses.size() > 20) {
                        financialContextSummary.append(String.format(
                                "... and %d more expenses\n", recentExpenses.size() - 20));
                    }
                }
                
                // Budget status
                if (financialContext.getActiveBudgets() != null
                        && !financialContext.getActiveBudgets().isEmpty()) {
                    financialContextSummary.append("Active Budgets:\n");
                    for (Budget budget : financialContext.getActiveBudgets()) {
                        double utilization = budget.getTotalAmount() > 0
                                ? (budget.getSpentAmount() / budget.getTotalAmount()) * 100 : 0;
                        financialContextSummary.append(String.format(
                                "- %s: $%.2f / $%.2f (%.1f%% used)\n",
                                budget.getCategory(), budget.getSpentAmount(),
                                budget.getTotalAmount(), utilization));
                    }
                }
                
                // Savings goals
                if (financialContext.getActiveSavingsCircles() != null
                        && !financialContext.getActiveSavingsCircles().isEmpty()) {
                    financialContextSummary.append("Active Savings Goals:\n");
                    for (SavingsCircle circle : financialContext.getActiveSavingsCircles()) {
                        financialContextSummary.append(String.format(
                                "- %s: $%.2f goal\n",
                                circle.getChallengeTitle(), circle.getGoalAmount()));
                    }
                }
                
                financialContextSummary.append("Use this context to provide personalized advice.\n");
            }
            
            // Add current user message with system instruction in the prompt
            JSONObject userContent = new JSONObject();
            JSONArray userParts = new JSONArray();
            JSONObject userPart = new JSONObject();
            
            // Build the user message
            String fullMessage = message;
            
            // Add financial context summary if available
            if (financialContextSummary.length() > 0) {
                // If we have context messages, add financial context as a separate system-like message
                // Otherwise, include it in the first message
                if (contents.length() == 0) {
                    // No previous context - include everything in first message
                    fullMessage = "You are a helpful financial assistant. "
                            + "Provide budgeting tips and financial advice. "
                            + "Be concise and helpful."
                            + financialContextSummary.toString()
                            + "\n\nUser: " + message;
                } else {
                    // We have context messages - add financial context as a separate user message
                    // before the current message to provide context without breaking conversation flow
                    JSONObject contextContent = new JSONObject();
                    JSONArray contextParts = new JSONArray();
                    JSONObject contextPart = new JSONObject();
                    contextPart.put("text", "Context: " + financialContextSummary.toString());
                    contextParts.put(contextPart);
                    contextContent.put("parts", contextParts);
                    contextContent.put("role", "user");
                    contents.put(contextContent);
                    
                    // Now add the actual user message
                    fullMessage = message;
                }
            } else if (contents.length() == 0) {
                // No financial context and no previous messages - add system instruction
                fullMessage = "You are a helpful financial assistant. "
                        + "Provide budgeting tips and financial advice. "
                        + "Be concise and helpful.\n\nUser: " + message;
            }
            
            userPart.put("text", fullMessage);
            userParts.put(userPart);
            userContent.put("parts", userParts);
            userContent.put("role", "user");
            contents.put(userContent);
            
            requestJson.put("contents", contents);
            
            // Add generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1024);
            requestJson.put("generationConfig", generationConfig);
            
            return requestJson.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error building Gemini request body", e);
            // Fallback to absolute simplest format
            try {
                JSONObject fallback = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", message);
                parts.put(part);
                content.put("parts", parts);
                content.put("role", "user");
                contents.put(content);
                fallback.put("contents", contents);
                return fallback.toString();
            } catch (JSONException e2) {
                Log.e(TAG, "Error building fallback request", e2);
                return "{\"contents\":[{\"parts\":[{\"text\":\"" + message + "\"}],\"role\":\"user\"}]}";
            }
        }
    }

    /**
     * Builds a simple Gemini API request body for a single prompt (used for title/summary generation).
     *
     * @param prompt the prompt text
     * @return JSON request body string
     */
    private String buildGeminiRequestBodyForPrompt(String prompt) {
        try {
            JSONObject requestJson = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            content.put("role", "user");
            contents.put(content);
            requestJson.put("contents", contents);
            
            // Add generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 256);
            requestJson.put("generationConfig", generationConfig);
            
            return requestJson.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error building Gemini request body for prompt", e);
            return "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt + "\"}],\"role\":\"user\"}]}";
        }
    }

    /**
     * Tries alternative Gemini models when the primary model fails with 404.
     */
    private void tryAlternativeGeminiModel(String message, String conversationId,
                                           List<ChatMessage> contextMessages,
                                           FinancialContext financialContext,
                                           MessageCallback callback) {
        // Try different model names and API versions (prioritize newer models)
        String[] alternativeConfigs = {
            "v1beta/models/gemini-1.5-pro",    // Try v1beta with pro (more capable)
            "v1/models/gemini-1.5-flash",      // Try v1 with flash
            "v1/models/gemini-1.5-pro",        // Try v1 with pro
            "v1beta/models/gemini-pro",        // Try v1beta with original gemini-pro
            "v1/models/gemini-pro"             // Try v1 with original gemini-pro (fallback)
        };

        tryAlternativeGeminiRecursive(alternativeConfigs, 0, message, contextMessages,
                financialContext, callback);
    }

    /**
     * Recursively tries alternative Gemini model configurations.
     */
    private void tryAlternativeGeminiRecursive(String[] configs, int index,
                                                String message,
                                                List<ChatMessage> contextMessages,
                                                FinancialContext financialContext,
                                                MessageCallback callback) {
        if (index >= configs.length) {
            // All models failed
            callback.onError("Gemini API models are not available. "
                    + "Please check your API key permissions or try again later.");
            return;
        }

        String config = configs[index];
        String[] parts = config.split("/");
        String apiVersion = parts[0];
        String modelName = parts[2];
        
        try {
            String requestBody = buildGeminiRequestBody(message, contextMessages, financialContext);
            String url = "https://generativelanguage.googleapis.com/" + apiVersion 
                    + "/models/" + modelName + ":generateContent?key=" + apiKey;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.w(TAG, "Alternative config " + config + " failed: " + e.getMessage());
                    tryAlternativeGeminiRecursive(configs, index + 1, message, contextMessages,
                            financialContext, callback);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            String aiResponse = parseGeminiResponse(responseBody);
                            if (aiResponse != null && !aiResponse.isEmpty()) {
                                Log.i(TAG, "Successfully used alternative config: " + config);
                                callback.onSuccess(aiResponse);
                                return;
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing response from " + config, e);
                        }
                    }
                    // Try next config
                    tryAlternativeGeminiRecursive(configs, index + 1, message, contextMessages,
                            financialContext, callback);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Error trying alternative config " + config, e);
            tryAlternativeGeminiRecursive(configs, index + 1, message, contextMessages,
                    financialContext, callback);
        }
    }

    /**
     * Parses the Gemini API response to extract the generated text.
     *
     * @param responseBody the raw response body
     * @return the extracted text
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            if (responseBody == null || responseBody.trim().isEmpty()) {
                return "I received an empty response. Please try again.";
            }

            JsonObject json = JsonParser.parseString(responseBody)
                    .getAsJsonObject();
            
            // Check for errors first
            if (json.has("error")) {
                JsonObject error = json.getAsJsonObject("error");
                String errorMsg = error.has("message") 
                        ? error.get("message").getAsString() 
                        : "Unknown error";
                Log.e(TAG, "Gemini API error: " + errorMsg);
                throw new Exception("API error: " + errorMsg);
            }
            
            // Parse candidates array
            if (json.has("candidates")) {
                com.google.gson.JsonArray candidates = json.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject content = candidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            com.google.gson.JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonObject part = parts.get(0).getAsJsonObject();
                                if (part.has("text")) {
                                    String text = part.get("text").getAsString();
                                    if (!text.isEmpty()) {
                                        return text.trim();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // If we can't parse, log the full response for debugging
            Log.w(TAG, "Unexpected response format. Response length: " + 
                    (responseBody != null ? responseBody.length() : 0));
            Log.w(TAG, "Response preview: " + 
                    (responseBody != null && responseBody.length() > 500 
                            ? responseBody.substring(0, 500) + "..." 
                            : responseBody));
            
            // Try to extract any text from the response even if structure is unexpected
            if (responseBody != null && responseBody.contains("\"text\"")) {
                try {
                    int textStart = responseBody.indexOf("\"text\"");
                    int textValueStart = responseBody.indexOf("\"", textStart + 6) + 1;
                    int textValueEnd = responseBody.indexOf("\"", textValueStart);
                    if (textValueEnd > textValueStart) {
                        String extractedText = responseBody.substring(textValueStart, textValueEnd);
                        if (!extractedText.isEmpty()) {
                            return extractedText;
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to extract text from response", e);
                }
            }
            
            // Last resort: return error message instead of generic hardcoded response
            return "I received an unexpected response format. Please try rephrasing your question.";
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Gemini response: " + responseBody, e);
            return "I encountered an error. Please try rephrasing your question.";
        }
    }


    /**
     * Generates a conversation title using AI API.
     *
     * @param messages the conversation messages
     * @param callback callback to handle the response or error
     */
    public void generateConversationTitle(List<ChatMessage> messages,
                                          MessageCallback callback) {
        if (messages == null || messages.isEmpty()) {
            callback.onSuccess("New Conversation");
            return;
        }

        if (apiKey == null || apiKey.isEmpty()) {
            callback.onSuccess("New Conversation");
            return;
        }

        // Build conversation context from all messages
        StringBuilder conversationBuilder = new StringBuilder();
        StringBuilder userQuestionsBuilder = new StringBuilder();
        
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if (msg != null && msg.getContent() != null) {
                if (i > 0) {
                    conversationBuilder.append("\n");
                }
                String role = "user".equalsIgnoreCase(msg.getRole()) ? "User" : "Assistant";
                conversationBuilder.append(role).append(": ").append(msg.getContent());
                
                // Extract user questions (skip greetings like "hello", "hi")
                if ("user".equalsIgnoreCase(msg.getRole())) {
                    String content = msg.getContent().toLowerCase().trim();
                    if (!content.equals("hello") && !content.equals("hi") 
                            && !content.equals("hey") && !content.startsWith("hello ")
                            && !content.startsWith("hi ") && !content.startsWith("hey ")) {
                        if (userQuestionsBuilder.length() > 0) {
                            userQuestionsBuilder.append(" ");
                        }
                        userQuestionsBuilder.append(msg.getContent());
                    }
                }
            }
        }
        String conversationText = conversationBuilder.toString();
        String userQuestions = userQuestionsBuilder.toString();

        // Prioritize user questions in the prompt
        String prompt;
        if (!userQuestions.isEmpty()) {
            prompt = "Based on this conversation, generate a short, descriptive title (6-7 words max) "
                    + "that captures the MAIN TOPIC or QUESTION the user is asking about. "
                    + "Focus on the user's questions, not greetings. "
                    + "Return ONLY the title, nothing else. No quotes, no explanations.\n\n"
                    + "User's questions: " + userQuestions + "\n\n"
                    + "Full conversation:\n" + conversationText;
        } else {
            prompt = "Based on this conversation, generate a short, descriptive title (6-7 words max) "
                    + "that captures the main topic. "
                    + "Return ONLY the title, nothing else. No quotes, no explanations.\n\n"
                    + "Conversation:\n" + conversationText;
        }

        try {
            String requestBody = buildGeminiRequestBodyForPrompt(prompt);
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/" + DEFAULT_MODEL + ":generateContent?key=" + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Title generation failed", e);
                    callback.onSuccess("New Conversation");
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onSuccess("New Conversation");
                        return;
                    }

                    try {
                        String responseBody = response.body() != null
                                ? response.body().string() : "";
                        String title = parseGeminiResponse(responseBody);
                        
                        // Clean up title - remove quotes, extra whitespace, and limit length
                        title = title.replaceAll("\"", "").trim();
                        title = title.replaceAll("^Title:?\\s*", ""); // Remove "Title:" prefix if present
                        title = title.replaceAll("\\s+", " "); // Normalize whitespace
                        
                        // Limit to 7 words max
                        String[] words = title.split("\\s+");
                        if (words.length > 7) {
                            StringBuilder limitedTitle = new StringBuilder();
                            for (int i = 0; i < 7; i++) {
                                if (i > 0) {
                                    limitedTitle.append(" ");
                                }
                                limitedTitle.append(words[i]);
                            }
                            title = limitedTitle.toString();
                        }
                        
                        // Final validation
                        if (title.isEmpty() || title.toLowerCase().contains("i'm here to help")
                                || title.toLowerCase().contains("i am here to help")) {
                            // Fallback: use first user message as title
                            if (messages != null && !messages.isEmpty()) {
                                for (ChatMessage msg : messages) {
                                    if (msg != null && "user".equalsIgnoreCase(msg.getRole())
                                            && msg.getContent() != null && !msg.getContent().isEmpty()) {
                                        String userMsg = msg.getContent();
                                        if (userMsg.length() > 40) {
                                            userMsg = userMsg.substring(0, 37) + "...";
                                        }
                                        title = userMsg;
                                        break;
                                    }
                                }
                            }
                            if (title.isEmpty()) {
                                title = "New Conversation";
                            }
                        }
                        
                        callback.onSuccess(title);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing title", e);
                        // Fallback to first user message
                        String fallbackTitle = "New Conversation";
                        if (messages != null && !messages.isEmpty()) {
                            for (ChatMessage msg : messages) {
                                if (msg != null && "user".equalsIgnoreCase(msg.getRole())
                                        && msg.getContent() != null && !msg.getContent().isEmpty()) {
                                    String userMsg = msg.getContent();
                                    if (userMsg.length() > 40) {
                                        userMsg = userMsg.substring(0, 37) + "...";
                                    }
                                    fallbackTitle = userMsg;
                                    break;
                                }
                            }
                        }
                        callback.onSuccess(fallbackTitle);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error generating title", e);
            callback.onSuccess("New Conversation");
        }
    }

    /**
     * Generates a conversation summary using AI API.
     *
     * @param messages the conversation messages
     * @param callback callback to handle the response or error
     */
    public void generateConversationSummary(List<ChatMessage> messages,
                                            MessageCallback callback) {
        if (messages == null || messages.isEmpty()) {
            callback.onSuccess("");
            return;
        }

        if (apiKey == null || apiKey.isEmpty()) {
            callback.onSuccess("");
            return;
        }

        StringBuilder conversationBuilder = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) {
                conversationBuilder.append("\n");
            }
            ChatMessage msg = messages.get(i);
            conversationBuilder.append(msg.getRole())
                    .append(": ")
                    .append(msg.getContent());
        }
        String conversationText = conversationBuilder.toString();

        String prompt = "Summarize this conversation in 2-3 sentences: "
                + conversationText;

        try {
            String requestBody = buildGeminiRequestBodyForPrompt(prompt);
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/" + DEFAULT_MODEL + ":generateContent?key=" + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Summary generation failed", e);
                    callback.onSuccess("");
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onSuccess("");
                        return;
                    }

                    try {
                        String responseBody = response.body() != null
                                ? response.body().string() : "";
                        String summary = parseGeminiResponse(responseBody);
                        callback.onSuccess(summary.trim());
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing summary", e);
                        callback.onSuccess("");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error generating summary", e);
            callback.onSuccess("");
        }
    }

    /**
     * Saves a conversation to Firestore.
     *
     * @param conversation the conversation to save
     * @param callback callback to handle success or error
     */
    public void saveConversation(ChatConversation conversation,
                                 SaveCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        if (conversation.getId() == null) {
            conversation.setId(UUID.randomUUID().toString());
        }
        conversation.updateTimestamp();

        db.collection("users").document(userId)
                .collection("conversations")
                .document(conversation.getId())
                .set(conversation)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Conversation saved: " + conversation.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving conversation", e);
                    callback.onError("Error saving conversation: "
                            + e.getMessage());
                });
    }

    /**
     * Loads all conversations for the current user.
     *
     * @param callback callback to handle the result
     */
    public void loadConversations(ConversationsCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection("users").document(userId)
                .collection("conversations")
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ChatConversation> conversations = new ArrayList<>();
                    for (DocumentSnapshot document
                            : queryDocumentSnapshots.getDocuments()) {
                        ChatConversation conversation = document
                                .toObject(ChatConversation.class);
                        if (conversation != null) {
                            conversation.setId(document.getId());
                            conversations.add(conversation);
                        }
                    }
                    callback.onSuccess(conversations);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading conversations", e);
                    callback.onError("Error loading conversations: "
                            + e.getMessage());
                });
    }

    /**
     * Loads messages for a specific conversation.
     *
     * @param conversationId the conversation ID
     * @param callback callback to handle the result
     */
    public void loadMessages(String conversationId,
                             MessagesCallback callback) {
        if (conversationId == null || conversationId.isEmpty()) {
            callback.onError("Invalid conversation ID");
            return;
        }

        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        db.collection("users").document(userId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ChatMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot document
                            : queryDocumentSnapshots.getDocuments()) {
                        ChatMessage message = document.toObject(ChatMessage.class);
                        if (message != null) {
                            message.setId(document.getId());
                            messages.add(message);
                        }
                    }
                    callback.onSuccess(messages);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading messages", e);
                    callback.onError("Error loading messages: "
                            + e.getMessage());
                });
    }

    /**
     * Saves a message to Firestore.
     *
     * @param message the message to save
     * @param callback callback to handle success or error
     */
    public void saveMessage(ChatMessage message, SaveCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        if (message.getConversationId() == null
                || message.getConversationId().isEmpty()) {
            callback.onError("Invalid conversation ID");
            return;
        }

        db.collection("users").document(userId)
                .collection("conversations")
                .document(message.getConversationId())
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    message.setId(documentReference.getId());
                    Log.d(TAG, "Message saved: " + message.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving message", e);
                    callback.onError("Error saving message: "
                            + e.getMessage());
                });
    }

    /**
     * Gets the current user ID.
     *
     * @return the user ID or null if not authenticated
     */
    private String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Callback interface for message responses.
     */
    public interface MessageCallback {
        /**
         * Called when the API call succeeds.
         *
         * @param response the AI response
         */
        void onSuccess(String response);

        /**
         * Called when the API call fails.
         *
         * @param error the error message
         */
        void onError(String error);
    }

    /**
     * Callback interface for save operations.
     */
    public interface SaveCallback {
        /**
         * Called when the save operation succeeds.
         */
        void onSuccess();

        /**
         * Called when the save operation fails.
         *
         * @param error the error message
         */
        void onError(String error);
    }

    /**
     * Callback interface for loading conversations.
     */
    public interface ConversationsCallback {
        /**
         * Called when conversations are loaded successfully.
         *
         * @param conversations the list of conversations
         */
        void onSuccess(List<ChatConversation> conversations);

        /**
         * Called when loading fails.
         *
         * @param error the error message
         */
        void onError(String error);
    }

    /**
     * Callback interface for loading messages.
     */
    public interface MessagesCallback {
        /**
         * Called when messages are loaded successfully.
         *
         * @param messages the list of messages
         */
        void onSuccess(List<ChatMessage> messages);

        /**
         * Called when loading fails.
         *
         * @param error the error message
         */
        void onError(String error);
    }

    /**
     * Callback interface for financial context fetching (Phase 5).
     */
    public interface FinancialContextCallback {
        /**
         * Called when financial context is fetched successfully.
         *
         * @param context the financial context
         */
        void onSuccess(FinancialContext context);

        /**
         * Called when fetching fails.
         *
         * @param error the error message
         */
        void onError(String error);
    }

    /**
     * Callback interface for merged context (Phase 4).
     */
    public interface MergedContextCallback {
        /**
         * Called when context is merged successfully.
         *
         * @param mergedMessages the merged messages
         */
        void onSuccess(List<ChatMessage> mergedMessages);
    }
}

