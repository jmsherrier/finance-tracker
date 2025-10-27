import java.time.LocalDate;

public abstract class Task implements Main_Task {
    protected String taskTitle;
    protected String taskDescription;
    protected LocalDate taskDeadLine;
    protected String taskStatus;
    protected int priority;

    public Task(String taskTitle, String taskDescription, LocalDate taskDeadLine, int priority) {
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskDeadLine = taskDeadLine;
        this.priority = priority;
        this.taskStatus = "To Do";
    }

    @Override
    public void updateStatus(String newStatus) {
        this.taskStatus = newStatus;
    }

    @Override
    public void updateDescription(String newDescription) {
        this.taskDescription = newDescription;
    }

    @Override
    public void markComplete() {
        this.taskStatus = "Complete";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Task o = (Task) obj;
        if (this.priority == o.priority &&
               this.taskTitle.equals(o.taskTitle) &&
               this.taskDescription.equals(o.taskDescription) &&
               this.taskDeadLine.equals(o.taskDeadLine) &&
               this.taskStatus.equals(o.taskStatus)) {
                return true;
               }
        return false;
    }

    public String getTaskTitle() {
        return this.taskTitle;
    }

    public String getTaskDescription() {
        return this.taskDescription;
    }

    public LocalDate getTaskDeadLine() {
        return this.taskDeadLine;
    }

    public String getTaskStatus() { 
        return this.taskStatus;
    }

    public int getPriority() {
        return this.priority;
    }
}
