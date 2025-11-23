package com.example.sprintproject.solid_grasp;

import java.time.LocalDate;

public class RepeatingTask extends Task {
    private int repeatTime;

    public RepeatingTask(String taskTitle, String taskDescription, LocalDate taskDeadLine, int priority, int repeatTime) {
        super(taskTitle, taskDescription, taskDeadLine, priority);
        this.repeatTime = repeatTime;
    }

    public void extendDeadlineInDays() {
        this.taskDeadLine = this.taskDeadLine.plusDays(repeatTime);
    }

    public void extendDeadlineInWeeks() {
        this.taskDeadLine = this.taskDeadLine.plusWeeks(repeatTime);
    }

    public void extendDeadlineInMonths() {
        this.taskDeadLine = this.taskDeadLine.plusMonths(repeatTime);
    }

    public void extendDeadlineInYears() {
        this.taskDeadLine = this.taskDeadLine.plusYears(repeatTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        RepeatingTask that = (RepeatingTask) obj;
        return repeatTime == that.repeatTime;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), repeatTime);
    }
}