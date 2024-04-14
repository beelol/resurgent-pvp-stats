package com.mcresurgence;
import org.apache.logging.log4j.Logger;

public class ModLogger {
    private Logger logger;
    private String prefix;

    public ModLogger(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
    }

    public void info(String message) {
        logger.info(prefix + message);
    }

    public void warn(String message) {
        logger.warn(prefix + message);
    }

    public void error(String message) {
        logger.error(prefix + message);
    }

    public void debug(String message) {
        logger.debug(prefix + message);
    }

    // Additional logging methods as needed
}
