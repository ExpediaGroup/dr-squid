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

import com.expediagroup.drsquidutils.model.NormalDelay;
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
public class NormalDelayDriverTest {

    @Spy
    @InjectMocks
    private DelayDriver delayDriver = new NormalDelayDriver();

    @Mock
    private NormalDelay normalDelay;

    @Mock
    private RandomNumberGenerator random;

    @Test
    public void shouldSleepNormalDelay() {

        //Arrange
        when(normalDelay.getMean()).thenReturn(3000);
        when(normalDelay.getStdDev()).thenReturn(1500);
        when(random.nextGaussian()).thenReturn(0.68);

        //Act
        int timeToSleep = delayDriver.getDelayTime(normalDelay);

        //Assert
        assertEquals(4020, timeToSleep);
    }


    @Test
    public void shouldNotSleepNormalDelayNegativeMeanValue() {

        //Arrange
        when(normalDelay.getMean()).thenReturn(-3000);

        //Act
        int timeToSleep = delayDriver.getDelayTime(normalDelay);

        //Assert
        assertEquals(0, timeToSleep);
    }

    @Test
    public void shouldNotSleepNormalDelayNegativeStdValue() {

        //Arrange
        when(normalDelay.getStdDev()).thenReturn(-1000);

        //Act
        int timeToSleep = delayDriver.getDelayTime(normalDelay);

        //Assert
        assertEquals(0, timeToSleep);
    }
}
