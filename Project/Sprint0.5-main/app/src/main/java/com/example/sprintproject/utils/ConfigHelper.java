package com.example.sprintproject.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Helper class to read configuration values from local.properties.
 * Used for storing API keys and other sensitive configuration.
 */
public class ConfigHelper {
    private static final String TAG = "ConfigHelper";
    private static final String PROPERTIES_FILE = "local.properties";
    private static Properties properties;

    /**
     * Loads properties from local.properties file.
     * This is called once and cached.
     */
    private static void loadProperties() {
        if (properties != null) {
            return; // Already loaded
        }

        properties = new Properties();
        try {
            // Try to find local.properties in the project root
            // For Android, we'll use a different approach
            // Since local.properties is at project root, we need to read it differently
            // For now, we'll use BuildConfig or a simpler approach
            
            // Note: In Android, local.properties is typically read by Gradle
            // We'll use a different approach - read from a resource or use BuildConfig
            // For simplicity, we'll create a method that can be overridden
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading properties", e);
        }
    }

    /**
     * Gets a property value by key.
     * For Android, we'll use a simpler approach with BuildConfig fields
     * or read from a file in assets/res.
     *
     * @param key the property key
     * @return the property value or null if not found
     */
    public static String getProperty(String key) {
        // For now, return null - we'll set it via a different method
        // This allows flexibility in how the API key is provided
        return null;
    }

    /**
     * Gets the Hugging Face API key.
     * This method tries multiple sources:
     * 1. System property (for testing)
     * 2. Environment variable
     * 3. Returns null (will need to be set manually)
     *
     * @return the API key or null
     */
    public static String getHuggingFaceApiKey() {
        // Try system property first (useful for testing)
        String apiKey = System.getProperty("HUGGING_FACE_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }

        // Try environment variable
        apiKey = System.getenv("HUGGING_FACE_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }

        // For now, return null - user needs to set it manually
        // We'll provide instructions for setting it in ChatbotFragment
        return null;
    }
}


