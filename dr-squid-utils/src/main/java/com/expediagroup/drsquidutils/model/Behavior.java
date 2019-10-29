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
public class Behavior {

    private Success success;
    private Failure failure;
    private Timeout timeout;

    @JsonCreator
    public Behavior(
            @JsonProperty(required = false, value = "success") Success success,
            @JsonProperty(required = false, value = "failure") Failure failure,
            @JsonProperty(required = false, value = "timeout") Timeout timeout) {
        this.success = success;
        this.failure = failure;
        this.timeout = timeout;
    }

    public Success getSuccess() {
        return success;
    }

    public Failure getFailure() {
        return failure;
    }

    public Timeout getTimeout() {
        return timeout;
    }
}
