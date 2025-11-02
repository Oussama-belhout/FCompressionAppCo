package com.project.bitpacking;

import com.project.bitpacking.controller.ReplController;
import com.project.bitpacking.util.Logger;

/**
 * Main entry point for the Bit Packing Compression Application.
 */
public class Main {
    public static void main(String[] args) {
        // Enable/disable debug logging by changing this single flag
        Logger.DEBUG = false;

        // Enable via command line argument
        if (args.length > 0 && ("--debug".equals(args[0]) || "-d".equals(args[0]))) {
            Logger.DEBUG = true;
            Logger.debug("Debug logging enabled");
        }

        try {
            ReplController controller = new ReplController();
            controller.start();
        } catch (Exception e) {
            Logger.error("Fatal error: %s", e.getMessage());
            if (Logger.DEBUG) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}

