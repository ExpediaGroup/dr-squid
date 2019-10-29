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

import com.expediagroup.drsquidutils.configretriever.ConfigRetriever;
import com.expediagroup.drsquidutils.configretriever.FileSystemConfigRetriever;
import com.expediagroup.drsquidutils.model.Delay;
import com.expediagroup.drsquidutils.model.DrSquidConfig;
import com.expediagroup.drsquidutils.model.LoggingLevel;
import com.expediagroup.drsquidutils.model.Service;
import com.expediagroup.drsquidutils.utils.RandomNumberGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static com.expediagroup.drsquidutils.utils.Constants.DR_SQUID_RESPONSE;
import static com.expediagroup.drsquidutils.utils.Constants.DR_SQUID_URL;
import static com.expediagroup.drsquidutils.utils.Constants.FACEBOOK_RESPONSE;
import static com.expediagroup.drsquidutils.utils.Constants.FACEBOOK_URL;
import static com.expediagroup.drsquidutils.utils.Constants.FAILURE;
import static com.expediagroup.drsquidutils.utils.Constants.GOOGLE_RESPONSE;
import static com.expediagroup.drsquidutils.utils.Constants.GOOGLE_URL;
import static com.expediagroup.drsquidutils.utils.Constants.POST_RESPONSE;
import static com.expediagroup.drsquidutils.utils.Constants.POST_URL;
import static com.expediagroup.drsquidutils.utils.Constants.SUCCESS;
import static com.expediagroup.drsquidutils.utils.Constants.TEST_CLIENT_NAME;
import static com.expediagroup.drsquidutils.utils.Constants.TEST_DOWNSTREAM_PATTERN;
import static com.expediagroup.drsquidutils.utils.Constants.TEST_PROFILE;
import static com.expediagroup.drsquidutils.utils.Constants.TEST_TOKEN;
import static com.expediagroup.drsquidutils.utils.Constants.TIMEOUT;
import static com.expediagroup.drsquidutils.utils.Constants.YAHOO_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DrSquidInterceptorTest {

    @Spy
    @InjectMocks
    private DrSquidInterceptor drSquidInterceptor = new DrSquidInterceptor();

    @Mock
    private Environment environment;

    @Mock
    private RandomNumberGenerator random;

    @Mock
    private FileSystemConfigRetriever fileSystemConfigRetriever;

    private byte[] getFileBytes(String fileName) throws IOException, NullPointerException {

        URL url = getClass().getClassLoader().getResource(fileName);
        File file = new File(url.getFile());
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bytes);
        return bytes;
    }

    @Test
    public void getBucketsTestSumAt100() {

        // Arrange
        int[] percentages = {70, 20, 10};

        // Act
        int[] buckets = drSquidInterceptor.getBuckets(percentages);

        // Assert
        assertNotNull(buckets);
        assertEquals(70, buckets[0]);
        assertEquals(90, buckets[1]);
        assertEquals(100, buckets[2]);
    }

    @Test
    public void getBucketsTestSumBelow100() {

        // Arrange
        int[] percentages = {50, 20, 10};

        // Act
        int[] buckets = drSquidInterceptor.getBuckets(percentages);

        // Assert
        assertEquals(0, buckets.length);
    }


    @Test
    public void getBucketsTestSumAbove100() {

        // Arrange
        int[] percentages = {90, 20, 10};

        // Act
        int[] buckets = drSquidInterceptor.getBuckets(percentages);

        // Assert
        assertEquals(0, buckets.length);
    }

    @Test
    public void getBucketsTestNegativePercentage() {

        // Arrange
        int[] percentages = {-70, 20, 10};

        // Act
        int[] buckets = drSquidInterceptor.getBuckets(percentages);

        // Assert
        assertEquals(0, buckets.length);
    }

    @Test
    public void getStatusTestFirstBucket() {

        // Arrange
        int[] buckets = {70, 90, 100};
        when(random.nextInt(100)).thenReturn(35);

        // Act
        String status = drSquidInterceptor.getStatus(buckets);

        // Assert
        assertNotNull(status);
        assertEquals(SUCCESS, status);
    }

    @Test
    public void getStatusTestSecondBucket() {

        // Arrange
        int[] buckets = {70, 90, 100};
        when(random.nextInt(100)).thenReturn(75);

        // Act
        String status = drSquidInterceptor.getStatus(buckets);

        // Assert
        assertNotNull(status);
        assertEquals(FAILURE, status);
    }

    @Test
    public void getStatusTestThirdBucket() {

        // Arrange
        int[] buckets = {70, 90, 100};
        when(random.nextInt(100)).thenReturn(95);

        // Act
        String status = drSquidInterceptor.getStatus(buckets);

        // Assert
        assertNotNull(status);
        assertEquals(TIMEOUT, status);
    }

    @Test
    public void getStatusTestFirstBucketEdge() {

        // Arrange
        int[] buckets = {70, 90, 100};
        when(random.nextInt(100)).thenReturn(69); // This gets incremented, since random.nextInt(100) returns 0-99 inclusive, so the actual number for bucketizing will be 70

        // Act
        String status = drSquidInterceptor.getStatus(buckets);

        // Assert
        assertNotNull(status);
        assertEquals(SUCCESS, status);
    }

    @Test
    public void getStatusTestLastBucketEdge() {

        // Arrange
        int[] buckets = {70, 90, 100};
        when(random.nextInt(100)).thenReturn(99); // This gets incremented, since random.nextInt(100) returns 0-99 inclusive, so the actual number for bucketizing will be 70

        // Act
        String status = drSquidInterceptor.getStatus(buckets);

        // Assert
        assertNotNull(status);
        assertEquals(TIMEOUT, status);
    }

    @Test
    public void getStatusTestEmptyBuckets() {

        // Act
        String status = drSquidInterceptor.getStatus(new int[0]);

        // Assert
        assertNull(status);
    }

    @Test
    public void getQueryStringTest() {

        // Arrange
        String clientName = TEST_CLIENT_NAME;
        String downstreamPattern = TEST_DOWNSTREAM_PATTERN;
        String profile = TEST_PROFILE;
        String status = SUCCESS;
        String expectedQueryString = "clientName=" + clientName + "&downstreamPattern=" + downstreamPattern + "&profile=" + profile + "&status=" + status;

        // Act
        String queryString = drSquidInterceptor.getQueryString(clientName, downstreamPattern, profile, status);

        // Assert
        assertNotNull(queryString);
        assertEquals(expectedQueryString, queryString);
    }

    @Test
    public void getTokenTest() {

        // Arrange
        String queryString = "clientName=test-service&downstreamPattern=*hello*&profile=test&status=success";
        String expectedToken = TEST_TOKEN;

        // Act
        String token = drSquidInterceptor.getToken(queryString);

        // Assert
        assertNotNull(token);
        assertEquals(expectedToken, token);
    }

    @Test
    public void createDrSquidRequestTest() throws Exception {

        // Arrange
        HttpRequest request = new SimpleClientHttpRequestFactory().createRequest(new URI(GOOGLE_URL), HttpMethod.GET);
        String drSquidUrl = DR_SQUID_URL;
        String token = TEST_TOKEN;
        String expectedUri = drSquidUrl + "?token=" + TEST_TOKEN;

        // Act
        HttpRequest drSquidRequest = drSquidInterceptor.createDrSquidRequest(request, drSquidUrl, token);

        // Assert
        assertNotNull(drSquidRequest);
        assertEquals(HttpMethod.GET, drSquidRequest.getMethod());
        assertEquals(expectedUri, drSquidRequest.getURI().toString());
    }

    @Test
    public void matchDownstreamTestTwoEnabledServices() {

        // Arrange
        Service tripadvisor = new Service("TripAdvisor", null, "*tripadvisor*", HttpMethod.GET, null, true, LoggingLevel.NONE, null);
        Service google = new Service("Google", null, "*google.com*", HttpMethod.GET, null, true, LoggingLevel.NONE, null);
        List<Service> services = Arrays.asList(tripadvisor, google);
        String downstreamUrl = GOOGLE_URL;

        // Act
        Service matchingService = drSquidInterceptor.matchDownstream(services, downstreamUrl);

        // Assert
        assertNotNull(matchingService);
        assertEquals("Google", matchingService.getName());
    }

    @Test
    public void matchDownstreamTestNotMatchDisabledService() {

        // Arrange
        Service testService = new Service("testService", null, "*testService*", HttpMethod.GET, null, true, LoggingLevel.NONE, null);
        Service google = new Service("Google", null, "*google.com*", HttpMethod.GET, null, false, LoggingLevel.NONE, null);
        List<Service> services = Arrays.asList(testService, google);
        String downstreamUrl = GOOGLE_URL;

        // Act
        Service matchingService = drSquidInterceptor.matchDownstream(services, downstreamUrl);

        // Assert
        assertNull(matchingService);
    }

    @Test
    public void matchDownstreamTestNoMatchingServices() {

        // Arrange
        Service testService = new Service("testService", null, "*testService*", HttpMethod.GET, null, true, LoggingLevel.NONE, null);
        Service google = new Service("Google", null, "*google.com*", HttpMethod.GET, null, true, LoggingLevel.NONE, null);
        List<Service> services = Arrays.asList(testService, google);
        String downstreamUrl = YAHOO_URL;

        // Act
        Service matchingService = drSquidInterceptor.matchDownstream(services, downstreamUrl);

        // Assert
        assertNull(matchingService);
    }

    @Test
    public void getTimeToSleepTestUnsupportedDelayType() {

        // Arrange
        Delay delay = () -> "blah";

        // Act
        int timeToSleep = drSquidInterceptor.getTimeToSleep(delay);

        // Assert
        assertEquals(0, timeToSleep);
    }

    @Test
    public void testInterceptorGet() throws URISyntaxException, IOException {
        // Arrange
        // Variable set up
        URI googleUri = new URI(GOOGLE_URL);
        URI facebookUri = new URI(FACEBOOK_URL);
        URI drSquidUri = new URI(DR_SQUID_URL);

        HttpRequest googleRequest = mock(HttpRequest.class);
        HttpRequest facebookRequest = mock(HttpRequest.class);
        HttpRequest drSquidRequest = mock(HttpRequest.class);

        String googleResponseText = GOOGLE_RESPONSE;
        String facebookResponseText = FACEBOOK_RESPONSE;
        String drSquidResponseText = DR_SQUID_RESPONSE;

        ClientHttpResponse googleResponse = mock(ClientHttpResponse.class);
        ClientHttpResponse facebookResponse = mock(ClientHttpResponse.class);
        ClientHttpResponse drSquidResponse = mock(ClientHttpResponse.class);

        byte[] emptyBody = new byte[1];
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        String fileName = "testConfig.yaml";
        ConfigRetriever configRetriever = new FileSystemConfigRetriever();
        DrSquidConfig drSquidConfig = configRetriever.getConfig(fileName);

        // Set up environment
        when(environment.getProperty(eq("drsquid.url"))).thenReturn(drSquidUri.toString());
        when(environment.getProperty(eq("drsquid.profile"))).thenReturn("test");
        when(environment.getProperty(eq("info.build.artifact"))).thenReturn("fake-service");
        when(fileSystemConfigRetriever.getConfig(eq("drsquid.fake-service.test"))).thenReturn(drSquidConfig);

        // Set up requests
        when(googleRequest.getURI()).thenReturn(googleUri);
        when(googleRequest.getMethod()).thenReturn(HttpMethod.GET);
        when(facebookRequest.getURI()).thenReturn(facebookUri);

        // Set up responses
        when(facebookResponse.getStatusText()).thenReturn(facebookResponseText);
        when(drSquidResponse.getStatusText()).thenReturn(drSquidResponseText);

        // Set up execution
        when(execution.execute(any(), any())).thenAnswer(invocation -> {

            // Get URI of passed-in request argument
            Object firstArgument = invocation.getArguments()[0];
            URI uri = ((HttpRequest) firstArgument).getURI();

            // Ignore query params
            uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());

            // Match URIs to mocked responses
            if (uri.equals(googleUri)) {
                return googleResponse;
            } else if (uri.equals(facebookUri)) {
                return facebookResponse;
            } else if (uri.equals(drSquidUri)) {
                return drSquidResponse;
            }

            throw new InvalidUseOfMatchersException(
                    String.format("Argument %s does not match", firstArgument)
            );
        });

        // Act
        ClientHttpResponse firstGetResponse = drSquidInterceptor.intercept(googleRequest, emptyBody, execution);
        ClientHttpResponse secondGetResponse = drSquidInterceptor.intercept(facebookRequest, emptyBody, execution);

        // Assert
        assertEquals(drSquidResponseText, firstGetResponse.getStatusText());
        assertEquals(facebookResponseText, secondGetResponse.getStatusText());
    }

    @Test
    public void testInterceptorPost() throws Exception {
        // Arrange
        // Variable set up
        URI postUri = new URI(POST_URL);
        URI drSquidUri = new URI(DR_SQUID_URL);

        HttpRequest postRequest = mock(HttpRequest.class);
        HttpRequest drSquidRequest = mock(HttpRequest.class);

        String postResponseText = POST_RESPONSE;
        String drSquidResponseText = DR_SQUID_RESPONSE;

        ClientHttpResponse postResponse = mock(ClientHttpResponse.class);
        ClientHttpResponse drSquidResponse = mock(ClientHttpResponse.class);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        String fileName = "testConfig.yaml";
        ConfigRetriever configRetriever = new FileSystemConfigRetriever();
        DrSquidConfig drSquidConfig = configRetriever.getConfig(fileName);

        byte[] matchingBody = getFileBytes("matchingPostBody.txt");
        byte[] normalBody = getFileBytes("normalPostBody.txt");

        // Set up environment
        when(environment.getProperty(eq("drsquid.url"))).thenReturn(drSquidUri.toString());
        when(environment.getProperty(eq("drsquid.profile"))).thenReturn("test");
        when(environment.getProperty(eq("info.build.artifact"))).thenReturn("fake-service");
        when(fileSystemConfigRetriever.getConfig(eq("drsquid.fake-service.test"))).thenReturn(drSquidConfig);

        // Set up requests
        when(postRequest.getURI()).thenReturn(postUri);
        when(postRequest.getMethod()).thenReturn(HttpMethod.POST);

        // Set up responses
        when(drSquidResponse.getStatusText()).thenReturn(drSquidResponseText);
        when(postResponse.getStatusText()).thenReturn(postResponseText);

        // Set up execution
        when(execution.execute(any(), any())).thenAnswer(invocation -> {

            // Get URI of passed-in request argument
            Object firstArgument = invocation.getArguments()[0];
            URI uri = ((HttpRequest) firstArgument).getURI();

            // Ignore query params
            uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());

            // Match URIs to mocked responses
            if (uri.equals(drSquidUri)) {
                return drSquidResponse;
            } else if (uri.equals(postUri)) {
                return postResponse;
            }

            throw new InvalidUseOfMatchersException(
                    String.format("Argument %s does not match", firstArgument)
            );
        });

        // Act
        ClientHttpResponse firstPostResponse = drSquidInterceptor.intercept(postRequest, matchingBody, execution);
        ClientHttpResponse secondPostResponse = drSquidInterceptor.intercept(postRequest, normalBody, execution);

        // Assert
        assertEquals(drSquidResponseText, firstPostResponse.getStatusText());
        assertEquals(postResponseText, secondPostResponse.getStatusText());
    }
}
