package com.example.sprintproject.solid_grasp;

import java.util.Objects;

public class TeamMember {
    private String name;
    private int age;
    private String email;
    private String gender;
    private Task task;

    public TeamMember(String name, int age, String email, String gender) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.gender = gender;
    }

    public void assignTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return this.task;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        TeamMember o = (TeamMember) obj;
        return this.age == o.age &&
               this.name.equals(o.name) &&
               this.email.equals(o.email) &&
               this.gender.equals(o.gender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, email, gender);
    }
}
