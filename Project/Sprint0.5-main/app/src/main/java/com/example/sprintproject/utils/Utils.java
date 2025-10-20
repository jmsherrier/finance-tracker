package com.example.sprintproject.utils;

import android.content.Context;

import java.io.File;

/**
 * Utility helper methods for clearing app data and cache.
 */
public final class Utils {

    /** Private constructor to prevent instantiation. */
    private Utils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Clears the app's cache, files, and shared preferences.
     *
     * @param context the Android context
     */
    public static void clearCache(Context context) {
        try {
            File cache = context.getCacheDir();
            File appDir = context.getFilesDir();
            deleteDir(cache);
            deleteDir(appDir);
            context.getSharedPreferences("user_prefs", 0)
                    .edit()
                    .clear()
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param dir the directory to delete
     * @return true if successfully deleted, false otherwise
     */
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir != null && dir.delete();
    }
}
