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

import com.expediagroup.drsquidutils.configretriever.FileSystemConfigRetriever;
import com.expediagroup.drsquidutils.delaydriver.DelayDriver;
import com.expediagroup.drsquidutils.delaydriver.FixedDelayDriver;
import com.expediagroup.drsquidutils.delaydriver.NormalDelayDriver;
import com.expediagroup.drsquidutils.delaydriver.RangeDelayDriver;
import com.expediagroup.drsquidutils.model.Behavior;
import com.expediagroup.drsquidutils.model.Delay;
import com.expediagroup.drsquidutils.model.DrSquidConfig;
import com.expediagroup.drsquidutils.model.Failure;
import com.expediagroup.drsquidutils.model.Service;
import com.expediagroup.drsquidutils.model.Success;
import com.expediagroup.drsquidutils.model.Timeout;
import io.swagger.annotations.Api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Base64;


/**
 * DrSquid Controller mocks all downstreams for performance testing lets users receive performance metrics
 * that accurately measure how their services perform if the downstreams behaved perfectly.
 * This will help in checking resiliency. With this backend users can mock certain percentages of our downstream calls as failures or timeouts.
 */
@Controller
@RequestMapping(value="/v1", produces= MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
@Api(value = "DrSquid Service")
public class DrSquidController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrSquidController.class);
    protected static final String SUCCESS_FIELD = "success";
    protected static final String TIMEOUT_FIELD = "timeout";
    protected static final String FAILURE_FIELD = "failure";
    protected static final String DELAY_FIXED = "fixed";
    protected static final String DELAY_RANGE = "range";
    protected static final String DELAY_NORMAL = "normal";
    protected static final String CLIENT_NAME = "clientName";
    protected static final String DOWNSTREAM_PATTERN = "downstreamPattern";
    protected static final String PROFILE = "profile";
    protected static final String STATUS = "status";

    private static final String SENDING_BACK_ERROR = " - Responding with 418 status code...";
    private static final int ERROR_RESPONSE_CODE = 418;

    @Autowired
    private RangeDelayDriver rangeDelayDriver;

    @Autowired
    private NormalDelayDriver normalDelayDriver;

    @Autowired
    private FixedDelayDriver fixedDelayDriver;

    @Autowired
    private FileSystemConfigRetriever fileSystemConfigRetriever;

    protected ResponseEntity<String> createResponseEntity(int statusCode, String body) {
        return ResponseEntity.status(HttpStatus.valueOf(statusCode)).contentType(MediaType.TEXT_PLAIN).body(body);
    }

    /**
     * Decodes the request token into params
     * @param token token sent to dr-squid-service
     * @return map of query param name to query param value
     */
    private Map<String, String> decodeToken(String token) {

        Map<String, String> paramMap = new HashMap<>();
        byte[] decodedBytes = Base64.getDecoder().decode(token.getBytes());
        String decodedString = new String(decodedBytes, Charset.forName("UTF-8"));
        String[] params = decodedString.split("&");
        for (String param : params) {
            String[] paramKVP = param.split("=");
            if (paramKVP.length != 2) {
                return null;
            }
            paramMap.put(paramKVP[0], paramKVP[1]);
        }

        return paramMap;
    }

    /**
     * Gets the time to sleep given a delay configuration.
     * @param delay delay configuration
     * @return time to sleep
     */
    private int getTimeToSleep(Delay delay) {

        String type = delay.getType();
        DelayDriver delayDriver;
        switch (type) {
            case DELAY_FIXED:
                delayDriver = fixedDelayDriver;
                break;
            case DELAY_RANGE:
                delayDriver = rangeDelayDriver;
                break;
            case DELAY_NORMAL:
                delayDriver = normalDelayDriver;
                break;
            default:
                LOGGER.warn("Configuration not setup properly. Unsupported delay type \"{}\" - Setting time to sleep to 0...", type);
                return 0;
        }

        return delayDriver.getDelayTime(delay);
    }

    private ResponseEntity<String> createSuccessResponse(Success success) {

        // Check spoofed
        if (!success.isSpoofed()) {
            String errorMessage = "Success is spoofed, and yet dr-squid-service was still called";
            LOGGER.error(errorMessage + SENDING_BACK_ERROR);
            return createResponseEntity(ERROR_RESPONSE_CODE, errorMessage);
        }

        // Get mock status code and response body
        int mockStatusCode = success.getMockStatusCode();
        String mockReponseBody = success.getMockResponseBody();
        if (mockReponseBody == null) {
            LOGGER.warn("No mock response body in configuration, setting to empty body...");
            mockReponseBody = "";
        }

        int timeToSleep = getTimeToSleep(success.getDelay());

        LOGGER.info("Mocking success scenario waiting {} ms then responding with {} status code with body: \"{}\"", new Object[]{timeToSleep, mockStatusCode, mockReponseBody});

        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException during sleep" + SENDING_BACK_ERROR, e);
            return createResponseEntity(ERROR_RESPONSE_CODE, e.getMessage());
        }

        return createResponseEntity(mockStatusCode, mockReponseBody);
    }

    private ResponseEntity<String> createFailureResponse(Failure failure) {

        // Get mock status code and response body
        int mockStatusCode = failure.getMockStatusCode();
        String mockReponseBody = failure.getMockResponseBody();
        if (mockReponseBody == null) {
            LOGGER.warn("No mock response body in configuration, setting to empty body...");
            mockReponseBody = "";
        }

        int timeToSleep = getTimeToSleep(failure.getDelay());

        LOGGER.info("Mocking failure scenario waiting {} ms then responding with {} status code with body: \"{}\"", new Object[]{timeToSleep, mockStatusCode, mockReponseBody});

        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException during sleep" + SENDING_BACK_ERROR, e);
            return createResponseEntity(ERROR_RESPONSE_CODE, e.getMessage());
        }

        return createResponseEntity(mockStatusCode, mockReponseBody);
    }

    private ResponseEntity<String> createTimeoutResponse(Timeout timeout) {

        int timeToSleep = getTimeToSleep(timeout.getDelay());

        LOGGER.info("Mocking timeout scenario waiting {} ms then responding", timeToSleep);

        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException during sleep" + SENDING_BACK_ERROR, e);
            return createResponseEntity(ERROR_RESPONSE_CODE, e.getMessage());
        }

        return createResponseEntity(500, "");
    }

    @ResponseBody
    @RequestMapping(value="/mock", method= RequestMethod.GET)
    public ResponseEntity<String> mockDownstreamGet(@RequestParam("token") String token) throws InterruptedException {

        try {

            // Get params from request
            Map<String, String> paramMap = decodeToken(token);
            if (paramMap == null) {
                String message = "Bad token!";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }
            String clientName = paramMap.get(CLIENT_NAME);
            String downstreamPattern = paramMap.get(DOWNSTREAM_PATTERN);
            String profile = paramMap.get(PROFILE);
            String status = paramMap.get(STATUS);
            if (clientName == null || downstreamPattern == null || profile == null || status == null) {
                String message = "Bad token!";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

            // Get the Dr. Squid configuration for the client and profile
            String configName = new StringJoiner(".").add("drsquid").add(clientName).add(profile).toString();

            // Check configuration exists
            DrSquidConfig drSquidConfig = fileSystemConfigRetriever.getConfig(configName);
            if (drSquidConfig == null) {
                String message = "DrSquid configuration \"" + configName + "\" is missing";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

            // Check Dr. Squid is enabled
            if (!drSquidConfig.isEnabled()) {
                String message = "DrSquid is not enabled for client name \"" + clientName + "\" in profile \"" + profile + "\"";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

            // Check services exist
            List<Service> services = drSquidConfig.getServices();
            if (services == null) {
                String message = "No services defined in configuration \"" + configName + "\"";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

            // Get target service
            Service targetService = null;
            for (Service service : services) {
                if (service.getUrlPattern().equals(downstreamPattern)) {
                    targetService = service;
                    break;
                }
            }

            // Check target service exists
            if (targetService == null) {
                String message = "No service in configuration \"" + configName + "\" has the requested pattern \"" + downstreamPattern + "\"";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

            // Check target service is enabled
            String targetServiceName = targetService.getName();
            if (!targetService.isEnabled()) {
                String message = "Target service \"" + targetServiceName + "\" not enabled in configuration \"" + configName + "\"";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

            // Check target service's behavior
            Behavior behavior = targetService.getBehavior();
            if (behavior == null) {
                String message = "No behavior defined for service \"" + targetServiceName + "\" in configuration \"" + configName + "\"";
                LOGGER.error(message + SENDING_BACK_ERROR);
                return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

            LOGGER.info("Emulating {} scenario for service \"{}\" in configuration \"{}\"", new Object[]{status, targetServiceName, configName});

            switch (status) {
                case SUCCESS_FIELD:
                    return createSuccessResponse(behavior.getSuccess());
                case FAILURE_FIELD:
                    return createFailureResponse(behavior.getFailure());
                case TIMEOUT_FIELD:
                    return createTimeoutResponse(behavior.getTimeout());
                default:
                    String message = "Unsupported status type \"" + status + "\" for service \"" + targetServiceName + "\" in configuration \"" + configName + "\"";
                    LOGGER.error(message + SENDING_BACK_ERROR);
                    return createResponseEntity(ERROR_RESPONSE_CODE, message);
            }

        } catch (Exception e) {

            // Unknown issue with configuration
            String message = "Unknown issue with configuration of Dr. Squid";
            LOGGER.error(message + SENDING_BACK_ERROR, e);
            return createResponseEntity(ERROR_RESPONSE_CODE, message);
        }
    }

    @ResponseBody
    @RequestMapping(value="/mock", method= RequestMethod.POST)
    public ResponseEntity<String> mockDownstreamPost(@RequestParam("token") String token) throws InterruptedException {
        return mockDownstreamGet(token);
    }

}