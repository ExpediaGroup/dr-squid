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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Database {

    private DatabaseType type;
    private String description;
    private boolean enabled;
    private LoggingLevel loggingLevel;

    @JsonCreator
    public Database(
            @JsonProperty(required = true, value = "type") DatabaseType type,
            @JsonProperty(required = false, value = "description") String description,
            @JsonProperty(required = true, value = "enabled") Boolean enabled,
            @JsonProperty(required = false, value = "logging_level") LoggingLevel loggingLevel) {
        this.type = type;
        this.description = description;
        this.enabled = enabled;
        this.loggingLevel = loggingLevel;
    }

    public DatabaseType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LoggingLevel getLoggingLevel() {
        return loggingLevel;
    }
}
