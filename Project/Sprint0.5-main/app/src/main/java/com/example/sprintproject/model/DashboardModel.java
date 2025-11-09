package com.example.sprintproject.model;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.List;

public class DashboardModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private ListenerRegistration expensesListener;
    private ListenerRegistration budgetsListener;
    private final MutableLiveData<Map<String, Object>> combinedLive = new MutableLiveData<>();
    private List<Expense> latestExpenses = new ArrayList<>();
    private List<Budget> latestBudgets = new ArrayList<>();
    private Date selectedDate = new Date();

    public DashboardModel() {

    }

    public LiveData<Map<String, Object>> getDashboardData(Date selectedDate) {
        this.selectedDate = selectedDate == null ? new Date() : selectedDate;
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
        final CollectionReference expRef = db.collection("users").document(uid).collection("expenses");
        final CollectionReference budRef = db.collection("users").document(uid).collection("budgets");
        if (expensesListener != null) {

        } else {
            expensesListener = expRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }
                    List<Expense> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot ds : snapshots) {
                            Expense ex = ds.toObject(Expense.class);
                            if (ex != null) {
                                ex.setId(ds.getId());
                                if (ex.getUserId() == null) ex.setUserId(uid);
                                list.add(ex);
                            }
                        }
                    }
                    latestExpenses = list;
                    if (latestExpenses.isEmpty()) seedExpensesIfNeeded(expRef, uid);
                    recomputeAndPost();
                }
            });
        }
        if (budgetsListener != null) {

        } else {
            budgetsListener = budRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }
                    List<Budget> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot ds : snapshots) {
                            Budget b = ds.toObject(Budget.class);
                            if (b != null) {
                                b.setId(ds.getId());
                                if (b.getUserId() == null) b.setUserId(uid);
                                list.add(b);
                            }
                        }
                    }
                    latestBudgets = list;
                    if (latestBudgets.isEmpty()) seedBudgetsIfNeeded(budRef, uid);
                    recomputeAndPost();
                }
            });
        }
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
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date end = cal.getTime();
        double totalSpent = 0.0;
        Map<String, Double> categories = new HashMap<>();
        if (latestExpenses != null) {
            for (Expense e : latestExpenses) {
                if (e == null) continue;
                Date expenseDate = e.getDate();
                if (expenseDate.compareTo(start) >= 0 && expenseDate.compareTo(end) < 0) {
                    double amt = e.getAmount();
                    totalSpent += amt;
                    String cat = e.getCategory() == null ? "Uncategorized" : e.getCategory();
                    categories.put(cat, categories.getOrDefault(cat, 0.0) + amt);
                }
            }
        }
        double totalBudget = 0.0;
        if (latestBudgets != null) {
            for (Budget b : latestBudgets) {
                if (b == null) continue;
                Date bStart = b.getStartDate();
                if (bStart == null) {
                    totalBudget += b.getTotalAmount();
                } else {
                    if (bStart.compareTo(start) >= 0 && bStart.compareTo(end) < 0) {
                        totalBudget += b.getTotalAmount();
                    } else {
                        totalBudget += b.getTotalAmount();
                    }
                }
            }
        }
        Map<String, Object> combined = new HashMap<>();
        combined.put("totalSpent", totalSpent);
        combined.put("totalBudget", totalBudget);
        combined.put("categories", categories);
        combined.put("budgets", latestBudgets == null ? new ArrayList<Budget>() : latestBudgets);
        combinedLive.postValue(combined);
    }

    private void seedExpensesIfNeeded(CollectionReference expRef, String uid) {
        Map<String, Object> e1 = new HashMap<>();
        e1.put("name", "Lunch");
        e1.put("amount", 12.50);
        e1.put("category", "Food");
        e1.put("date", new Date());
        e1.put("notes", "Seeded lunch");
        e1.put("userId", uid);
        e1.put("createdAt", new Date());

        Map<String, Object> e2 = new HashMap<>();
        e2.put("name", "Bus");
        e2.put("amount", 2.75);
        e2.put("category", "Transport");
        e2.put("date", new Date());
        e2.put("notes", "Seeded transport");
        e2.put("userId", uid);
        e2.put("createdAt", new Date());

        expRef.add(e1);
        expRef.add(e2);
    }

    private void seedBudgetsIfNeeded(CollectionReference budRef, String uid) {
        Map<String, Object> b1 = new HashMap<>();
        b1.put("title", "Monthly Essentials");
        b1.put("totalAmount", 500.0);
        b1.put("category", "General");
        b1.put("frequency", "monthly");
        b1.put("startDate", new Date());
        b1.put("userId", "uid");
        b1.put("createdAt", new Date());
        b1.put("spentAmount", 0.0);

        Map<String, Object> b2 = new HashMap<>();
        b2.put("title", "Fun Money");
        b2.put("totalAmount", 150.0);
        b2.put("category", "Entertainment");
        b2.put("frequency", "monthly");
        b2.put("startDate", new Date());
        b2.put("userId", "uid");
        b2.put("createdAt", new Date());
        b2.put("spentAmount", 0.0);

        budRef.add(b1);
        budRef.add(b2);
    }
}
