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
package com.expediagroup.drsquidutils.configretriever;

import com.expediagroup.drsquidutils.model.DrSquidConfig;
import com.expediagroup.drsquidutils.utils.FilteredLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * This is the file system based implementation of ConfigDriver.
 */
@Component
public class FileSystemConfigRetriever implements ConfigRetriever {

    private static final FilteredLogger LOGGER = new FilteredLogger(LoggerFactory.getLogger(FileSystemConfigRetriever.class));

    @Override
    public DrSquidConfig getConfig(String name) {

        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        if (stream == null) {
            LOGGER.error("The resource could not be found.");
            return null;
        }

        DrSquidConfig drSquidConfig;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(stream, writer, Charset.defaultCharset());
            String fileString = writer.toString();
            drSquidConfig = mapper.readValue(fileString, DrSquidConfig.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        return drSquidConfig;
    }
}
