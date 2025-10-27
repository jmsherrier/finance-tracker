import java.time.LocalDate;

public class HighPriorityTask extends Task {
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
            System.out.println("This task's priority is not high enough to update the deadline!");
        }
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }
}
