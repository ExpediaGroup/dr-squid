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

import com.expediagroup.drsquidutils.interceptor.DrSquidInterceptor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RangeDelay implements Delay {

    private int min;
    private int max;

    @JsonCreator
    public RangeDelay(
            @JsonProperty(required = true, value = DrSquidInterceptor.MIN_VALUE_IN_MSECS) int min,
            @JsonProperty(required = true, value = DrSquidInterceptor.MAX_VALUE_IN_MSECS) int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String getType() {
        return "range";
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
