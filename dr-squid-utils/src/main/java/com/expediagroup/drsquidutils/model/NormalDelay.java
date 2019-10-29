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
public class NormalDelay implements Delay {

    private int mean;
    private int stdDev;

    @JsonCreator
    public NormalDelay(
            @JsonProperty(required = true, value = DrSquidInterceptor.MEAN_VALUE_IN_MSECS) int mean,
            @JsonProperty(required = true, value = DrSquidInterceptor.STD_VALUE_IN_MSECS) int stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }

    @Override
    public String getType() {
        return "normal";
    }

    public int getMean() {
        return mean;
    }

    public int getStdDev() {
        return stdDev;
    }
}
