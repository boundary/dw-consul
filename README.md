# dw-consul

Tools for integrating consul with dropwizard applications.

Current implementation allows quick configuration and setup of a managed [Consul client](https://github.com/OrbitzWorldwide/consul-client),
registration of multiple service endpoints hooked into the dropwizard app life cycle, and a dropwizard-friendly load balancer using consul
healthClient for service discovery.

## Motivation

Provide a shared, consistent method to make use of consul within dropwizard applications for service registration and discovery.

## Installation

### Maven

```xml

    <dependency>
        <groupId>com.boundary.dropwizard.consul</groupId>
        <artifactId>registration</artifactId>
        <version>0.14</version>
    </dependency>
```


Current implementation allows quick configuration and setup of a managed [Consul client](https://github.com/OrbitzWorldwide/consul-client),
registration of multiple service endpoints hooked into the dropwizard app life cycle, and a dropwizard-friendly load balancer using consul
healthClient for service discovery.


## Usage

These examples build upon each other - use of either `loadbalancer` or `registration` modules requires
the `client` module.

### Client example


```java

    @Valid
    @NotNull
    @JsonProperty("consul")
    private ConsulClientConfig consul = new ConsulClientConfig();

    public ConsulClientConfig getConsul() {
       return consul;
    }
```

In your application class:

```java
        // get a consul-client object. see https://github.com/OrbitzWorldwide/consul-client for more info
        Consul consul = configuration.getConsul().build(environment);

```

Configuration:

```yml
consul:
  agent: localhost:8500

```

### Registration example:


In your configuration class:

```java


    @Valid
    @NotNull
    @JsonProperty("consul")
    private ConsulClientConfig consul = new ConsulClientConfig();

    public ConsulClientConfig getConsul() {
       return consul;
    }

    @Valid
    @NotNull
    @JsonProperty("registration")
    private ConsulRegistrationConfig registration = new ConsulRegistrationConfig();

    public ConsulRegistrationConfig getRegistration() {
        return registration;
    }

```

In your application class:

```java

    Consul consul = configuration.getConsul().build(environment);

    // register your configured application services in consul
    configuration.getRegistration().register(environment, consul.agentClient());

```

Configuration:

```yml

registration:
  services:
    service: {service port}
    admin: {admin port}

```

### Load balancer example


In your configuration class:

```java


    @Valid
    @NotNull
    @JsonProperty("consul")
    private ConsulClientConfig consul = new ConsulClientConfig();

    public ConsulClientConfig getConsul() {
       return consul;
    }

    @JsonProperty
    @Valid
    @NotNull
    private LBFactory loadBalancer;

    public LBFactory getLoadBalancer() {
        return loadBalancer;
    }

```

In your application class:

```java

    Consul consul = configuration.getConsul().build(environment);

    // create typed ClientFactory instance
    // that will create a client of your choosing
    // based on a ServiceHealth (https://github.com/OrbitzWorldwide/consul-client/blob/master/src/main/java/com/orbitz/consul/model/health/ServiceHealth.java) instance
    final ClientFactory<CLIENT>> clientFactory = // call your clientFactory code

    final LoadBalancer<CLIENT> lb = configuration.getLoadBalancer().build(env, consul.healthClient(), clientFactory);
    // now call lb.getClient() each time you need to make a request to the target service

```


### Configuration:

```yml

loadBalancer:
  type: round-robin
  serviceName: {targetServicName}
  serviceTag: {optional service tag}

```

## all config options

```yml
consul:
  agent: localhost:8500

registration:
  healthConnectorName: admin
  serviceName: serviceName
  registerJmx: true
  checkInterval: 10s
  healthUrl: http://localhost:%d/healthcheck
  tagSeparator: _
  services:
    service: {service port}
    admin: {admin port}
    other: {other port}

loadBalancer:
  type: round-robin
  serviceName: {targetServicName}
  serviceTag: {optional service tag}
  watchSeconds: 30

```

### registration format

Using the registration module, here's an example of you you might get under the suggested config, registering a service named 'sasquatch':

```json
[
  {
    "Node": "premium.local",
    "Address": "172.16.9.138",
    "ServiceID": "sasquatch_jmx",
    "ServiceName": "sasquatch",
    "ServiceTags": [
      "jmx"
    ],
    "ServiceAddress": "",
    "ServicePort": 11502
  },
  {
    "Node": "premium.local",
    "Address": "172.16.9.138",
    "ServiceID": "sasquatch_service",
    "ServiceName": "sasquatch",
    "ServiceTags": [
      "service"
    ],
    "ServiceAddress": "",
    "ServicePort": 11500
  },
  {
    "Node": "premium.local",
    "Address": "172.16.9.138",
    "ServiceID": "sasquatch_admin",
    "ServiceName": "sasquatch",
    "ServiceTags": [
      "admin"
    ],
    "ServiceAddress": "",
    "ServicePort": 11501
  }
]
```

You can query these healthy nodes based on tags as `curl 127.0.0.1:8500/v1/health/service/sasquatch?tag=jmx`. See the [consul api docs](http://www.consul.io/docs/agent/http.html) for more info.


# Tests

To run the tests, clone the repo and run `mvn test` from the parent directory.

# LICENSE

Copyright 2016 BMC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
