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
import org.springframework.http.HttpMethod;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Service {

    private String name;
    private String description;
    private String urlPattern;
    private HttpMethod method;
    private String bodyKeyword;
    private boolean enabled;
    private LoggingLevel loggingLevel;
    private Behavior behavior;

    @JsonCreator
    public Service(
            @JsonProperty(required = true, value = "name") String name,
            @JsonProperty(required = false, value = "description") String description,
            @JsonProperty(required = true, value = "pattern") String urlPattern,
            @JsonProperty(required = false, value = "method") HttpMethod method,
            @JsonProperty(required = false, value = "body_keyword") String bodyKeyword,
            @JsonProperty(required = true, value = "enabled") Boolean enabled,
            @JsonProperty(required = false, value = "logging_level") LoggingLevel loggingLevel,
            @JsonProperty(required = true, value = "behavior") Behavior behavior) {
        this.name = name;
        this.description = description;
        this.urlPattern = urlPattern;
        this.method = method;
        this.bodyKeyword = bodyKeyword;
        this.enabled = enabled;
        this.loggingLevel = loggingLevel;
        this.behavior = behavior;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getBodyKeyword() {
        return bodyKeyword;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LoggingLevel getLoggingLevel() {
        return loggingLevel;
    }

    public Behavior getBehavior() {
        return behavior;
    }
}
