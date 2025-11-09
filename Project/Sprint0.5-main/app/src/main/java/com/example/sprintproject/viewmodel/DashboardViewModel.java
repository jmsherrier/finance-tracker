package com.example.sprintproject.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.Budget;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import com.example.sprintproject.model.DashboardModel;
import com.github.mikephil.charting.utils.ColorTemplate;

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
    private final MutableLiveData<List<String>> barLabelsLive = new MutableLiveData<>(new ArrayList<>());

    public LiveData<PieData> getPieData() { return pieDataLive; }
    public LiveData<BarData> getBarData() { return barDataLive; }
    public LiveData<List<String>> getBarLabels() {  return barLabelsLive;   }

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
        List<PieEntry> pieEntries = new ArrayList<>();
        if (data != null && data.get("categories") instanceof Map) {
            Map<String, Double> categories = (Map<String, Double>) data.get("categories");
            if (categories != null && !categories.isEmpty()) {
                for (Map.Entry<String, Double> e : categories.entrySet()) {
                    double v = e.getValue() == null ? 0.0 : e.getValue();
                    pieEntries.add(new PieEntry((float) v, e.getKey()));
                }
            }
        }
        if (pieEntries.isEmpty()) {
            pieEntries.add(new PieEntry(1f, "No Data"));
        }
        PieDataSet pieSet = new PieDataSet(pieEntries, "Expenses by Category");
        pieSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieSet.setSliceSpace(2f);
        pieSet.setValueTextSize(12f);
        pieDataLive.postValue((new PieData(pieSet)));

        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (data != null && data.get("budgets") instanceof List) {
            List<Budget> budgets = (List<Budget>) data.get("budgets");
            if (budgets != null && !budgets.isEmpty()) {
                for (int i = 0; i < budgets.size(); i++) {
                    Budget b = budgets.get(i);
                    float spent = (float) b.getSpentAmount();
                    float remaining = (float) Math.max(0.0, b.getTotalAmount() - b.getSpentAmount());
                    barEntries.add(new BarEntry(i, new float[]{spent, remaining}));
                    labels.add(b.getTitle() == null ? ("Budget " + (i + 1)) : b.getTitle());
                }
            }
        }
        if (barEntries.isEmpty()) {
            barEntries.add(new BarEntry(0, new float[]{0f, 1f}));
            labels.add("No Budgets");
        }
        BarDataSet barSet = new BarDataSet(barEntries, "Budget Spent vs Remaining");
        barSet.setStackLabels(new String[]{"Spent", "Remaining"});
        barSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieSet.setValueTextSize(10f);
        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.6f);
        barDataLive.postValue(barData);
        barLabelsLive.postValue(labels);
    }

}
