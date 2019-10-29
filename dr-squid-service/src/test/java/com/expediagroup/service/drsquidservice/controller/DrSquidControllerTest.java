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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class DrSquidControllerTest {

    @Spy
    @InjectMocks
    DrSquidController drSquidController = new DrSquidController();

    @Test
    public void shouldReturnSuccessResponseBody(){
        //arrange
        String response = "successfully mocked success";
        HttpStatus status = HttpStatus.OK;
        //act
        ResponseEntity<String> responseEntity = drSquidController.createResponseEntity(status.value(), response);
        //assert
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(responseEntity.getBody(),response);
        Assert.assertEquals(responseEntity.getStatusCode(),status);
    }

    @Test
    public void shouldReturnFailureStatusCode(){
        //arrange
        String response = "successfully mocked failure";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        //act
        ResponseEntity<String> responseEntity = drSquidController.createResponseEntity(status.value(), response);
        //assert
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(responseEntity.getBody(),response);
        Assert.assertEquals(responseEntity.getStatusCodeValue(),500);
    }


}

