package com.example.sprintproject.model;

import java.util.Calendar;
import java.util.Date;

public class GlobalDateManager {
    private static GlobalDateManager manager;
    private Date currentDate;

    private GlobalDateManager() {
        currentDate = Calendar.getInstance().getTime();
    }

    public static synchronized GlobalDateManager getInstance() {
        if (manager == null) {
            manager = new GlobalDateManager();
        }
        return manager;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date newDate) {
        this.currentDate = newDate;
    }
}
