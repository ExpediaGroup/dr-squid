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

import com.expediagroup.drsquidutils.interceptor.DrSquidInterceptor;
import com.expediagroup.drsquidutils.model.Delay;
import com.expediagroup.drsquidutils.model.FixedDelay;
import com.expediagroup.drsquidutils.model.NormalDelay;
import com.expediagroup.drsquidutils.model.RangeDelay;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class DelayDeserializer extends StdDeserializer<Delay> {

    private static final String DELAY_TYPE_FIELD = "type";

    public DelayDeserializer() {
        this(null);
    }

    public DelayDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Delay deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        // Get the JSON node
        JsonNode node = jp.getCodec().readTree(jp);

        // Extract type of delay and construct delay object from fields
        Delay delay;
        String type = node.get(DELAY_TYPE_FIELD).asText();
        switch (type) {
            case DrSquidInterceptor.DELAY_FIXED:
                int value = node.get(DrSquidInterceptor.FIXED_VALUE_IN_MSECS).intValue();
                delay = new FixedDelay(value);
                break;
            case DrSquidInterceptor.DELAY_NORMAL:
                int mean = node.get(DrSquidInterceptor.MEAN_VALUE_IN_MSECS).intValue();
                int stdDev = node.get(DrSquidInterceptor.STD_VALUE_IN_MSECS).intValue();
                delay = new NormalDelay(mean, stdDev);
                break;
            case DrSquidInterceptor.DELAY_RANGE:
                int min = node.get(DrSquidInterceptor.MIN_VALUE_IN_MSECS).intValue();
                int max = node.get(DrSquidInterceptor.MAX_VALUE_IN_MSECS).intValue();
                delay = new RangeDelay(min, max);
                break;
            default:
                delay = null;
                break;
        }

        return delay;
    }
}
