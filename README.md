### dw-consul

Tools for integrating consul with dropwizard apps

Current implementation allows quick configuration and setup of a [Consul client](https://github.com/OrbitzWorldwide/consul-client), and registration of multiple
service endpoints.

## usage

# Maven:


```xml

        <dependency>
            <groupId>com.boundary.dropwizard.consul</groupId>
            <artifactId>registration</artifactId>
            <version>0.2</version>
        </dependency>
```

# service registration example:

In your configuration class:

```java

    @Valid
    @NotNull
    @JsonProperty("consul")
    private ConsulClientConfig consul = new ConsulClientConfig();

    @Valid
    @NotNull
    @JsonProperty("registration")
    private ConsulRegistrationConfig registration = new ConsulRegistrationConfig();

    public ConsulClientConfig getConsul() {
            return consul;
        }

    public ConsulRegistrationConfig getRegistration() {
        return registration;
    }

```

In your application class:

```java

        Consul consul = configuration.getConsul().build(environment);
        configuration.getRegistration().register(environment, consul.agentClient());

```

Now you can manage how/what your app registers via config

## minimal config
```yml

registration:
  services:
    service: {service port}
    admin: {admin port}

```

## all config options

```yml
consul: localhost:8500
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


```

# results

Here's an example of you you might get under the suggested config, registering a service named 'sasquatch':

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

You can query these healthy nodes based on tags as `curl localhost:8500/v1/health/service/sasquatch?tag=jmx`. See the [consul api docs](http://www.consul.io/docs/agent/http.html) for more info.

