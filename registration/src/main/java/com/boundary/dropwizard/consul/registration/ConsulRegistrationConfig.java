package com.boundary.dropwizard.consul.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.orbitz.consul.AgentClient;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import java.util.Map;
import java.util.Optional;

public class ConsulRegistrationConfig {


    @JsonProperty
    private String healthConnectorName = "admin";

    @JsonProperty
    private String healthUrl = "http://localhost:%d/healthcheck";

    @JsonProperty
    private boolean registerJmx = true;

    @JsonProperty
    private Duration checkInterval = Duration.seconds(10);


    public String getHealthConnectorName() {
        return healthConnectorName;
    }

    public String getHealthUrl() {
        return healthUrl;
    }

    public boolean isRegisterJmx() {
        return registerJmx;
    }


    public void register(Environment environment, AgentClient client) {

        ConsulServiceRegistration csr = new ConsulServiceRegistration(client, environment.getName(), healthConnectorName, healthUrl, checkInterval, additionalServices());

        environment.lifecycle().addServerLifecycleListener(csr);
        environment.lifecycle().manage(csr);


    }

    private Map<String, Integer> additionalServices() {

        if (registerJmx) {
            Optional<Integer> jmxport = Optional.ofNullable(System.getProperty("com.sun.management.jmxremote.port")).map(Ints::tryParse);
            if (jmxport.isPresent()) {
                return ImmutableMap.of("jmx", jmxport.get());
            }
        }
        return ImmutableMap.of();
    }
}
