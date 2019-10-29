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
package com.expediagroup.drsquidutils.interceptor;

import com.expediagroup.drsquidutils.configretriever.FileSystemConfigRetriever;
import com.expediagroup.drsquidutils.delaydriver.DelayDriver;
import com.expediagroup.drsquidutils.delaydriver.FixedDelayDriver;
import com.expediagroup.drsquidutils.delaydriver.NormalDelayDriver;
import com.expediagroup.drsquidutils.delaydriver.RangeDelayDriver;
import com.expediagroup.drsquidutils.model.Behavior;
import com.expediagroup.drsquidutils.model.Delay;
import com.expediagroup.drsquidutils.model.DrSquidConfig;
import com.expediagroup.drsquidutils.model.Failure;
import com.expediagroup.drsquidutils.model.LoggingLevel;
import com.expediagroup.drsquidutils.model.Service;
import com.expediagroup.drsquidutils.model.Success;
import com.expediagroup.drsquidutils.model.Timeout;
import com.expediagroup.drsquidutils.utils.ConfigUtils;
import com.expediagroup.drsquidutils.utils.FilteredLogger;
import com.expediagroup.drsquidutils.utils.RandomNumberGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * HTTP request interceptor that reads a configuration and determines whether the request should be forwarded to dr-squid-service.
 */
@Component
public class DrSquidInterceptor implements ClientHttpRequestInterceptor {

    private static final FilteredLogger LOGGER = new FilteredLogger(LoggerFactory.getLogger(DrSquidInterceptor.class));
    private static final Integer SUCCESS_BUCKET_INDEX = 0;
    private static final Integer FAILURE_BUCKET_INDEX = 1;
    public static final String DELAY_FIXED = "fixed";
    public static final String DELAY_RANGE = "range";
    public static final String DELAY_NORMAL = "normal";
    public static final String FIXED_VALUE_IN_MSECS = "fixed_value_in_msecs";
    public static final String MEAN_VALUE_IN_MSECS = "mean_value_in_msecs";
    public static final String STD_VALUE_IN_MSECS = "std_value_in_msecs";
    public static final String MIN_VALUE_IN_MSECS = "min_value_in_msecs";
    public static final String MAX_VALUE_IN_MSECS = "max_value_in_msecs";

    @Autowired
    private Environment environment;

    @Autowired
    private RandomNumberGenerator random;

    @Autowired
    private FixedDelayDriver fixedDelayDriver;

    @Autowired
    private RangeDelayDriver rangeDelayDriver;

    @Autowired
    private NormalDelayDriver normalDelayDriver;

    @Autowired
    private FileSystemConfigRetriever configRetriever;

    /**
     * Produces an array of upper bucket boundaries given an array of percentages. This aids in bucketing logic.
     * @param percentages array of percentages, should all be positive and sum to 100
     * @return array of bucket boundaries if percentages are valid (none are negative and they sum to 100), otherwise null
     */
    protected int[] getBuckets(int[] percentages) {

        int[] buckets = new int[percentages.length];

        int sumPercentage = 0;
        for (int i = 0; i < percentages.length; i++) {
            if (percentages[i] < 0) {
                LOGGER.error("No negative percentages allowed. {} percentage found in bucket {}.", Integer.toString(percentages[i]), i);
                return new int[0];
            }
            sumPercentage += percentages[i];
            buckets[i] = sumPercentage;
        }

        if (sumPercentage != 100) {
            LOGGER.error("Sum of success, failure and timeout percentages should be 100. Current sum is {}.", Integer.toString(sumPercentage));
            return new int[0];
        }

        return buckets;
    }

    /**
     * Produces a status given an array of upper bucket boundaries.
     * @param buckets array of upper bucket boundaries
     * @return string status, either "success", "failure", or "timeout"
     */
    protected String getStatus(int[] buckets) {

        if (buckets.length == 0) {
            return null;
        }

        String status;
        int randomNumber = (random.nextInt(100) + 1);
        if (randomNumber <= buckets[SUCCESS_BUCKET_INDEX]) {
            status = "success";
        } else if (randomNumber > buckets[SUCCESS_BUCKET_INDEX] && randomNumber <= buckets[FAILURE_BUCKET_INDEX]) {
            status = "failure";
        } else {
            status = "timeout";
        }

        return status;
    }

    /**
     * Produces a query string from set parameters.
     * @param clientName name of the client using Dr. Squid
     * @param downstreamPattern pattern that the downstream matched
     * @param profile Dr. Squid profile
     * @param status status to mock
     * @return query string for the parameters
     */
    protected String getQueryString(String clientName, String downstreamPattern, String profile, String status) {

        return new StringJoiner("&")
                .add("clientName=" + clientName)
                .add("downstreamPattern=" + downstreamPattern)
                .add("profile=" + profile)
                .add("status=" + status)
                .toString();
    }

    /**
     * Produces an encoded token for the given query string
     * @param queryString query string to encode
     * @return encoded token
     */
    protected String getToken(String queryString) {
        byte[] encodedBytes = Base64.getEncoder().encode(queryString.getBytes());
        return new String(encodedBytes, Charset.forName("UTF-8"));
    }

    /**
     * Creates a request to Dr. Squid Service
     * @param originalRequest the original intercepted request
     * @param drSquidMockEndpoint the url of Dr. Squid Service mock endpoint
     * @param token encoded token
     * @return request to Dr. Squid. Service
     * @throws IOException from being unable to create URL
     */
    protected HttpRequest createDrSquidRequest(HttpRequest originalRequest, String drSquidMockEndpoint, String token) throws IOException {

        drSquidMockEndpoint += "?token=" + token;

        URI uri;
        try {
            uri = new URI(drSquidMockEndpoint);
        } catch (URISyntaxException e) {
            return originalRequest;
        }

        return new SimpleClientHttpRequestFactory().createRequest(uri, originalRequest.getMethod());
    }

    /**
     * Matches a downstream URL to a configured service
     * @param services list of all services
     * @param downstreamUrl downstream URL to match
     * @return enabled service which matched the URL, otherwise null
     */
    protected Service matchDownstream(List<Service> services, String downstreamUrl) {

        for (Service service : services) {
            if (service == null) {
                continue;
            }
            String downstreamPattern = service.getUrlPattern();
            downstreamPattern = downstreamPattern.replaceAll("\\*", "(.*)");
            if (Pattern.compile(downstreamPattern).matcher(downstreamUrl).find() && service.isEnabled()) {
                return service;
            }
        }
        return null;
    }

    /**
     * Gets the time to sleep given a delay config.
     * @param delay delay config
     * @return time to sleep
     */
    protected int getTimeToSleep(Delay delay) {

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
                LOGGER.error("Config not setup properly. Unsupported delay type \"{}\".", type);
                return 0;
        }

        return delayDriver.getDelayTime(delay);
    }

    /**
     * The Dr. Squid Interceptor. Intercepts outgoing requests and either does nothing, spoofs the request
     * using Dr. Squid Service, or waits before sending the original request - all dependent on the behavior
     * configured in the Dr. Squid Config.
     * @param interceptedRequest request being intercepted
     * @param interceptedRequestBody body of the request being intercepted
     * @param execution context of HTTP request execution, used to execute the returned response
     * @return the response from the executed request
     * @throws IOException IOException is wrapped around any exception thrown to match inheriting signature.
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest interceptedRequest, byte[] interceptedRequestBody, ClientHttpRequestExecution execution) throws IOException {

        try {

            final String executingInterceptedRequest = "Executing originally intercepted request...";

            // Get the downstream URL of the intercepted call
            String downstreamUrl = interceptedRequest.getURI().toURL().toString();

            // Get Dr. Squid config name
            String configName = ConfigUtils.getDrSquidConfigName(environment);
            String[] nameParts = configName.split("\\.");
            if (nameParts.length == 0) {
                LOGGER.error("No active profile, can't set Dr. Squid profile - {}", executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Retrieve Dr. Squid config
            DrSquidConfig drSquidConfig = configRetriever.getConfig(configName);
            if (drSquidConfig == null) {
                LOGGER.error("Dr. Squid config \"{}\" cannot be found - {}", configName, executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            String client = nameParts[1];
            String profile = nameParts[2];
            LOGGER.debug("Client \"{}\" is using Dr. Squid profile \"{}\", accessing config \"{}\"", client, profile, configName);
            String forClientInProfile = String.format("for client \"%s\" in profile \"%s\"", client, profile);

            // Check if Dr. Squid is enabled globally.
            if (BooleanUtils.isNotTrue(drSquidConfig.isEnabled())) {
                LOGGER.debug("Dr. Squid is not enabled {} - {}", forClientInProfile, executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Set the configured global logging level (default ERROR).
            LoggingLevel loggingLevel = drSquidConfig.getLoggingLevel();
            if (loggingLevel != null) {
                FilteredLogger.setLoggingLevel(loggingLevel);
            }

            // Get configured services.
            List<Service> services = drSquidConfig.getServices();
            if (CollectionUtils.isEmpty(services)) {
                LOGGER.error("No services configured {} - {}", forClientInProfile, executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Find matching service for the current URL.
            Service matchingService = matchDownstream(services, downstreamUrl);
            if (matchingService == null) {
                LOGGER.debug("No enabled service matching downstream url \"{}\" configured {} - {}", downstreamUrl, forClientInProfile, executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Set logging level to the service logging level if present.
            loggingLevel = matchingService.getLoggingLevel();
            if (loggingLevel != null) {
                FilteredLogger.setLoggingLevel(loggingLevel);
            }

            String matchingServiceName = matchingService.getName();
            String forClientCallingServiceInProfile = String.format("for client \"%s\" calling service \"%s\" with drsquid.profile \"%s\"", client, matchingServiceName, profile);
            String downstreamPattern = matchingService.getUrlPattern();

            LOGGER.info("Downstream url \"{}\" matched enabled service \"{}\" {}...", downstreamUrl, matchingServiceName, forClientInProfile);

            // Check method, if configured.
            HttpMethod configuredMethod = matchingService.getMethod();
            HttpMethod actualMethod = interceptedRequest.getMethod();
            if (configuredMethod != null && configuredMethod != actualMethod) {
                LOGGER.debug("The method's don't match - configured: \"{}\", actual: \"{}\" - {}", configuredMethod, actualMethod, executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Check body keyword is contained in the body
            String bodyString = new String(interceptedRequestBody);
            String bodyKeyword = matchingService.getBodyKeyword();
            if (bodyKeyword != null && !bodyString.contains(bodyKeyword)) {
                LOGGER.debug("The request body doesn't contain the keyword \"{}\" - {}", bodyKeyword, executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Get configured behavior for this service.
            Behavior behavior = matchingService.getBehavior();
            if (behavior == null) {
                LOGGER.error("Behavior not configured {} - {}", forClientCallingServiceInProfile, executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Get success, failure, and timeout configs.
            Success success = behavior.getSuccess();
            Failure failure = behavior.getFailure();
            Timeout timeout = behavior.getTimeout();
            int successPercentage = (success == null) ? 0 : success.getPercentage();
            int failurePercentage = (failure == null) ? 0 : failure.getPercentage();
            int timeoutPercentage = (timeout == null) ? 0 : timeout.getPercentage();

            // Get which status we will mock.
            int[] percentageBuckets = getBuckets(new int[]{successPercentage, failurePercentage, timeoutPercentage});
            String status = getStatus(percentageBuckets);
            if (status == null) {
                return execution.execute(interceptedRequest, interceptedRequestBody); // Message already logged, see getBuckets
            }

            LOGGER.debug("Landed in {} bucket {}...", status, forClientCallingServiceInProfile);

            // Handle non-spoofed success (doesn't call Dr. Squid Service).
            if (status.equals("success") && success != null && !success.isSpoofed()) {

                LOGGER.debug("Success is not spoofed {} - will call real service", forClientCallingServiceInProfile);
                Delay delay = success.getDelay();
                if (delay == null) {
                    LOGGER.warn("Delay not configured for non-spoofed success {} - Defaulting to no delay.", forClientCallingServiceInProfile);
                    return execution.execute(interceptedRequest, interceptedRequestBody);
                }

                int timeToSleep = getTimeToSleep(delay);
                LOGGER.debug("Sleeping for {} ms delay...", timeToSleep);
                Thread.sleep(timeToSleep);
                LOGGER.info("Performing non-spoofed success {}...", forClientCallingServiceInProfile);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Get the Dr. Squid Service url.
            String drSquidUrl = environment.getProperty("drsquid.url");
            if (StringUtils.isBlank(drSquidUrl)) {
                LOGGER.error("Can't call drsquid-service as \"drsquid.url\" environment variable is not defined - {}" + executingInterceptedRequest);
                return execution.execute(interceptedRequest, interceptedRequestBody);
            }

            // Call Dr. Squid Service for spoofed success, failure, and timeout.
            LOGGER.info("Calling drsquid-service to spoof {} scenario {}...", status, forClientCallingServiceInProfile);
            String queryString = getQueryString(client, downstreamPattern, profile, status);
            String token = getToken(queryString);
            HttpRequest drSquidRequest = createDrSquidRequest(interceptedRequest, drSquidUrl, token);
            return execution.execute(drSquidRequest, interceptedRequestBody);

        } catch (Exception e) {
            // Rethrow as IOException, since this method can only throw IOException.
            // This is so that non Dr. Squid related errors propagate up instead of looking like Dr. Squid errors.
            throw new IOException(e);
        }
    }
}
