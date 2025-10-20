package com.example.sprintproject.utils;

import android.content.Context;

import java.io.File;

public class Utils {
    public static void clearCache(Context context) {
        try {
            File cache = context.getCacheDir();
            File appDir = context.getFilesDir();
            deleteDir(cache);
            deleteDir(appDir);
            context.getSharedPreferences("user_prefs", 0).edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
        }
        return dir != null && dir.delete();
    }
}
