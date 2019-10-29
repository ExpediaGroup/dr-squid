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

import com.expediagroup.drsquidutils.model.RangeDelay;
import com.expediagroup.drsquidutils.utils.RandomNumberGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RangeDelayDriverTest {

    @Spy
    @InjectMocks
    private DelayDriver delayDriver = new RangeDelayDriver();

    @Mock
    private RangeDelay rangeDelay;

    @Mock
    private RandomNumberGenerator random;

    @Test
    public void shouldSleepRangeDelay() {

        //Arrange
        when(rangeDelay.getMin()).thenReturn(2000);
        when(rangeDelay.getMax()).thenReturn(4000);
        when(random.nextInt((4000 - 2000) + 1)).thenReturn(1500);

        //Act
        int timeToSleep = delayDriver.getDelayTime(rangeDelay);

        //Assert
        assertEquals(3500, timeToSleep);
    }

    @Test
    public void shouldNotSleepRangeDelayNegativeMinValue() {

        //Arrange
        when(rangeDelay.getMin()).thenReturn(-2000);

        //Act
        int timeToSleep = delayDriver.getDelayTime(rangeDelay);

        //Assert
        assertEquals(0, timeToSleep);
    }

    @Test
    public void shouldNotSleepRangeDelayNegativeMaxValue() {

        //Arrange
        when(rangeDelay.getMin()).thenReturn(2000);
        when(rangeDelay.getMax()).thenReturn(-4000);

        //Act
        int timeToSleep = delayDriver.getDelayTime(rangeDelay);

        //Assert
        assertEquals(0, timeToSleep);
    }

    @Test
    public void shouldNotSleepRangeDelayMinGreaterThanMax() {

        //Arrange
        when(rangeDelay.getMin()).thenReturn(4000);
        when(rangeDelay.getMax()).thenReturn(2000);

        //Act
        int timeToSleep = delayDriver.getDelayTime(rangeDelay);

        //Assert
        assertEquals(0, timeToSleep);
    }

}
