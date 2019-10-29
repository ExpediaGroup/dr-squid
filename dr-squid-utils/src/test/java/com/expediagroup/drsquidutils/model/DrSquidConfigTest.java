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
package com.expediagroup.drsquidutils.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DrSquidConfigTest {

    private void assertDrSquidConfig(DrSquidConfig drSquidConfig, boolean expectedEnabled) {
        assertEquals(expectedEnabled, drSquidConfig.isEnabled());
    }

    private void assertList(List<?> list, int expectedSize) {
        assertNotNull(list);
        assertEquals(expectedSize, list.size());
    }

    private void assertService(Service service, boolean expectedEnabled, String expectedName, String expectedDescription, String expectedPattern) {
        assertNotNull(service);
        assertEquals(expectedEnabled, service.isEnabled());
        assertNotNull(service.getName());
        assertEquals(expectedName, service.getName());
        assertNotNull(service.getDescription());
        assertEquals(expectedDescription, service.getDescription());
        assertNotNull(service.getUrlPattern());
        assertEquals(expectedPattern, service.getUrlPattern());
    }

    private void assertBehavior(Behavior behavior) {
        assertNotNull(behavior);
    }

    private void assertSuccess(Success success, boolean expectedSpoofed, int expectedPercentage, int expectedStatusCode, String expectedBody) {
        assertNotNull(success);
        assertEquals(expectedPercentage, success.getPercentage());
        assertEquals(expectedSpoofed, success.isSpoofed());
        assertEquals(expectedStatusCode, success.getMockStatusCode());
        assertNotNull(success.getMockResponseBody());
        assertEquals(expectedBody, success.getMockResponseBody());
    }

    private void assertFailure(Failure failure, int expectedPercentage, int expectedStatusCode, String expectedBody) {
        assertNotNull(failure);
        assertEquals(expectedPercentage, failure.getPercentage());
        assertEquals(expectedStatusCode, failure.getMockStatusCode());
        assertNotNull(failure.getMockResponseBody());
        assertEquals(expectedBody, failure.getMockResponseBody());
    }

    private void assertTimeout(Timeout timeout, int expectedPercentage) {
        assertNotNull(timeout);
        assertEquals(expectedPercentage, timeout.getPercentage());
    }

    private void assertFixedDelay(Delay delay, int expectedValue) {
        assertNotNull(delay);
        assertTrue(delay instanceof FixedDelay);
        FixedDelay fixedDelay = (FixedDelay) delay;
        assertNotNull(fixedDelay.getType());
        assertEquals("fixed", fixedDelay.getType());
        assertEquals(expectedValue, fixedDelay.getValue());
    }

    private void assertNormalDelay(Delay delay, int expectedMean, int expectedStdDev) {
        assertNotNull(delay);
        assertTrue(delay instanceof NormalDelay);
        NormalDelay normalDelay = (NormalDelay) delay;
        assertNotNull(normalDelay.getType());
        assertEquals("normal", normalDelay.getType());
        assertEquals(expectedMean, normalDelay.getMean());
        assertEquals(expectedStdDev, normalDelay.getStdDev());
    }

    private void assertRangeDelay(Delay delay, int expectedMin, int expectedMax) {
        assertNotNull(delay);
        assertTrue(delay instanceof RangeDelay);
        RangeDelay rangeDelay = (RangeDelay) delay;
        assertNotNull(rangeDelay.getType());
        assertEquals("range", rangeDelay.getType());
        assertEquals(expectedMin, rangeDelay.getMin());
        assertEquals(expectedMax, rangeDelay.getMax());
    }

    private void assertDatabase(Database database, boolean expectedEnabled, String expectedName, String expectedDescription) {
        assertNotNull(database);
        assertEquals(expectedEnabled, database.isEnabled());
        assertNotNull(database.getType());
        assertEquals(expectedName, database.getType());
        assertNotNull(database.getDescription());
        assertEquals(expectedDescription, database.getDescription());
    }

    @Test
    public void shouldDeserialize() throws Exception {

        // Arrange
        // Get JSON of config.
        URL url = this.getClass().getResource("/fullConfig.yaml");
        String json = Resources.toString(url, Charsets.UTF_8);

        // Act
        // Deserialize to DrSquidConfig object
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        DrSquidConfig drSquidConfig = mapper.readValue(json, DrSquidConfig.class);

        // Assert
        // DrSquidConfig
        assertDrSquidConfig(drSquidConfig, true);

        // Services List
        List<Service> services = drSquidConfig.getServices();
        assertList(services, 2);

        // TestService
        Service service = services.get(0);
        Behavior behavior = service.getBehavior();
        assertService(service, true, "TestService", "TestService does things.", "http://www.testservice.com/*");
        assertBehavior(behavior);

        // TestService Success
        Success success = behavior.getSuccess();
        assertSuccess(success, true, 70, 200, "Success Body");
        assertNormalDelay(success.getDelay(), 1000, 100);

        // TestService Failure
        Failure failure = behavior.getFailure();
        assertFailure(failure, 20, 500, "Failure Body");
        assertRangeDelay(failure.getDelay(), 1000, 2000);

        // TestService Timeout
        Timeout timeout = behavior.getTimeout();
        assertTimeout(timeout, 10);
        assertFixedDelay(timeout.getDelay(), 5000);

        // HelperService
        service = services.get(1);
        behavior = service.getBehavior();
        assertService(service, false, "HelperService", "HelperService does other things.", "*helperservice*");
        assertBehavior(behavior);

        // HelperService Success
        success = behavior.getSuccess();
        assertSuccess(success, true, 100, 200, "Success Body");
        assertFixedDelay(success.getDelay(), 100);

    }

}
