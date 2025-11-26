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
    private static Properties properties;

    // Private constructor to prevent instantiation
    private ConfigHelper() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }


    /**
     * Gets a property value by key.
     * For Android, we'll use a simpler approach with BuildConfig fields
     * or read from a file in assets/res.
     *
     * @return the property value or null if not found
     */
    public static String getProperty() {
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


