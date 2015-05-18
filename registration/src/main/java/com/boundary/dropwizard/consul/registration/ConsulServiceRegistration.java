package com.boundary.dropwizard.consul.registration;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.model.agent.Registration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class ConsulServiceRegistration implements Managed {

    private final BiConsumer<String, Integer> register;
    private final Consumer<String> deregister;
    private final ImmutableMap<String, Integer> services;

    public ConsulServiceRegistration(AgentClient agentClient,
                                     String serviceName,
                                     String healthConnector,
                                     String healthConnectorUrl,
                                     Duration checkInterval,
                                     String tagSeparator,
                                     Map<String, Integer> services) {

        this.services = ImmutableMap.copyOf(services);

        requireNonNull(serviceName);
        requireNonNull(healthConnector);
        requireNonNull(healthConnectorUrl);
        requireNonNull(checkInterval);
        requireNonNull(tagSeparator);

        final Integer healthPort = services.get(healthConnector);
        checkArgument(healthPort != null, "Health check key [%s] must match a configured service", healthConnector);

        final Registration.Check check = new Registration.Check();
        check.setHttp(String.format(healthConnectorUrl, healthPort));
        check.setInterval(checkInterval.toSeconds() + "s");

        final Function<String, String> serviceId = (tag) -> serviceName + tagSeparator + tag;
        this.register = (tag, port) ->
                agentClient.register(port, check, serviceName, serviceId.apply(tag), tag);
        this.deregister = (tag) -> agentClient.deregister(serviceId.apply(tag));

    }

    @Override
    public void start() throws Exception {
        services.forEach(this.register);
    }

    @Override
    public void stop() throws Exception {
        services.keySet().forEach(this.deregister);
    }
}
