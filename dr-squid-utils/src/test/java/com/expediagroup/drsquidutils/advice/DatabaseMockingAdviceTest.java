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
import com.mongodb.MongoTimeoutException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.sql.SQLDataException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseMockingAdviceTest {
    @InjectMocks
    private DatabaseMockingAdvice databaseMockingAdvice;

    @Mock
    private Environment environment;

    @Mock
    private FileSystemConfigRetriever fileSystemConfigRetriever;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Before
    public void setUp() {
        when(environment.getProperty(eq("drsquid.profile"))).thenReturn("test");
        when(environment.getProperty(eq("info.build.artifact"))).thenReturn("fake-service");
    }

    @Test(expected = MongoTimeoutException.class)
    public void mockMongoTimeout() throws Throwable {

        Database database = new Database(DatabaseType.MONGO, null, true, LoggingLevel.NONE);
        List<Database> databases = Collections.singletonList(database);
        DrSquidConfig drSquidConfig = new DrSquidConfig(true, LoggingLevel.NONE, null, databases);

        when(fileSystemConfigRetriever.getConfig(eq("drsquid.fake-service.test"))).thenReturn(drSquidConfig);

        databaseMockingAdvice.mongoCall(proceedingJoinPoint);
    }

    @Test(expected = SQLDataException.class)
    public void mockStoredProcedureTimeout() throws Throwable {

        Database database = new Database(DatabaseType.SQL, null, true, LoggingLevel.NONE);
        List<Database> databases = Collections.singletonList(database);
        DrSquidConfig drSquidConfig = new DrSquidConfig(true, LoggingLevel.NONE, null, databases);

        when(fileSystemConfigRetriever.getConfig(eq("drsquid.fake-service.test"))).thenReturn(drSquidConfig);

        databaseMockingAdvice.jdbcCall(proceedingJoinPoint);
    }
}
