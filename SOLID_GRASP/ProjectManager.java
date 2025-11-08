public class ProjectManager extends TeamMember {
    private Project project;

    public ProjectManager(String name, int age, String email, String gender) {
        super(name, age, email, gender);
    }

    public void startProject(Project newProject) {
        this.project = newProject;
    }

    public Project getProject() {
        return this.project;
    }
}