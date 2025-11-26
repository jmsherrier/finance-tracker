package com.example.sprintproject.manager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sprintproject.model.ExpenseReminder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class that manages missed expense log reminders.
 * Checks last expense date and triggers reminders when needed.
 */
public class ExpenseReminderManager {
    private static final String PREFS_NAME = "expense_reminders";
    private static final String KEY_LAST_REMINDER_CHECK = "last_reminder_check";
    private static final String COLLECTION_EXPENSES = "expenses";

    private static ExpenseReminderManager instance;
    private final MutableLiveData<ExpenseReminder> currentReminder;
    private Context applicationContext;
    private FirebaseFirestore db;

    /**
     * Private constructor for singleton pattern.
     */
    private ExpenseReminderManager() {
        this.currentReminder = new MutableLiveData<>();
    }

    /**
     * Gets the singleton instance of ExpenseReminderManager.
     *
     * @return the singleton instance
     */
    public static synchronized ExpenseReminderManager getInstance() {
        if (instance == null) {
            instance = new ExpenseReminderManager();
        }
        return instance;
    }

    /**
     * Initializes the manager with application context and Firestore.
     *
     * @param context the application context
     * @param firestore the Firestore instance
     */
    public void initialize(Context context, FirebaseFirestore firestore) {
        if (context != null) {
            this.applicationContext = context.getApplicationContext();
        }
        this.db = firestore;
    }

    /**
     * Checks if user has missed expense logs and creates reminder.
     *
     * @param userId the user ID to check
     */
    public void checkMissedExpenses(String userId) {
        if (db == null || userId == null || applicationContext == null) {
            return;
        }

        // Query last expense for this user
        db.collection(COLLECTION_EXPENSES)
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No expenses logged yet - no reminder needed
                        return;
                    }

                    // Get the last expense date
                    QueryDocumentSnapshot lastExpenseDoc =
                            (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    Date lastExpenseDate = lastExpenseDoc.getDate("date");

                    if (lastExpenseDate != null) {
                        int daysSince = calculateDaysSince(lastExpenseDate);
                        
                        if (daysSince > 0 && !wasReminderShownToday()) {
                            ExpenseReminder reminder = new ExpenseReminder(
                                    daysSince,
                                    lastExpenseDate.getTime(),
                                    userId
                            );
                            currentReminder.postValue(reminder);
                            markReminderShown();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Silent fail - reminder is not critical
                });
    }

    /**
     * Gets the LiveData for observing current reminder.
     *
     * @return LiveData containing the current reminder
     */
    public LiveData<ExpenseReminder> getCurrentReminder() {
        return currentReminder;
    }

    /**
     * Dismisses the current reminder.
     */
    public void dismissCurrentReminder() {
        currentReminder.setValue(null);
    }

    /**
     * Calculates days since a given date.
     *
     * @param pastDate the past date
     * @return number of days since that date
     */
    private int calculateDaysSince(Date pastDate) {
        Date now = new Date();
        long diffMillis = now.getTime() - pastDate.getTime();
        return (int) TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    /**
     * Checks if reminder was already shown today.
     *
     * @return true if reminder was shown today
     */
    private boolean wasReminderShownToday() {
        if (applicationContext == null) {
            return false;
        }

        SharedPreferences prefs = applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCheck = prefs.getLong(KEY_LAST_REMINDER_CHECK, 0);

        Calendar lastCheckCal = Calendar.getInstance();
        lastCheckCal.setTimeInMillis(lastCheck);

        Calendar today = Calendar.getInstance();

        return lastCheckCal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && lastCheckCal.get(Calendar.DAY_OF_YEAR)
                == today.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Marks that reminder was shown today.
     */
    private void markReminderShown() {
        if (applicationContext == null) {
            return;
        }

        SharedPreferences prefs = applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_LAST_REMINDER_CHECK, System.currentTimeMillis())
                .apply();
    }

    /**
     * Clears reminder history. Useful for testing or logout.
     */
    public void clearReminderHistory() {
        if (applicationContext == null) {
            return;
        }

        SharedPreferences prefs = applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        currentReminder.setValue(null);
    }
}
