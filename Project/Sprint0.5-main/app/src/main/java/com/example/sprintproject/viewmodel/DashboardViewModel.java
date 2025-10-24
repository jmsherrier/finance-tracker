package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.DashboardModel;

import java.util.Map;

public class DashboardViewModel extends ViewModel {
    private final DashboardModel repository = new DashboardModel();
    private final LiveData<Map<String, Object>> dashboardData;

    public DashboardViewModel() {
        // ✅ Directly load all dashboard data (no date filtering)
        dashboardData = repository.getDashboardData();
    }

    public LiveData<Map<String, Object>> getDashboardData() {
        return dashboardData;
    }
}

