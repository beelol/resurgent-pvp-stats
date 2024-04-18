package com.mcresurgence;

import org.apache.logging.log4j.Logger;

public class ModLogger {
    private Logger logger;
    private String prefix;
    private static final boolean VERBOSE = false;

    public ModLogger(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
    }

    public void info(String message) {
        String finalMessage = String.format("%s%s", prefix, message);
        logger.info(finalMessage);

        if (VERBOSE) {
            System.out.println(finalMessage);
        }
    }

    public void warn(String message) {
        String finalMessage = String.format("WARN: %s%s", prefix, message);
        logger.warn(finalMessage);

        if (VERBOSE) {
            System.out.println(finalMessage);
        }
    }

    public void error(String message) {
        String finalMessage = String.format("ERROR: %s%s", prefix, message);
        logger.error(finalMessage);

        if (VERBOSE) {
            System.out.println(finalMessage);
        }
    }

    public void error(String message, Exception e) {
        String finalMessage = String.format("ERROR: %s%s", prefix, message);
        logger.error(finalMessage);

        if (VERBOSE) {
            System.out.println(finalMessage);
        }
    }

    public void debug(String message) {
        String finalMessage = String.format("DEBUG: %s%s", prefix, message);
        logger.debug(finalMessage);

        if (VERBOSE) {
            System.out.println(finalMessage);
        }
    }

    // Additional logging methods as needed
}
