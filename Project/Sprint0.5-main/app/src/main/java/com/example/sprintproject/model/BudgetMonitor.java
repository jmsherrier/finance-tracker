package com.example.sprintproject.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sprintproject.utils.NotificationQueue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BudgetMonitor {
    private final NotificationQueue queue;
    private final int[] thresholds;
    private final Set<String> shownSet  = new HashSet<>();
    private final SharedPreferences prefs;
    private final String prefsKey;

    public BudgetMonitor(Context ctx, NotificationQueue queue, int[] thresholds, String prefsKey) {
        this.queue = queue;
        this.thresholds = thresholds != null ? thresholds : new int[] {80, 90};
        this.prefsKey = prefsKey;
        this.prefs = ctx.getSharedPreferences("budget_monitor", Context.MODE_PRIVATE);
        loadPresisted();
    }

    public BudgetMonitor(Context ctx, int[] thresholds, String prefsKey) {
        this(ctx, null, thresholds, prefsKey);
    }

    private void loadPresisted() {
        if (prefsKey == null) return;
        String joined = prefs.getString(prefsKey, null);
        if (joined == null || joined.isEmpty()) return;
        for (String s : joined.split(",")) if (!s.isEmpty()) shownSet.add(s);
    }

    private void persist() {
        if (prefsKey == null) return;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : shownSet) {
            if (!first) sb.append(",");
            sb.append(s);
            first = false;
        }
        prefs.edit().putString(prefsKey, sb.toString()).apply();
    }

    public void onBudgetsUpdated(List<Budget> budgets) {
        if (queue == null) {
            List<ThresholdNotification> notifs = checkBudgetsForNewNotif(budgets);
            return;
        }
        if (budgets == null) return;
        for (Budget b : budgets) {
            if (b == null) continue;
            double total = b.getTotalAmount();
            double spent = b.getSpentAmount();
            if (total <= 0) continue;
            double progress = Math.max(0.0, Math.min(1.0, spent / total));
            for (int t : thresholds) {
                if (progress * 100.0 >= t) {
                    String key = b.getId() + "_" + t;
                    if (!shownSet.contains(key)) {
                        shownSet.add(key);
                        persist();
                        ThresholdNotification tn = new ThresholdNotification(b.getId(), b.getTitle(), t, progress);
                        queue.enqueue(tn);
                    }
                }
            }
        }
    }
    public List<ThresholdNotification> checkBudgetsForNewNotif(List<Budget> budgets) {
        List<ThresholdNotification> result = new ArrayList<>();
        if (budgets == null) return result;
        for (Budget b : budgets) {
            if (b == null) continue;
            double total = b.getTotalAmount();
            double spent = b.getSpentAmount();
            if (total <= 0) continue;
            double progress = Math.max(0.0, Math.min(1.0, spent / total));
            for (int t : thresholds) {
                if (progress * 100.0 >= t) {
                    String key = b.getId() + "_" + t;
                    if (!shownSet.contains(key)) {
                        shownSet.add(key);
                        persist();
                        ThresholdNotification tn = new ThresholdNotification(b.getId(), b.getTitle(), t, progress);
                        result.add(tn);
                    }
                }
            }
        }
        return result;
    }

    public void clearShownForBudgetIfBelow(Budget b) {
        if (b == null) return;
        double total = b.getTotalAmount();
        double spent = b.getSpentAmount();
        if (total <= 0) return;
        double progress = Math.max(0.0, Math.min(1.0, spent / total));
        for (int t : thresholds) {
            if (progress * 100.0 < t) shownSet.remove(b.getId() + "_" + t);
        }
        persist();
    }
}
