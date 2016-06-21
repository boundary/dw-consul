package com.boundary.dropwizard.consul.registration;

import com.google.common.collect.ImmutableList;
import com.orbitz.consul.AgentClient;
import io.dropwizard.lifecycle.Managed;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ConsulServiceRegistration implements Managed {

    private final Consumer<ConsulRegistrationConfig.ServiceConfig> register;
    private final Consumer<String> deregister;
    private final ImmutableList<ConsulRegistrationConfig.ServiceConfig> services;

    public ConsulServiceRegistration(AgentClient agentClient,
                                     String serviceName,
                                     String tagSeparator,
                                     List<ConsulRegistrationConfig.ServiceConfig> services) {

        this.services = ImmutableList.copyOf(services);
        requireNonNull(serviceName);
        requireNonNull(tagSeparator);

        final Function<String, String> serviceId = (tag) -> serviceName + tagSeparator + tag;
        this.register = (serviceConfig) -> {
            agentClient.register(
                    serviceConfig.getPort(),
                    serviceConfig.getHealthCheck(),
                    serviceName,
                    serviceId.apply(serviceConfig.getServiceTag()),
                    serviceConfig.getServiceTag()
            );
        };

        this.deregister = (tag) -> agentClient.deregister(serviceId.apply(tag));
    }

    @Override
    public void start() throws Exception {
        services.forEach(this.register);
    }

    @Override
    public void stop() throws Exception {
        services.stream()
                .map(ConsulRegistrationConfig.ServiceConfig::getServiceTag)
                .forEach(this.deregister);
    }
}
