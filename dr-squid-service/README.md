# dr-squid-service

This module contains **dr-squid-service**, which serves requests from the **DrSquidInterceptor** in the **dr-squid-utils** module.

See: [Changelog](../CHANGELOG.md)

## Dependency Versions
* Spring Framework 5.0
* Spring Boot 2.0

## Building & Running

### Install

This runs the tests and generates a jar in your target directory `dr-squid-service/target`.

```bash
cd dr-squid/
mvn clean install
```

### Run the Jar File

This runs the program manually using java from command line.

```bash
cd dr-squid-service/target
java -jar dr-squid-service.jar 
```

### Run Using Docker

This runs the program through the docker file.

```bash
cd dr-squid-service
mvn clean package                           //builds the jar
docker build -t dr-squid-service .          //builds the docker image
docker run -p 5000:8080 dr-squid-service    //Runs the application at 5000 port number
```
