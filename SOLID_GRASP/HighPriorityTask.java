package com.example.sprintproject.solid_grasp;

import java.time.LocalDate;
import java.util.logging.Logger;

public class HighPriorityTask extends Task {
    private static final Logger logger = Logger.getLogger(HighPriorityTask.class.getName());
    private String priorityLevel;

    public HighPriorityTask(String taskTitle, String taskDescription, LocalDate taskDeadLine, int priority) {
        super(taskTitle, taskDescription, taskDeadLine, priority);

        if (priority >= 1 && priority <= 3) {
            this.priorityLevel = "Low";
        } else if (priority >= 4 && priority <= 6) {
            this.priorityLevel = "Medium";
        } else {
            this.priorityLevel = "High";
        }
    }

    public void updateDeadLine(LocalDate newDeadLine) {
        if (priorityLevel.equals("High")) {
            this.taskDeadLine = newDeadLine;
        } else {
            logger.warning("This task's priority is not high enough to update the deadline!");
        }
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        HighPriorityTask that = (HighPriorityTask) obj;
        return priorityLevel != null ? priorityLevel.equals(that.priorityLevel) : that.priorityLevel == null;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), priorityLevel);
    }
}
