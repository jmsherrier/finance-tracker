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
}