### dw-consul

Tools for integrating consul with dropwizard applications.

Current implementation allows quick configuration and setup of a managed [Consul client](https://github.com/OrbitzWorldwide/consul-client), 
registration of multiple service endpoints hooked into the dropwizard app life cycle, and a dropwizard-friendly load balancer using consul
healthClient for service discovery.


## usage

# Maven:

```xml

        <dependency>
            <groupId>com.boundary.dropwizard.consul</groupId>
            <artifactId>registration</artifactId>
            <version>0.8</version>
        </dependency>
```

## code examples:

These examples build upon each other - use of either `loadbalancer` or `registration` modules requires
the `client` module.


# client example


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

# registration example:

In your configuration class:

```java

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

# load balancer example


In your configuration class:

```java

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

// create typed ClientFactory instance
// that will create a client of your choosing
// based on a ServiceHealth (https://github.com/OrbitzWorldwide/consul-client/blob/master/src/main/java/com/orbitz/consul/model/health/ServiceHealth.java) instance
final ClientFactory<CLIENT>> clientFactory = // call your clientFactory code

final LoadBalancer<CLIENT> lb = configuration.getLoadBalancer().build(env, consul.healthClient(), clientFactory);
// now call lb.getClient() each time you need to make a request to the target service

```


Configuration:

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

# registration format

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


# tests

To run the tests, clone the repo and run `mvn test` from the parent directory.

