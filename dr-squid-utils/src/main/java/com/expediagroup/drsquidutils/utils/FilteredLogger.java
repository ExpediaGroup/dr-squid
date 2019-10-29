/**
 * Copyright (C) 2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.drsquidutils.utils;

import com.expediagroup.drsquidutils.model.LoggingLevel;
import org.slf4j.Logger;

/**
 * A wrapper class for a standard logger where we can more finely control the logging level.
 * Use this when you wish to be in more direct control of logging levels.
 */
public class FilteredLogger {

    // The logger we are wrapped around.
    private final Logger logger;

    // The level, below which we shouldn't be logging.
    // Static, so that all filtered loggers are aligned to the same logging level setting.
    private static LoggingLevel loggingLevel = LoggingLevel.ERROR;

    // If logging is forced, all logs always go through.
    // Static, so that all filtered loggers are aligned to the same force logging setting.
    private static boolean forceLogging = false;

    /**
     * Construct a new FilteredLogger.
     *
     * @param logger The logger we are wrapping around.
     */
    public FilteredLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Get the logging level.
     */
    public static LoggingLevel getLoggingLevel() {
        return loggingLevel;
    }

    /**
     * Set the logging level.
     *
     * @param loggingLevel The logging level.
     */
    public static void setLoggingLevel(LoggingLevel loggingLevel) {
        FilteredLogger.loggingLevel = loggingLevel;
    }

    /**
     * Get whether logging is forced.
     */
    public static boolean isForceLogging() {
        return FilteredLogger.forceLogging;
    }

    /**
     * Set whether logging is forced.
     *
     * @param forceLogging Whether to force logging.
     */
    public static void setForceLogging(boolean forceLogging) {
        FilteredLogger.forceLogging = forceLogging;
    }

    /**
     * Log the message based on its level vs. the logger level, as well as whether logging is forced.
     *
     * @param messageLevel The level of the message we are attempting to log.
     * @param message      The message we are attempting to log.
     */
    private void log(LoggingLevel messageLevel, String message) {

        // Only log if forcing logging, or if the logger level is below or equal to the message level
        if (forceLogging || loggingLevel.ordinal() <= messageLevel.ordinal()) {
            switch (messageLevel) {
                case DEBUG:
                    logger.debug(message);
                    break;
                case INFO:
                    logger.info(message);
                    break;
                case WARN:
                    logger.warn(message);
                    break;
                case ERROR:
                    logger.error(message);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Log the message based on its level vs. the logger level, as well as whether logging is forced.
     *
     * @param messageLevel The level of the message we are attempting to log.
     * @param format       The format string for the message we are attempting to log.
     * @param objects      The objects to be inserted into the format string.
     */
    private void log(LoggingLevel messageLevel, String format, Object... objects) {

        // Only log if forcing logging, or if the logger level is below or equal to the message level
        if (forceLogging || loggingLevel.ordinal() <= messageLevel.ordinal()) {
            switch (messageLevel) {
                case DEBUG:
                    logger.debug(format, objects);
                    break;
                case INFO:
                    logger.info(format, objects);
                    break;
                case WARN:
                    logger.warn(format, objects);
                    break;
                case ERROR:
                    logger.error(format, objects);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Log a debug message based on a String.
     *
     * @param message Message to log.
     */
    public void debug(String message) {
        log(LoggingLevel.DEBUG, message);
    }

    /**
     * Log a debug message based on a format and objects.
     *
     * @param format  Format of the message to log.
     * @param objects Objects to insert into the the format.
     */
    public void debug(String format, Object... objects) {
        log(LoggingLevel.DEBUG, format, objects);
    }

    /**
     * Log an info message based on a String.
     *
     * @param message Message to log.
     */
    public void info(String message) {
        log(LoggingLevel.INFO, message);
    }

    /**
     * Log an info message based on a format and objects.
     *
     * @param format  Format of the message to log.
     * @param objects Objects to insert into the the format.
     */
    public void info(String format, Object... objects) {
        log(LoggingLevel.INFO, format, objects);
    }

    /**
     * Log a warn message based on a String.
     *
     * @param message Message to log.
     */
    public void warn(String message) {
        log(LoggingLevel.WARN, message);
    }

    /**
     * Log a warn message based on a format and objects.
     *
     * @param format  Format of the message to log.
     * @param objects Objects to insert into the the format.
     */
    public void warn(String format, Object... objects) {
        log(LoggingLevel.WARN, format, objects);
    }

    /**
     * Log an error message based on a String.
     *
     * @param message Message to log.
     */
    public void error(String message) {
        log(LoggingLevel.ERROR, message);
    }

    /**
     * Log an error message based on a format and objects.
     *
     * @param format  Format of the message to log.
     * @param objects Objects to insert into the the format.
     */
    public void error(String format, Object... objects) {
        log(LoggingLevel.ERROR, format, objects);
    }

}
