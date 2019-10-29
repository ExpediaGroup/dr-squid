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
package com.expediagroup.drsquidutils.delaydriver;

import com.expediagroup.drsquidutils.model.FixedDelay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FixedDelayDriverTest {

    @Mock
    private FixedDelay fixedDelay;

    @Test
    public void shouldSleepFixedDelay() {

        //Arrange
        DelayDriver delayDriver = new FixedDelayDriver();
        when(fixedDelay.getValue()).thenReturn(2000);

        //Act
        int timeToSleep = delayDriver.getDelayTime(fixedDelay);

        //Assert
        assertEquals(2000, timeToSleep);
    }

    @Test
    public void shouldNotSleepFixedDelayNegativeFixedValue() {

        //Arrange
        DelayDriver delayDriver = new FixedDelayDriver();
        when(fixedDelay.getValue()).thenReturn(-2000);

        //Act
        int timeToSleep = delayDriver.getDelayTime(fixedDelay);

        //Assert
        assertEquals(0, timeToSleep);
    }
}
