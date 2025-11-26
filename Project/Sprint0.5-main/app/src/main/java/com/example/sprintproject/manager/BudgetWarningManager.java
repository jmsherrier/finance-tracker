package com.example.sprintproject.manager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sprintproject.model.BudgetWarning;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Singleton class that manages budget warning notifications.
 * Maintains a queue to ensure only one pop-up shows at a time
 * and tracks already-shown warnings to prevent repeated alerts.
 */
public class BudgetWarningManager {
    private static final String PREFS_NAME = "budget_warnings";
    private static final String KEY_SHOWN_WARNINGS = "shown_warnings_";

    private static BudgetWarningManager instance;
    private final Queue<BudgetWarning> warningQueue;
    private final MutableLiveData<BudgetWarning> currentWarning;
    private Context applicationContext;

    /**
     * Private constructor for singleton pattern.
     */
    private BudgetWarningManager() {
        this.warningQueue = new LinkedList<>();
        this.currentWarning = new MutableLiveData<>();
    }

    /**
     * Gets the singleton instance of BudgetWarningManager.
     *
     * @return the singleton instance
     */
    public static synchronized BudgetWarningManager getInstance() {
        if (instance == null) {
            instance = new BudgetWarningManager();
        }
        return instance;
    }

    /**
     * Initializes the manager with application context.
     *
     * @param context the application context
     */
    public void initialize(Context context) {
        if (context != null) {
            this.applicationContext = context.getApplicationContext();
        }
    }

    /**
     * Adds a warning to the queue if it hasn't been shown before.
     *
     * @param warning the budget warning to add
     */
    public void addWarning(BudgetWarning warning) {
        if (warning == null || applicationContext == null) {
            return;
        }

        String warningKey = generateWarningKey(warning);
        if (!hasWarningBeenShown(warningKey)) {
            synchronized (warningQueue) {
                warningQueue.offer(warning);
                if (currentWarning.getValue() == null) {
                    showNextWarning();
                }
            }
        }
    }

    /**
     * Gets the LiveData for observing current warning.
     *
     * @return LiveData containing the current warning
     */
    public LiveData<BudgetWarning> getCurrentWarning() {
        return currentWarning;
    }

    /**
     * Dismisses the current warning and shows the next one in queue.
     */
    public void dismissCurrentWarning() {
        BudgetWarning warning = currentWarning.getValue();
        if (warning != null) {
            String warningKey = generateWarningKey(warning);
            markWarningAsShown(warningKey);
        }
        currentWarning.setValue(null);
        showNextWarning();
    }

    /**
     * Shows the next warning from the queue.
     */
    private void showNextWarning() {
        synchronized (warningQueue) {
            BudgetWarning nextWarning = warningQueue.poll();
            if (nextWarning != null) {
                currentWarning.postValue(nextWarning);
            }
        }
    }

    /**
     * Generates a unique key for a warning based on budget ID and percentage.
     *
     * @param warning the budget warning
     * @return unique key string
     */
    private String generateWarningKey(BudgetWarning warning) {
        int percentageThreshold = (int) (warning.getPercentage() / 10) * 10;
        return warning.getBudgetId() + "_" + percentageThreshold;
    }

    /**
     * Checks if a warning has already been shown.
     *
     * @param warningKey the warning key
     * @return true if warning was already shown
     */
    private boolean hasWarningBeenShown(String warningKey) {
        if (applicationContext == null) {
            return false;
        }
        SharedPreferences prefs = applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> shownWarnings = prefs
                .getStringSet(KEY_SHOWN_WARNINGS, new HashSet<>());
        return shownWarnings.contains(warningKey);
    }

    /**
     * Marks a warning as shown in SharedPreferences.
     *
     * @param warningKey the warning key
     */
    private void markWarningAsShown(String warningKey) {
        if (applicationContext == null) {
            return;
        }
        SharedPreferences prefs = applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> shownWarnings = new HashSet<>(
                prefs.getStringSet(KEY_SHOWN_WARNINGS, new HashSet<>()));
        shownWarnings.add(warningKey);
        prefs.edit().putStringSet(KEY_SHOWN_WARNINGS, shownWarnings).apply();
    }

    /**
     * Clears all shown warnings for a specific budget period.
     * Call this when a new budget period starts.
     *
     * @param budgetId the budget ID
     */
    public void clearWarningsForBudget(String budgetId) {
        if (applicationContext == null || budgetId == null) {
            return;
        }
        SharedPreferences prefs = applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> shownWarnings = new HashSet<>(
                prefs.getStringSet(KEY_SHOWN_WARNINGS, new HashSet<>()));
        
        Set<String> updatedWarnings = new HashSet<>();
        for (String key : shownWarnings) {
            if (!key.startsWith(budgetId + "_")) {
                updatedWarnings.add(key);
            }
        }
        prefs.edit().putStringSet(KEY_SHOWN_WARNINGS, updatedWarnings).apply();
    }

    /**
     * Clears all warning history. Useful for testing or user preference reset.
     */
    public void clearAllWarnings() {
        if (applicationContext == null) {
            return;
        }
        SharedPreferences prefs = applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        synchronized (warningQueue) {
            warningQueue.clear();
        }
        currentWarning.setValue(null);
    }
}
