import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Project {
    private String projName;
    private String projDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<MainTask> tasks;
    private List<TeamMember> members;
    private ProjectManager projManager;

    public Project(String name, String description, LocalDate startDate, LocalDate endDate, ProjectManager projManager) {
        this.projName = name;
        this.projDescription = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tasks = new ArrayList<>();
        this.members = new ArrayList<>();
        this.projManager = projManager;
    }

    public void addTask(MainTask task) {
        tasks.add(task);
    }

    public void removeTask(MainTask task) {
        tasks.remove(task);
    }

    public void addMember(TeamMember member) {
        members.add(member);
    }

    public void removeMember(TeamMember member) {
        members.remove(member);
    }

    public String getProjectName() {
        return this.projName;
    }

    public String getProjDescription() {
        return this.projDescription;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public ProjectManager getProjectManager() {
        return this.projManager;
    }
}
