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
package com.expediagroup.service.drsquidservice.controller;

import com.expediagroup.drsquidutils.configretriever.ConfigRetriever;
import com.expediagroup.drsquidutils.configretriever.FileSystemConfigRetriever;
import com.expediagroup.drsquidutils.model.DrSquidConfig;
import com.expediagroup.service.drsquidservice.Application;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.CLIENT_NAME;
import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.FAILURE;
import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.SUCCESS;
import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.TEST_DOWNSTREAM_PATTERN;
import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.TEST_PROFILE;
import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.TIMEOUT;
import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.TOKEN;
import static com.expediagroup.service.drsquidservice.controller.DrSquidConstants.URL_TEMPLATE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest (classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DrSquidControllerIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Mock
    private Environment environment;

    @Mock
    private DrSquidConfig drSquidConfig;

    @Spy
    @InjectMocks
    private ConfigRetriever fileSystemConfigRetriever = new FileSystemConfigRetriever();

    @Before
    public void setUp() throws IOException{
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        environment = mock(Environment.class);
        drSquidConfig = fileSystemConfigRetriever.getConfig("drsquid.service.test");

        doReturn(drSquidConfig).when(environment).getProperty(anyString(), eq(DrSquidConfig.class));
    }

    private String encodeToken(String clientName, String downstreamPattern, String profile, String status){
        String token = "clientName=" + clientName;
        token += "&downstreamPattern=" + downstreamPattern;
        token += "&profile=" + profile;
        token += "&status=" + status;
        byte[] encodedBytes = Base64.getEncoder().encode(token.getBytes());
        return new String(encodedBytes, Charset.forName("UTF-8"));
    }

    @Test
    @Ignore
    public void shouldReturnSuccessStatusOK() throws Exception {
        String profile = TEST_PROFILE;
        String clientName = CLIENT_NAME;
        String downstreamPattern = TEST_DOWNSTREAM_PATTERN;
        String status = SUCCESS;

        String token = encodeToken(clientName, downstreamPattern, profile, status);

        mockMvc.perform(get(URL_TEMPLATE)
                .param(TOKEN, token)
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    @Test
    @Ignore
    public  void shouldReturnFailure() throws Exception{
        String profile = TEST_PROFILE;
        String clientName = CLIENT_NAME;
        String downstreamPattern = TEST_DOWNSTREAM_PATTERN;
        String status = FAILURE;

        String token = encodeToken(clientName, downstreamPattern, profile, status);

        mockMvc.perform(get(URL_TEMPLATE)
                .param(TOKEN, token)
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().is5xxServerError());

    }

    @Test
    @Ignore
    public  void shouldReturnTimeout() throws Exception{
        String profile = TEST_PROFILE;
        String clientName = CLIENT_NAME;
        String downstreamPattern = TEST_DOWNSTREAM_PATTERN;
        String status = TIMEOUT;

        String token = encodeToken(clientName, downstreamPattern, profile, status);

        mockMvc.perform(get(URL_TEMPLATE)
                .param(TOKEN, token)
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().is5xxServerError());

    }

    @Test
    @Ignore
    public  void shouldReturnGenericErrorStatusCode() throws Exception{
        String profile = TEST_PROFILE;
        String clientName = CLIENT_NAME;
        String downstreamPattern = TEST_DOWNSTREAM_PATTERN;
        String status = "null";

        String token = encodeToken(clientName, downstreamPattern, profile, status);

        mockMvc.perform(get(URL_TEMPLATE)
                .param(TOKEN, token)
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isIAmATeapot());

    }
}