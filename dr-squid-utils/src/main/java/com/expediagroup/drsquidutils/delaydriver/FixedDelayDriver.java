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
import com.expediagroup.drsquidutils.model.FixedDelay;
import com.expediagroup.drsquidutils.utils.FilteredLogger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This is the fixed delay type implementation of Dr Squid DelayDriver.
 */

@Component
public class FixedDelayDriver implements DelayDriver {

    private static final FilteredLogger LOGGER = new FilteredLogger(LoggerFactory.getLogger(FixedDelayDriver.class));

    @Override
    public int getDelayTime(Delay delay) {

        if (!(delay instanceof FixedDelay)) {
            return 0;
        }

        FixedDelay fixedDelay = (FixedDelay) delay;

        int timeToSleep = fixedDelay.getValue();
        if (timeToSleep < 0) {
            LOGGER.error("Value is negative for fixed delay. Setting time to sleep to 0.");
            return 0;
        }

        return timeToSleep;
    }
}
