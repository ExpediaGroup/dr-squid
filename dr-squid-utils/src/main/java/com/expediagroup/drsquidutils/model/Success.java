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
public class Success {

    private int percentage;
    private boolean spoofed;
    private String mockResponseBody;
    private int mockStatusCode;
    private Delay delay;

    @JsonCreator
    public Success(
            @JsonProperty(required = true, value = "percentage") int percentage,
            @JsonProperty(required = true, value = "spoofed") boolean spoofed,
            @JsonProperty(value = "mock_response_body", defaultValue = "spoofing success with drSquid") String mockResponseBody,
            @JsonProperty(required = true, value = "mock_status_code") int mockStatusCode,
            @JsonProperty(required = true, value = "delay") Delay delay) {
        this.percentage = percentage;
        this.spoofed = spoofed;
        this.mockResponseBody = mockResponseBody;
        this.mockStatusCode = mockStatusCode;
        this.delay = delay;
    }

    public int getPercentage() {
        return percentage;
    }

    public boolean isSpoofed() {
        return spoofed;
    }

    public String getMockResponseBody() {
        return mockResponseBody;
    }

    public int getMockStatusCode() {
        return mockStatusCode;
    }

    public Delay getDelay() {
        return delay;
    }
}
