package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.DashboardModel;

import java.util.Date;
import java.util.Map;

public class DashboardViewModel extends ViewModel {
    private final DashboardModel repository = new DashboardModel();
    private final MutableLiveData<Date> currentDate = new MutableLiveData<>(new Date());

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
}

