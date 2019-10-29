# dr-squid-utils

This module contains **dr-squid-utils**, which provides utilities including most importantly the **DrSquidInterceptor**.

See: [Changelog](../CHANGELOG.md)

## Dependency Versions
* Spring Framework 5.0
* Spring Boot 2.0

## Usage

1. **Import Dr. Squid**

   Add dependency 
   ```xml
   <dependency>
      <groupId>com.expediagroup</groupId>
      <artifactId>dr-squid</artifactId>
      <version>X.X.X</version> <!--Adopt the latest version-->
   </dependency>
   ```
   to the `pom.xml` file of your project/module.
   
2. **Hook Up Interceptor**

   In your driver class where you create your `RestTemplate` for calling the downstream dependencies add the new interceptor:
   
   ```java
   //imports...
   
   class Example {
    
       @Autowired
       private DrSquidInterceptor drSquidInterceptor;
      
       //...
        
       public RestTemplate createRestTemplate() {
        
           //...
        
           // Add drSquidInterceptor to this restTemplate.
           restTemplate.addInterceptors(drsquidInterceptor);
            
           //...
       }
   }
   ```

3. **Use ComponentScan with Spring Configuration**

   For Dr. Squid to work correctly, you will also need to add a `ComponentScan` onto your `ApplicationPropertiesConfiguration.java` file:
   
   ```java
   // imports...
   
   @Configuration
   @ComponentScan("com.expediagroup.drsquidutils.*")
   public class ApplicationPropertiesConfiguration {
       // ...the rest of your existing class...
   }
   ```

4. **Create Your Dr. Squid Configuration**

   See the [sample configuration](#sample-configuration) below, and create the appropriate files for your scenarios.

   *Note:* All Dr. Squid config names must follow the convention `drsquid.<Application Name>.<Dr. Squid Profile>`.
    
   - **`<Application Name>`** is the name returned by `applicationMetadataHolder.getApplicationName()`, and should be the name of your service.
   
   - **`<Dr. Squid Profile>`** is the value of the `drsquid.profile` property. This lets you maintain multiple configs for one service, and switch the Dr. Squid Profile based upon use case.

5. **Set Java Properties (in application.yml)**

   In your service's `application.yml` add the `drsquid.url` and `drsquid.profile` properties in each spring profile section you wish to use Dr. Squid with.
   
   Below is a common use case for this, where (let's say) we've already defined a very basic configuration which disables Dr. Squid globally at `drsquid.example-service.disabled` as well as a basic configuration that fails all calls at `drsquid.example-service.chaos` and set the Dr. Squid URLs and profiles accordingly:
   
   ```yaml
   # Start of the default spring profile
    
   #......
    
   # DrSquid Service (default profile)
   drsquid:
     url: https://int.site.com/drsquid-service/v1/mock # Integration environment URL
     profile: disabled                                 # Use disabled profile by default
    
   #......
    
   # Start of the 'beta' spring profile
   ---
   spring:
     profiles: beta
    
   #......
    
   # DrSquid Service (beta profile)
   drsquid:
     # Still want Integration URL so not setting and taking the default
     profile: chaos # Use chaos profile to fail all calls
    
   #......
    
   # Start of the 'prod' spring profile
   ---
   spring:
     profiles: prod
    
   #......
    
   # DrSquid Service (prod profile)
   drsquid:
     url: https://www.site.com/drsquid-service/v1/mock # Using prod environment for prod call mocking
     # Still want disabled profile so not setting and taking the default
    
   #......
   ```

## Building

```bash
mvn clean install
```

## Sample Configuration

```yaml
# Dr. Squid Sample Config

# Whether Dr. Squid is enabled. 
enabled: false

# List of configurations for mocking databases using Dr. Squid.
databases:
  
  # List item (-) starts the definition of a database configuration.
  - type: SQL # Only "SQL" and "MONGO" are accepted.
    description: SQL calls should fail. # Optional description of the behavior of this database under Dr. Squid.

# List of configurations for mocking web services using Dr. Squid.
services:
  
  # List item (-) starts the definition of a service configuration. This one will be relatively simple.
  - enabled: true # Whether Dr. Squid is enabled for this particular service.
    name: Site One # A name to give this service, recommended to just go with how you reference it verbally.
    description: Site One calls should all fail. # Optional description of the behavior of this service under Dr. Squid.

    # URL pattern used to match to this particular service. Wildcards (*) accepted. 
    # Note: Currently takes query parameters into account, but because of the unordered nature of query parameters, 
    #       it is not recommended to try to match more than one parameter in a single pattern.
    pattern: http://www.siteone.com/* 

    behavior: # Behavior Dr. Squid will give this service.
      failure: # Defining a scenario. Must be one of "failure", "success", or "timeout".
        percentage: 100 # Percentage of calls to affect with this scenario. In this case, 100%.
        mock_response_body: FAILURE BODY # A mock response body to send back from the mock calls. In this case, a dummy message. 
        mock_status_code: 500 # A mock status code to send back from the mock calls. In this case, a 500 internal server error.
        delay: # Defining a delay in sending the response.
          type: fixed # Defining the delay type. Must be one of "fixed", "normal", or "range". See below service for all examples.
          fixed_value_in_msecs: 100 # Fixed amount of time to wait before sending each response in milliseconds. In this case, 100ms.

  # This service showcases most of Dr. Squid's functionality.
  - name: Site Two
    description: Site Two calls should start failing.
    pattern: http://www.sitetwo.com/*
    enabled: true
    behavior:
      failure: # 20% varying speed failures with a spoofed body and 500 error code.
        percentage: 20
        mock_response_body: FAILURE BODY
        mock_status_code: 500
        delay:
          type: range # Range delay expects a minimum and maximum value. Uses a uniform distribution.
          min_value_in_msecs: 1000 # Min value of one second.
          max_value_in_msecs: 5000 # Max value of five seconds.
      success: # 70% (mostly realistically) slow un-spoofed success.
        percentage: 70
        # On success configurations, we can define "spoofed" - whether the real call goes through,
        # or a Dr. Squid spoofed one does instead. In this case, we are not spoofing using Dr. Squid.
        # This also means we do not need to define a mock_response_body or mock_status_code.
        spoofed: false
        delay: # We can also define delay, even on spoofed success. This just adds time before the real call is executed.
          type: normal # Normal delay expects mean and standard deviation values. Uses a general normal distribution.
          mean_value_in_msecs: 2000 # The mean of the distribution. In this case, calls will be around two seconds.
          std_value_in_msecs: 500 # Essentially how near the mean values should fall. In this case, a std. dev. of half a second is moderate variation.
      timeout: # 10% full timeout. Notice that all percentages must add up to 100. In this case (20+70+10==100) they do.
        percentage: 10
        delay:
          type: fixed
          # Notice that on timeout configurations, the time you give it should be more than the configured timeout for calling this service.
          # In this case we went with a very generous time of one minute.
          fixed_value_in_msecs: 60000
```

## Configuration Reference

What follows is a full reference for the configuration. It's recommended to use the sample config above if you're just starting out. This following is mostly for reference.

*NOTE:* Those words in **Bold** are referencing other parts of the configuration, and more can be learned by visiting that other section of the document. 

### DrSquidConfig
These are the top level fields in the config.
#### enabled
Boolean defining whether Dr. Squid is enabled for this application and profile. If false, the interceptor simply executes the original request that came to it, not exhibiting any Dr. Squid functionality.
#### services
List of configurations for mocking **Services** using Dr. Squid.
#### databases
List of configurations for mocking **Database** using Dr. Squid.

### Service
Configuration of a downstream web service we wish to mock.
#### enabled
Boolean defining whether Dr. Squid is enabled for this service. If false, the service will never be selected to be mocked.
#### name
String name you can give the service to help clarity, also used in some logging messages.
#### description
String description of the scenario the service will be mocked to emulate. Not required.
#### pattern
String pattern used to match a downstream URL to this particular service. Wildcards (\*) accepted.

*Note:* Currently takes query parameters into account, but because of the unordered nature of query parameters, it is not recommended to try to match more than one parameter in a single pattern.
#### behavior
**Behavior** to exhibit once this service config is matched to the downstream URL.

### Database
Configuration of a downstream database we wish to mock.

*Note:* This functionality is incomplete, and can only perform complete database failure per type.
#### enabled
Boolean defining whether Dr. Squid is enabled for this database type. If false, the database's calls will never be selected to be mocked.
#### type
String type of database you are mocking. All database calls of this type will be intercepted and exceptions thrown.

Supported types:
- MONGO
- SQL
#### description
String description of the scenario the database will be mocked to emulate. Not required. 

### Behavior
Configuration of the behavior of a service.

#### success
**Success** behavior to exhibit. Optional.

#### failure
**Failure** behavior to exhibit. Optional.

#### timeout
**Timeout** behavior to exhibit. Optional.

### Success
Configuration of the behavior of a service call which is succeeding.

#### percentage
Integer percentage of calls which should fall into this bucket. Must be in the range \[0,100\], and sum of all percentages (success + failure + timeout) must be exactly equal to 100.
#### spoofed
Boolean defining whether this success scenario should be mocked. If true, the call will be mocked through Dr. Squid in the normal way. If false, the call will be delayed inside of the interceptor according to the *delay* field, then the real call will proceed as normal.

*Note:* This is very useful in a production chaos testing use case. 
#### mock_response_body
String mock response body to send back instead. Only required if *spoofed* is set to true.
#### mock_status_code
Integer mock status code to send back.
#### delay	
How to **Delay** the response.

### Failure
Configuration of the behavior of a service call which is failing.

#### percentage
Integer percentage of calls which should fall into this bucket. Must be in the range \[0,100\], and sum of all percentages (success + failure + timeout) must be exactly equal to 100.
#### mock_response_body
String mock response body to send back.
#### mock_status_code
Integer mock status code to send back.
#### delay
How long to **Delay** the mocked response.

### Timeout
Configuration of the behavior of a service call which is timing out.
#### percentage
Integer percentage of calls which should fall into this bucket. Must be in the range \[0,100\], and sum of all percentages (success + failure + timeout) must be exactly equal to 100.
#### delay
How to **Delay** the response.
*NOTE:* This should be a delay guaranteed to be longer than your service's configured timeout for calling this downstream. 

### Delay
Configuration of how long to wait.

#### type
There are currently 3 supported delay types, and therefore the "type" field has 3 acceptable values:

- *fixed*: an exact amount of time to wait.
- *range*: a [uniformly distributed](https://en.wikipedia.org/wiki/Discrete_uniform_distribution) random amount of time to wait. 
- *normal*: a [normally distributed](https://en.wikipedia.org/wiki/Normal_distribution#General_normal_distribution) random amount of time to wait.

#### fixed_value_in_msecs
Fixed time to wait in milliseconds. Only applies to the **fixed** delay type.

#### mean_value_in_msecs 
Integer mean value of the normal distribution of time to wait in milliseconds.

#### std_value_in_msecs
Integer standard deviation value of the normal distribution of time to wait in milliseconds.

#### min_value_in_msecs
Integer minimum value of the uniform distribution of time to wait in milliseconds.

#### max_value_in_msecs
Integer maximum value of the uniform distribution of time to wait in milliseconds.
