package com.project.bitpacking.util;

/**
 * Centralized logging utility with a single DEBUG flag to control all logging.
 */
public final class Logger {
    /**
     * Set this to true to enable debug logging, false to disable.
     * This single flag controls all logging in the application.
     */
    public static boolean DEBUG = false;

    private Logger() {
    }

    public static void debug(String message) {
        if (DEBUG) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public static void debug(String format, Object... args) {
        if (DEBUG) {
            System.out.printf("[DEBUG] " + format + "%n", args);
        }
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void info(String format, Object... args) {
        System.out.printf("[INFO] " + format + "%n", args);
    }

    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    public static void error(String format, Object... args) {
        System.err.printf("[ERROR] " + format + "%n", args);
    }
}


