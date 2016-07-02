package com.boundary.dropwizard.consul.example;

import com.boundary.dropwizard.consul.ConsulClientConfig;
import com.boundary.dropwizard.consul.registration.ConsulRegistrationConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbitz.consul.Consul;
import io.dropwizard.setup.Environment;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ConsulConfig extends ConsulClientConfig {

    @JsonProperty
    @Valid
    @NotNull
    private ConsulRegistrationConfig registration;

    public Consul buildAndRegisterServices(Environment env) {
        final Consul consul = build();
        registration.register(env, consul.agentClient());
        return consul;
    }
}
