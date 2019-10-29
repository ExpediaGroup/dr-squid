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

import com.expediagroup.drsquidutils.model.Delay;
import com.expediagroup.drsquidutils.model.NormalDelay;
import com.expediagroup.drsquidutils.utils.FilteredLogger;
import com.expediagroup.drsquidutils.utils.RandomNumberGenerator;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the normal delay type implementation of Dr Squid DelayDriver.
 */

@Component
public class NormalDelayDriver implements DelayDriver {

    private static final FilteredLogger LOGGER = new FilteredLogger(LoggerFactory.getLogger(NormalDelayDriver.class));

    @Autowired
    RandomNumberGenerator random;

    @Override
    public int getDelayTime(Delay delay) {

        if (!(delay instanceof NormalDelay)) {
            return 0;
        }

        NormalDelay normalDelay = (NormalDelay) delay;

        int mean = normalDelay.getMean();
        if (mean < 0) {
            LOGGER.error("Mean is negative for normal delay. Setting time to sleep to 0.");
            return 0;
        }

        int stdDev = normalDelay.getStdDev();
        if (stdDev < 0) {
            LOGGER.error("Standard deviation is negative for normal delay. Setting time to sleep to 0.");
            return 0;
        }

        return (int) (random.nextGaussian() * stdDev + mean);
    }
}
