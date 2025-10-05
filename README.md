Joseph, Yitian, Shreya, Austin, Bogdan, Nehal

# CS 2340 Fall 2025 Repository
This repository is meant to be used throughout the semester to help you organize your work. This will be the repository you send out to your teammates as you work on your various group assignments throught the semester.

**Please note the current directory structure, which should be maintained as you use this repository in the future.**
- `Project` This directory currently contains an Android Studio Project called `Sprint0.5-main`. This is a template project you should feel free to use in order to set up your group's project for the semester. If you decide to start your own Android Studio Project from scratch, we recommend that you save it inside the `Project` folder as well.
> *Note that `Sprint0.5-main` is a Project folder, which means Android Studio will recognize it as a project if and only if it is opened at this level.* If you attempt to open the project by selecting any other parent or child directory, you will run into issues as the program will not know which *gradle* configurations to use.
- `SOLID_GRASP` This directory will be used later in the semester for your group assignments related to SOLID/GRASP.
- `Code Smell` This directory will be used later in the semester for your group assignments related to Code Smells.

````markdown
# Class Project Git & Jira Guidelines

This repository is for our class project. It contains the main code and instructions for collaborating using Git and Jira.

## Git Branching Strategy

We use the following branches:

- **main**: The stable version of the project. Only tested code should go here.
- **original**: The starting point or template code.
- **individual-name branches**: Each team member creates a branch with their name (e.g., `alice`, `bob`) for their work.
````
### Basic Git Procedures

0. **Set up coding environment**
   
    * (Optional - Highly Recommended) download [GitHub Desktop](https://docs.github.com/en/desktop/overview/about-github-desktop)
    * (Optional) Verify Git Pro status with student ID
    * Follow canvas directions to install Git and contribute

2. **Clone the repo**
   ```bash
   git clone <repo-url>
   ```
   
3. **Create a branch**

   ```bash
   git checkout -b yourname
   ```

4. **Work and commit changes**

   ```bash
   git add .
   git commit -m "PROJ-123: short description of changes"
   ```

5. **Push your branch**

   ```bash
   git push origin yourname
   ```

6. **Update main**

   * Pull latest changes from `main` before merging:

     ```bash
     git checkout main
     git pull origin main
     ```

7. **Merge your work**

   * Open a Pull Request (PR) to merge your branch into `main` after review.
   * Resolve any conflicts before merging.

---

## Jira Usage

* Each task or feature should have a Jira issue. Create Jira issues on the [Jira project page.](https://jmsherrier.atlassian.net/jira/software/projects/MBA/summary?atlOrigin=eyJpIjoiZjJhOGUzNjFlNjIzNDNlNDhiNjIzMjZiNzZmZDVlYjEiLCJwIjoiaiJ9)
* Include the **Jira issue key** in branch names and commit messages:

  ```
  PROJ-123: Implement login validation
  ```
* This ensures commits, branches, and PRs are linked to Jira issues.

---

## Good Practices

* Commit often with clear messages.
* Sync with `main` frequently to avoid conflicts.
* Each team member works in their own branch.
* Keep `main` stable; only merge tested code.
* Use Jira to track progress and communicate task status.

```
