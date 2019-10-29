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

/**
 * This is an interface for getting the DrSquidConfig from the config name. Clients can create their own implementations
 * for different config hosts.
 */
public interface ConfigRetriever {
    DrSquidConfig getConfig(String name);
}
