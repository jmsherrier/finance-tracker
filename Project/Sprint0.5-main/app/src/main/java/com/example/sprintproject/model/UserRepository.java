package com.example.sprintproject.model;

import androidx.annotation.NonNull;

// Simple repository placeholder. In a real app this would wrap Firebase or local DB operations.
public class UserRepository {

    public interface CreateUserCallback {
        void onSuccess(User user);
        void onError(@NonNull String error);
    }

    public void createUser(String email, String password, CreateUserCallback callback) {
        // Placeholder: immediately return a fake user for template
        User user = new User("uid-123", email);
        callback.onSuccess(user);
    }
}