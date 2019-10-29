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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.StringJoiner;

public class ConfigUtils {
    private ConfigUtils() {
        throw new IllegalStateException("Utility class - do not instantiate.");
    }

    private static final FilteredLogger LOGGER = new FilteredLogger(LoggerFactory.getLogger(ConfigUtils.class));

    /**
     * Gets the name of a Dr. Squid config from the environment.
     * @param environment Environment of the call we're intercepting. Used to derive the Dr. Squid profile and client.
     * @return Name of the Dr. Squid config.
     */
    public static String getDrSquidConfigName(Environment environment) {

        // Get the configured Dr. Squid profile.
        String profile = environment.getProperty("drsquid.profile");
        if (StringUtils.isBlank(profile)) {

            LOGGER.warn("No Dr. Squid profile defined, please define \"drsquid.profile\" property in your application.yml.");
            LOGGER.warn("Using active profiles to determine Dr. Squid profile...");

            String[] activeProfiles = environment.getActiveProfiles();
            if (activeProfiles.length == 0) {
                return StringUtils.EMPTY; // No drsquid profile, nor active profile, can't get a drsquid config by name
            }

            // Handling the case of multiple profiles being activated at same time  (properties being split in multiple profiles)
            if (activeProfiles.length > 1) {
                profile = activeProfiles[activeProfiles.length - 1]; // pick the last entry of array, that's the main profile activated
            }
        }

        // Get the Dr. Squid config name for the current client and profile.
        String client = environment.getProperty("info.build.artifact");
        return new StringJoiner(".").add("drsquid").add(client).add(profile).toString();
    }
}
