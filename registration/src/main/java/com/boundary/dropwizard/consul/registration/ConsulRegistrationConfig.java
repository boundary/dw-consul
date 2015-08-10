package com.boundary.dropwizard.consul.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import com.orbitz.consul.AgentClient;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ConsulRegistrationConfig {


    @NotEmpty
    @JsonProperty
    private String healthConnectorName = "admin";

    @NotNull
    @JsonProperty
    private Optional<String> serviceName = Optional.empty();

    @NotEmpty
    @JsonProperty
    private String healthUrl = "http://localhost:%d/healthcheck";

    @JsonProperty
    private boolean registerJmx = true;

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    @NotNull
    @JsonProperty
    private Duration checkInterval = Duration.seconds(10);

    @NotNull
    @JsonProperty
    private String tagSeparator = "_";

    @NotNull
    @JsonProperty
    private Map<String, Integer> services;


    public void register(Environment environment, AgentClient client) {

        ConsulServiceRegistration csr = new ConsulServiceRegistration(
                client,
                getServiceName().orElse(environment.getName()),
                getHealthConnectorName(),
                getHealthUrl(),
                getCheckInterval(),
                getTagSeparator(),
                getServices()
                );

        environment.lifecycle().manage(csr);

    }

    public Map<String, Integer> getServices() {

        if (isRegisterJmx() && services.get("jmx") == null) {
            Optional<Integer> jmxport = Optional.ofNullable(System.getProperty("com.sun.management.jmxremote.port")).map(Ints::tryParse);
            if (jmxport.isPresent()) {
                return ImmutableMap.<String, Integer>builder()
                        .putAll(services)
                        .put("jmx", jmxport.get())
                        .build();
            }
        }
        return services;
    }

    public void setRegisterJmx(boolean registerJmx) {
        this.registerJmx = registerJmx;
    }

    public void setServices(Map<String, Integer> services) {
        this.services = services;
    }

    public String getTagSeparator() {
        return tagSeparator;
    }

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public boolean isRegisterJmx() {
        return registerJmx;
    }

    public String getHealthUrl() {
        return healthUrl;
    }

    public Optional<String> getServiceName() {
        return serviceName;
    }

    public String getHealthConnectorName() {
        return healthConnectorName;
    }

}
