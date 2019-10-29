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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.expediagroup.drsquidutils.model.LoggingLevel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests our FilteredLogger using methodology tastefully stolen from here:
 * https://jsoftbiz.wordpress.com/2011/11/29/unit-testing-asserting-that-a-line-was-logged-by-logback/
 */
public class FilteredLoggerTest {

    // Our filteredLogger and mocked appender on that filteredLogger.
    private FilteredLogger filteredLogger;
    private Appender mockAppender;

    // Testing constants.
    private String message = "test 1 true 6.66";
    private String format = "{} {} {} {}";
    private Object[] objects = {"test", 1, true, 6.66};


    /**
     * Before each test we will set up the root logger with a mocked appender. This is so, later, we can verify that
     * the appender was or wasn't called and match values, effectively testing logger output.
     */
    @Before
    public void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        filteredLogger = new FilteredLogger(root);
        mockAppender = mock(Appender.class);
        root.addAppender(mockAppender);
    }

    /**
     * Verifies that the logging message produced matches the expected message.
     *
     * @param expected The message we expected to log.
     */
    private void verifyLoggingMessage(String expected) {

        verify(mockAppender, atLeastOnce()).doAppend(argThat(new ArgumentMatcher() {
            @Override
            public boolean matches(final Object argument) {
                return ((LoggingEvent) argument).getFormattedMessage().contains(expected);
            }
        }));
    }

    /**
     * Verifies that no logging message was produced.
     */
    private void verifyNoLoggingMessage() {
        verifyZeroInteractions(mockAppender);
    }

    /**
     * Verifies that DEBUG logs are going through.
     */
    private void verifyDebugSucceeds() {
        filteredLogger.debug(message);
        verifyLoggingMessage(message);
        filteredLogger.debug(format, objects);
        verifyLoggingMessage(message);
    }

    /**
     * Verifies that INFO logs are going through.
     */
    private void verifyInfoSucceeds() {
        filteredLogger.info(message);
        verifyLoggingMessage(message);
        filteredLogger.info(format, objects);
        verifyLoggingMessage(message);
    }

    /**
     * Verifies that WARN logs are going through.
     */
    private void verifyWarnSucceeds() {
        filteredLogger.warn(message);
        verifyLoggingMessage(message);
        filteredLogger.warn(format, objects);
        verifyLoggingMessage(message);
    }

    /**
     * Verifies that ERROR logs are going through.
     */
    private void verifyErrorSucceeds() {
        filteredLogger.error(message);
        verifyLoggingMessage(message);
        filteredLogger.error(format, objects);
        verifyLoggingMessage(message);
    }

    /**
     * Verifies that DEBUG logs aren't going through.
     */
    private void verifyDebugFails() {
        filteredLogger.debug(message);
        verifyNoLoggingMessage();
        filteredLogger.debug(format, objects);
        verifyNoLoggingMessage();
    }

    /**
     * Verifies that INFO logs aren't going through.
     */
    private void verifyInfoFails() {
        filteredLogger.info(message);
        verifyNoLoggingMessage();
        filteredLogger.info(format, objects);
        verifyNoLoggingMessage();
    }

    /**
     * Verifies that WARN logs aren't going through.
     */
    private void verifyWarnFails() {
        filteredLogger.warn(message);
        verifyNoLoggingMessage();
        filteredLogger.warn(format, objects);
        verifyNoLoggingMessage();
    }

    /**
     * Verifies that ERROR logs aren't going through.
     */
    private void verifyErrorFails() {
        filteredLogger.error(message);
        verifyNoLoggingMessage();
        filteredLogger.error(format, objects);
        verifyNoLoggingMessage();
    }

    /**
     * Tests all scenarios with DEBUG logging level.
     */
    @Test
    public void testDebugLoggingLevel() {

        FilteredLogger.setLoggingLevel(LoggingLevel.DEBUG);
        FilteredLogger.setForceLogging(false);

        verifyDebugSucceeds();
        verifyInfoSucceeds();
        verifyWarnSucceeds();
        verifyErrorSucceeds();

        FilteredLogger.setForceLogging(true);

        verifyDebugSucceeds();
        verifyInfoSucceeds();
        verifyWarnSucceeds();
        verifyErrorSucceeds();
    }

    /**
     * Tests all scenarios with INFO logging level.
     */
    @Test
    public void testInfoLoggingLevel() {

        FilteredLogger.setLoggingLevel(LoggingLevel.INFO);
        FilteredLogger.setForceLogging(false);

        verifyDebugFails();
        verifyInfoSucceeds();
        verifyWarnSucceeds();
        verifyErrorSucceeds();

        FilteredLogger.setForceLogging(true);

        verifyDebugSucceeds();
        verifyInfoSucceeds();
        verifyWarnSucceeds();
        verifyErrorSucceeds();
    }

    /**
     * Tests all scenarios with WARN logging level.
     */
    @Test
    public void testWarnLoggingLevel() {

        FilteredLogger.setLoggingLevel(LoggingLevel.WARN);
        FilteredLogger.setForceLogging(false);

        verifyDebugFails();
        verifyInfoFails();
        verifyWarnSucceeds();
        verifyErrorSucceeds();

        FilteredLogger.setForceLogging(true);

        verifyDebugSucceeds();
        verifyInfoSucceeds();
        verifyWarnSucceeds();
        verifyErrorSucceeds();
    }

    /**
     * Tests all scenarios with ERROR logging level.
     */
    @Test
    public void testErrorLoggingLevel() {

        FilteredLogger.setLoggingLevel(LoggingLevel.ERROR);
        FilteredLogger.setForceLogging(false);

        verifyDebugFails();
        verifyInfoFails();
        verifyWarnFails();
        verifyErrorSucceeds();

        FilteredLogger.setForceLogging(true);

        verifyDebugSucceeds();
        verifyInfoSucceeds();
        verifyWarnSucceeds();
        verifyErrorSucceeds();
    }

    /**
     * Tests all scenarios with NONE logging level.
     */
    @Test
    public void testNoneLoggingLevel() {

        FilteredLogger.setLoggingLevel(LoggingLevel.NONE);
        FilteredLogger.setForceLogging(false);

        verifyDebugFails();
        verifyInfoFails();
        verifyWarnFails();
        verifyErrorFails();

        FilteredLogger.setForceLogging(true);

        verifyDebugSucceeds();
        verifyInfoSucceeds();
        verifyWarnSucceeds();
        verifyErrorSucceeds();
    }

}