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
import com.expediagroup.drsquidutils.model.Delay;
import com.expediagroup.drsquidutils.utils.FilteredLogger;
import com.expediagroup.drsquidutils.utils.RandomNumberGenerator;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the range delay type implementation of Dr Squid DelayDriver.
 */

@Component
public class RangeDelayDriver implements DelayDriver {

    private static final FilteredLogger LOGGER = new FilteredLogger(LoggerFactory.getLogger(RangeDelayDriver.class));

    @Autowired
    RandomNumberGenerator random;

    @Override
    public int getDelayTime(Delay delay) {

        if (!(delay instanceof RangeDelay)) {
            return 0;
        }

        RangeDelay rangeDelay = (RangeDelay) delay;

        int min = rangeDelay.getMin();
        if (min < 0) {
            LOGGER.error("Minimum is negative for normal delay. Setting time to sleep to 0.");
            return 0;
        }

        int max = rangeDelay.getMax();
        if (max < 0) {
            LOGGER.error("Maximum is negative for normal delay. Setting time to sleep to 0.");
            return 0;
        }

        if (min > max) {
            LOGGER.error("Minimum is greater than maximum for range delay. Setting time to sleep to 0.");
            return 0;
        }

        return random.nextInt((max - min) + 1) + min;
    }
}
