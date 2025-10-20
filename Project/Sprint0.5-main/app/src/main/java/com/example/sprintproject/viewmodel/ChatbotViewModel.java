package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class ChatbotViewModel extends ViewModel {
    private final MutableLiveData<Date> currentDate = new MutableLiveData<>(new Date());

    public LiveData<Date> getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date date) {
        currentDate.setValue(date);
    }
}

