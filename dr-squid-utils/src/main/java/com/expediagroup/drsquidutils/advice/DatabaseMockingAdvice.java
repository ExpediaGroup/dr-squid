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
package com.expediagroup.drsquidutils.advice;

import com.expediagroup.drsquidutils.configretriever.FileSystemConfigRetriever;
import com.expediagroup.drsquidutils.model.Database;
import com.expediagroup.drsquidutils.model.DatabaseType;
import com.expediagroup.drsquidutils.model.DrSquidConfig;
import com.expediagroup.drsquidutils.model.LoggingLevel;
import com.expediagroup.drsquidutils.utils.ConfigUtils;
import com.expediagroup.drsquidutils.utils.FilteredLogger;
import com.mongodb.MongoTimeoutException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.SQLDataException;

@Component
@Aspect
public class DatabaseMockingAdvice {

    private static final FilteredLogger LOGGER = new FilteredLogger(LoggerFactory.getLogger(DatabaseMockingAdvice.class));

    @Autowired
    private Environment environment;

    @Autowired
    private FileSystemConfigRetriever configRetriever;

    private DrSquidConfig getConfig(String name) {
        if (configRetriever == null) {
            configRetriever = new FileSystemConfigRetriever();
        }
        return configRetriever.getConfig(name);
    }

    @Around("execution(* org.springframework.data.mongodb.core.MongoOperations+.*(..))")
    public Object mongoCall(ProceedingJoinPoint pjp) throws Throwable {

        LOGGER.debug("Evaluating fault injection for your mongo call");

        String configName = ConfigUtils.getDrSquidConfigName(environment);
        DrSquidConfig drSquidConfig = getConfig(configName);
        if (drSquidConfig != null && BooleanUtils.isTrue(drSquidConfig.isEnabled()) && CollectionUtils.isNotEmpty(drSquidConfig.getDatabases())) {

            boolean faultInjection = false;
            for (Database db : drSquidConfig.getDatabases()) {
                if (db != null && DatabaseType.MONGO.equals(db.getType()) && db.isEnabled()) {
                    faultInjection = true;
                    LoggingLevel loggingLevel = db.getLoggingLevel();
                    if (loggingLevel != null) {
                        FilteredLogger.setLoggingLevel(loggingLevel);
                    }
                }
            }

            if (!faultInjection) {
                LOGGER.debug("Dr. Squid is not enabled for mongo fault injection");
            } else {
                LOGGER.info("Throwing mongo connection timeout exception with Dr. Squid");
                throw getDatabaseException(DatabaseType.MONGO);
            }
        }

        Object result = pjp.proceed();
        LOGGER.debug("Returning with mongo result.");
        return result;
    }

    @Around("execution(* org.springframework.jdbc.core.JdbcTemplate.call(..))")
    public Object jdbcCall(ProceedingJoinPoint pjp) throws Throwable {

        LOGGER.debug("Evaluating fault injection for your jdbc call");

        String configName = ConfigUtils.getDrSquidConfigName(environment);
        DrSquidConfig drSquidConfig = getConfig(configName);
        if (drSquidConfig != null && BooleanUtils.isTrue(drSquidConfig.isEnabled()) && CollectionUtils.isNotEmpty(drSquidConfig.getDatabases())) {

            boolean faultInjection = false;
            for (Database db : drSquidConfig.getDatabases()) {
                if (db != null && DatabaseType.SQL.equals(db.getType()) && db.isEnabled()) {
                    faultInjection = true;
                    LoggingLevel loggingLevel = db.getLoggingLevel();
                    if (loggingLevel != null) {
                        FilteredLogger.setLoggingLevel(loggingLevel);
                    }
                }
            }

            if (!faultInjection) {
                LOGGER.debug("Dr. Squid is not enabled for jdbc fault injection");
            } else {
                LOGGER.info("Throwing SQLDataException exception with Dr. Squid");
                throw getDatabaseException(DatabaseType.SQL);
            }
        }

        Object result = pjp.proceed();
        LOGGER.debug("Returning with jdbc result.");
        return result;
    }

    private Throwable getDatabaseException(final DatabaseType databaseType) {
        switch (databaseType) {
            case MONGO:
                return new MongoTimeoutException("Mongo connection timed out");
            case SQL:
                return new SQLDataException();
            default:
                return new Exception();
        }
    }
}
