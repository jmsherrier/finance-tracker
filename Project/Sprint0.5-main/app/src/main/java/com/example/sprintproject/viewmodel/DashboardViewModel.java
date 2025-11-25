package com.example.sprintproject.viewmodel;

import android.util.Log;

import com.example.sprintproject.FirestoreManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.DashboardModel;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DashboardViewModel extends ViewModel {

    private final DashboardModel repository = new DashboardModel();

    private final MutableLiveData<Date> currentDate =
            new MutableLiveData<>(new Date());

    private final MutableLiveData<PieData> pieDataLive =
            new MutableLiveData<>();
    private final MutableLiveData<BarData> barDataLive =
            new MutableLiveData<>();
    private final MutableLiveData<List<String>> barLabelsLive =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Boolean> showExpenseReminder =
            new MutableLiveData<>(false);

    // Combined data, recomputed whenever currentDate changes
    private final LiveData<Map<String, Object>> dashboardData =
            Transformations.switchMap(currentDate, repository::getDashboardData);

    public LiveData<PieData> getPieData() {
        return pieDataLive;
    }

    public LiveData<BarData> getBarData() {
        return barDataLive;
    }

    public LiveData<List<String>> getBarLabels() {
        return barLabelsLive;
    }

    public LiveData<Map<String, Object>> getDashboardData() {
        return dashboardData;
    }

    public LiveData<Date> getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date date) {
        currentDate.setValue(date);
    }


    public LiveData<Boolean> getExpenseReminder() {
        return showExpenseReminder;
    }

    @SuppressWarnings("unchecked")
    public void updateCharts(Map<String, Object> data) {
        updatePieChart(data);
        updateBarChart(data);
    }

    @SuppressWarnings("unchecked")
    private void updatePieChart(Map<String, Object> data) {
        List<PieEntry> pieEntries = buildPieEntries(data);

        if (pieEntries.isEmpty()) {
            pieEntries.add(new PieEntry(1f, "No Data"));
        }

        PieDataSet pieSet = new PieDataSet(pieEntries, "Expenses by Category");
        pieSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieSet.setSliceSpace(2f);
        pieSet.setValueTextSize(12f);
        PieData pieData = new PieData(pieSet);
        pieDataLive.postValue(pieData);
    }

    @SuppressWarnings("unchecked")
    private List<PieEntry> buildPieEntries(Map<String, Object> data) {
        List<PieEntry> pieEntries = new ArrayList<>();
        if (data != null && data.get("categories") instanceof Map) {
            Map<String, Double> categories =
                    (Map<String, Double>) data.get("categories");
            if (categories != null && !categories.isEmpty()) {
                for (Map.Entry<String, Double> e : categories.entrySet()) {
                    double v = normalizeValue(e.getValue());
                    pieEntries.add(new PieEntry((float) v, e.getKey()));
                }
            }
        }
        return pieEntries;
    }

    private double normalizeValue(Double value) {
        double v = value == null ? 0.0 : value;
        return v < 0 ? 0.0 : v;
    }

    @SuppressWarnings("unchecked")
    private void updateBarChart(Map<String, Object> data) {
        BarChartData chartData = buildBarChartData(data);

        if (chartData.barEntries.isEmpty()) {
            chartData.barEntries.add(new BarEntry(0, new float[]{0f, 1f}));
            chartData.labels.add("No Budgets");
        }

        BarDataSet barSet =
                new BarDataSet(chartData.barEntries, "Budget Spent vs Remaining");
        barSet.setStackLabels(new String[]{"Spent", "Remaining"});
        barSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barSet.setValueTextSize(10f);

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.6f);

        barDataLive.postValue(barData);
        barLabelsLive.postValue(chartData.labels);
    }

    @SuppressWarnings("unchecked")
    private BarChartData buildBarChartData(Map<String, Object> data) {
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (data != null && data.get("budgets") instanceof List) {
            List<Budget> budgets = (List<Budget>) data.get("budgets");
            if (budgets != null && !budgets.isEmpty()) {
                for (int i = 0; i < budgets.size(); i++) {
                    Budget b = budgets.get(i);
                    if (b != null) {
                        addBudgetBarEntry(barEntries, labels, b, i);
                    }
                }
            }
        }

        return new BarChartData(barEntries, labels);
    }

    private void addBudgetBarEntry(List<BarEntry> barEntries, List<String> labels,
                                   Budget budget, int index) {
        float spent = (float) budget.getSpentAmount();
        float remaining = (float) Math.max(0.0,
                budget.getTotalAmount() - budget.getSpentAmount());
        barEntries.add(new BarEntry(index, new float[]{spent, remaining}));
        String title = budget.getTitle();
        labels.add(title == null || title.isEmpty()
                ? ("Budget " + (index + 1))
                : title);
    }

    private static class BarChartData {
        final List<BarEntry> barEntries;
        final List<String> labels;

        BarChartData(List<BarEntry> barEntries, List<String> labels) {
            this.barEntries = barEntries;
            this.labels = labels;
        }
    }

    public void checkExpenseReminder() {
        Log.d("REMINDER_DEBUG", "checkExpenseReminder() called");

        FirebaseAuth auth = FirestoreManager.getInstance().getAuth();
        String userId = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : null;

        Log.d("REMINDER_DEBUG", "User ID = " + userId);

        FirestoreManager.getInstance().getLastExpenseDate(userId, task -> {
            if (!task.isSuccessful()) {
                Log.e("REMINDER_DEBUG", "Query failed: ", task.getException());
                return;
            }

            QuerySnapshot snap = task.getResult();
            Log.d("REMINDER_DEBUG", "Snap size = " + (snap == null ? -1 : snap.size()));

            if (snap.isEmpty()) {
                Log.d("REMINDER_DEBUG", "No expenses found → showing reminder");
                showExpenseReminder.postValue(true);
                return;
            }

            DocumentSnapshot doc = snap.getDocuments().get(0);
            Date last = doc.getDate("date");
            Log.d("REMINDER_DEBUG", "Last expense date = " + last);

            long diff = (System.currentTimeMillis() - last.getTime()) / (1000L * 60 * 60 * 24);
            Log.d("REMINDER_DEBUG", "Days since last expense = " + diff);

            showExpenseReminder.postValue(diff >= 3);
        });
    }



    // For tests / cleanup
    public void clearData() {
        repository.stopListening();
        pieDataLive.setValue(null);
        barDataLive.setValue(null);
        barLabelsLive.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }
}

