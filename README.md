[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Dr. Squid

Dr Squid, a framework developed by Egencia (part of Expedia Group), is a downstream services and databases mocking tool primarily used for chaos testing and gathering performance metrics for Java Spring service.

## Development
See [Contributing](CONTRIBUTING.md) and [Code of Conduct](CODE-OF-CONDUCT.md) to contribute.

## Modules

For more information, including installing and running Dr. Squid, please follow these guidelines:

- [Dr. Squid Utils](dr-squid-utils/README.md)
- [Dr. Squid Service](dr-squid-service/README.md)

## Protocol Support

HTTP

## Description

In a distributed environment, components failing and/or behaving in an unpredictable manner is a given. Usually it's not a matter of “if” but “when” a downstream system that your service critically depends upon will fail. Although we cannot expect downstream dependencies to guarantee 100% up-time, but we can certainly control how our service behaves in the face of those failures. One of the key mechanisms for building resiliency into services is to use circuit-breakers and implement good fallback logic. Hystrix is a popular open-source library for enabling the circuit breaker capability that we've also used in the Egencia Hotels team. While we did various levels of testing (unit testing, manual testing, and performance testing) to test and configure Hystrix, what we also really needed for testing was a way to introduce random failures on running systems. To replicate real life scenarios, this new system could introduce failures in the form of slow responses, network errors, client side or server side errors (5xx, 4xx), etc. With this in mind, we decided to build that system as a framework for resiliency and performance testing called “Doctor Squid” or just Dr. Squid! 

## Use Cases

At Egencia, we used Dr. Squid to perform the following use cases:

### Chaos Testing

The main use case of this software is to check on how resilient our service is to the erroneous behavior of its downstreams, commonly referred to as “Chaos Testing”. With this software, we can mock different kinds of real life scenarios, including downstreams completely failing, starting to fail (some success but also some timeouts and failures), all the way up to complete success. Using tests like this helps ensure that if these realistic scenarios occur in our production environments, that we are resilient and our service handles these scenarios in the way we expect it to.

### Performance Testing

Aside from chaos testing, being able to mock all downstreams in a controlled manner lets us receive more accurate performance testing metrics for our services. This way of doing performance testing ensures that our metrics are accurate to our services’ performance itself and that they do not take into account performance of downstreams, which can often be chaotic as mentioned previously.

## Release

- Update all modules pom.xml to have new versions
- Once PR approved and merged to master, tag the release.
Example:
```bash
git tag -a 1.0.1 -m "release 1.0.1"
git push origin 1.0.1
```

## Legal
This project is available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

Copyright 2019 Expedia, Inc.
