package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.GlobalDateManager;

import java.util.Date;


public class TimeViewModel extends ViewModel {
    private final MutableLiveData<Date> currentDate =
            new MutableLiveData<>(GlobalDateManager.getInstance().getCurrentDate());

    public LiveData<Date> getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date newDate) {
        GlobalDateManager.getInstance().setCurrentDate(newDate);
        currentDate.setValue(newDate);
    }
}
