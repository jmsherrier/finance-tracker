package com.example.sprintproject.model;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardModel {

    private static final String FIELD_USER_ID = "userId";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private ListenerRegistration expensesListener;
    private ListenerRegistration budgetsListener;

    private final MutableLiveData<Map<String, Object>> combinedLive =
            new MutableLiveData<>();

    private List<Expense> latestExpenses = new ArrayList<>();
    private List<Budget> latestBudgets = new ArrayList<>();
    private Date selectedDate = new Date();

    // Default constructor - initialization handled in getDashboardData()
    public DashboardModel() {
    }

    public LiveData<Map<String, Object>> getDashboardData(Date selectedDate) {
        this.selectedDate = (selectedDate == null) ? new Date() : selectedDate;
        startListening();
        recomputeAndPost();
        return combinedLive;
    }

    private String uid() {
        FirebaseUser u = auth.getCurrentUser();
        return (u == null) ? null : u.getUid();
    }

    private void startListening() {
        String uid = uid();
        if (uid == null) {
            combinedLive.postValue(new HashMap<>());
            return;
        }

        startListeningToExpenses(uid);
        startListeningToBudgets(uid);
    }

    private void startListeningToExpenses(String uid) {
        if (expensesListener == null) {
            expensesListener = db.collection("expenses")
                    .whereEqualTo(FIELD_USER_ID, uid)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            latestExpenses = parseExpenses(snapshots, uid);
                            recomputeAndPost();
                        }
                    });
        }
    }

    private List<Expense> parseExpenses(@Nullable QuerySnapshot snapshots, String uid) {
        List<Expense> list = new ArrayList<>();
        if (snapshots != null) {
            for (QueryDocumentSnapshot ds : snapshots) {
                Expense ex = ds.toObject(Expense.class);
                if (ex != null) {
                    ex.setId(ds.getId());
                    if (ex.getUserId() == null) {
                        ex.setUserId(uid);
                    }
                    list.add(ex);
                }
            }
        }
        return list;
    }

    private void startListeningToBudgets(String uid) {
        if (budgetsListener == null) {
            final CollectionReference budRef = db.collection("users")
                    .document(uid)
                    .collection("budgets");

            budgetsListener = budRef.addSnapshotListener(
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            latestBudgets = parseBudgets(snapshots, uid);
                            if (latestBudgets.isEmpty()) {
                                seedBudgetsIfNeeded(budRef, uid);
                            }
                            recomputeAndPost();
                        }
                    });
        }
    }

    private List<Budget> parseBudgets(@Nullable QuerySnapshot snapshots, String uid) {
        List<Budget> list = new ArrayList<>();
        if (snapshots != null) {
            for (QueryDocumentSnapshot ds : snapshots) {
                Budget b = ds.toObject(Budget.class);
                if (b != null) {
                    b.setId(ds.getId());
                    if (b.getUserId() == null) {
                        b.setUserId(uid);
                    }
                    list.add(b);
                }
            }
        }
        return list;
    }

    public void stopListening() {
        if (expensesListener != null) {
            expensesListener.remove();
            expensesListener = null;
        }
        if (budgetsListener != null) {
            budgetsListener.remove();
            budgetsListener = null;
        }
    }

    private void recomputeAndPost() {
        Date[] monthRange = calculateMonthRange();
        Date start = monthRange[0];
        Date end = monthRange[1];

        double totalSpent = calculateTotalSpent(start, end);
        Map<String, Double> categories = calculateCategories(start, end);
        double totalBudget = calculateTotalBudget(start, end);

        Map<String, Object> combined = new HashMap<>();
        combined.put("totalSpent", totalSpent);
        combined.put("totalBudget", totalBudget);
        combined.put("categories", categories);
        combined.put("budgets",
                latestBudgets == null
                        ? new ArrayList<Budget>()
                        : latestBudgets);

        combinedLive.postValue(combined);
    }

    private Date[] calculateMonthRange() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        Date end = cal.getTime();
        return new Date[]{start, end};
    }

    private double calculateTotalSpent(Date start, Date end) {
        double totalSpent = 0.0;
        if (latestExpenses != null) {
            for (Expense e : latestExpenses) {
                if (e != null && isExpenseInRange(e, start, end)) {
                    totalSpent += e.getAmount();
                }
            }
        }
        return totalSpent;
    }

    private boolean isExpenseInRange(Expense e, Date start, Date end) {
        Date expenseDate = e.getDate();
        return expenseDate != null
                && !expenseDate.before(start)
                && expenseDate.before(end);
    }

    private Map<String, Double> calculateCategories(Date start, Date end) {
        Map<String, Double> categories = new HashMap<>();
        if (latestExpenses != null) {
            for (Expense e : latestExpenses) {
                if (e != null && isExpenseInRange(e, start, end)) {
                    String cat = (e.getCategory() == null)
                            ? "Uncategorized"
                            : e.getCategory();
                    categories.put(cat,
                            categories.getOrDefault(cat, 0.0) + e.getAmount());
                }
            }
        }
        return categories;
    }

    private double calculateTotalBudget(Date start, Date end) {
        double totalBudget = 0.0;
        if (latestBudgets != null) {
            for (Budget b : latestBudgets) {
                if (b != null && isBudgetInRange(b, start, end)) {
                    totalBudget += b.getTotalAmount();
                }
            }
        }
        return totalBudget;
    }

    private boolean isBudgetInRange(Budget b, Date start, Date end) {
        Date bStart = b.getStartDate();
        return bStart == null
                || (!bStart.before(start) && bStart.before(end));
    }

    private void seedBudgetsIfNeeded(CollectionReference budRef, String uid) {
        // Simple demo budgets only if user has none
        Map<String, Object> b1 = new HashMap<>();
        b1.put("title", "Monthly Essentials");
        b1.put("totalAmount", 500.0);
        b1.put("category", "General");
        b1.put("frequency", "monthly");
        b1.put("startDate", new Date());
        b1.put(FIELD_USER_ID, uid);
        b1.put("createdAt", new Date());
        b1.put("spentAmount", 0.0);

        Map<String, Object> b2 = new HashMap<>();
        b2.put("title", "Fun Money");
        b2.put("totalAmount", 150.0);
        b2.put("category", "Entertainment");
        b2.put("frequency", "monthly");
        b2.put("startDate", new Date());
        b2.put(FIELD_USER_ID, uid);
        b2.put("createdAt", new Date());
        b2.put("spentAmount", 0.0);

        budRef.add(b1);
        budRef.add(b2);
    }
}
}