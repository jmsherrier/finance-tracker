package com.example.sprintproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Singleton class that manages Firestore and Firebase Auth instances.
 * Ensures only one instance of database connection exists throughout the app.
 */
public class FirestoreManager {
    private static FirestoreManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // Private constructor prevents direct instantiation
    private FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Thread-safe singleton instance getter
    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }
}
