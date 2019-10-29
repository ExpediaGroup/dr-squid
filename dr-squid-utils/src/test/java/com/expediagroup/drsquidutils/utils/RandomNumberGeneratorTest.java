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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class RandomNumberGeneratorTest {

    @Spy
    private RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();

    @Test
    public void shouldReturnNextInteger() {

        // Arrange
        doReturn(34).when(randomNumberGenerator).nextInt(100);

        // Act
        int randomNumber = randomNumberGenerator.nextInt(100);

        // Assert
        assertEquals(34, randomNumber);
    }

    @Test
    public void shouldReturnNextGaussian() {

        // Arrange
        doReturn(34.5).when(randomNumberGenerator).nextGaussian();

        // Act
        int randomNumber = (int) randomNumberGenerator.nextGaussian();

        // Assert
        assertEquals(34, randomNumber);
    }
}
