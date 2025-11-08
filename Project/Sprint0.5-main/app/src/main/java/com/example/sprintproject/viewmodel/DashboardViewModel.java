package com.example.sprintproject.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import com.example.sprintproject.model.DashboardModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardViewModel extends ViewModel {
    private final DashboardModel repository = new DashboardModel();
    private final MutableLiveData<Date> currentDate = new MutableLiveData<>(new Date());
    private final MutableLiveData<PieData> pieDataLive = new MutableLiveData<>();
    private final MutableLiveData<BarData> barDataLive = new MutableLiveData<>();
    private List<String> latestBarLabels = new ArrayList<>();

    public LiveData<PieData> getPieData() { return pieDataLive; }
    public LiveData<BarData> getBarData() { return barDataLive; }

    // Expose dashboard data as LiveData that updates when date changes
    private final LiveData<Map<String, Object>> dashboardData =
            Transformations.switchMap(currentDate, repository::getDashboardData);

    public LiveData<Map<String, Object>> getDashboardData() {
        return dashboardData;
    }

    public void setCurrentDate(Date date) {
        currentDate.setValue(date);
    }

    public LiveData<Date> getCurrentDate() {
        return currentDate;
    }

    public void updateCharts(Map<String, Object> data) {
        if (data == null) {
            List<PieEntry> placeholderPie = new ArrayList<>();
            placeholderPie.add(new PieEntry(1f, "No Data"));
            PieDataSet placeholderPieSet = new PieDataSet(placeholderPie, "Expenses by Category");
            placeholderPieSet.setSliceSpace(2f);
            pieDataLive.postValue((new PieData(placeholderPieSet)));

            List<BarEntry> placeholderBars = new ArrayList<>();
            placeholderBars.add(new BarEntry(0, new float[]{0f, 1f}));
            BarDataSet placeholderBarSet = new BarDataSet(placeholderBars, "Budget Spend vs Remaining");
            placeholderBarSet.setStackLabels(new String[]{"Spent", "Remaining"});
            barDataLive.postValue((new BarData(placeholderBarSet)));
            return;
        }
        Map<String, Double> categoryTotals = null;
        try {
            categoryTotals = (Map<String, Double>) data.get("categories");
        } catch (ClassCastException ignored) {  }

        List<PieEntry> pieEntries = new ArrayList<>();
        if (categoryTotals != null && categoryTotals.isEmpty()) {
            for (Map.Entry<String, Double> e : categoryTotals.entrySet()) {
                pieEntries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
            }
        } else {
            pieEntries.add(new PieEntry(1f, "No Data"));
        }
        PieDataSet pieSet = new PieDataSet(pieEntries, "Expenses by Category");
        pieSet.setSliceSpace(2f);
        pieSet.setValueTextSize(12f);
        pieDataLive.postValue(new PieData(pieSet));

        List<Budget> budgets = null;
        try {
            budgets =  (List<Budget>) data.get("budgets");
        } catch (ClassCastException ignored) {  }
        List<BarEntry> barEntries = new ArrayList<>();
        final List<String> budgetLabels = new ArrayList<>();
        if (budgets != null && !budgets.isEmpty()) {
            for (int i = 0; i < budgets.size(); i++) {
                Budget b = budgets.get(i);
                float spent = (float) b.getSpentAmount();
                float remaining = (float) Math.max(0.0, b.getTotalAmount() - b.getSpentAmount());
                barEntries.add(new BarEntry(i, new float[]{spent, remaining}));
                budgetLabels.add(b.getTitle() == null ? ("Budget " + (i + 1)) : b.getTitle());
            }
        } else {
            barEntries.add(new BarEntry(0, new float[]{0f, 1f}));
            budgetLabels.add("No Budgets");
        }
        BarDataSet barSet = new BarDataSet(barEntries, "Budget Spent vs Remaining");
        barSet.setStackLabels(new String[]{"Spent", "Remaining"});
        barSet.setValueTextSize(10f);
        BarData barData = new BarData(barSet);
        barDataLive.postValue(new BarData(barSet));
        Map<String, Object> meta = new HashMap<>();
        meta.put("barLabels", budgetLabels);
        this.latestBarLabels = budgetLabels;
    }

    //included this method so you can call resetDashboardData() in DashboardFragment.java so that our unit test can run
    public void clearData() {
        barDataLive.setValue(null);
        pieDataLive.setValue(null);
    }

}
