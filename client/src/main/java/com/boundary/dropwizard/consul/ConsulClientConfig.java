package com.boundary.dropwizard.consul;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import io.dropwizard.setup.Environment;

import javax.validation.constraints.NotNull;

public class ConsulClientConfig {

    private static final int DEFAULT_PORT = 8500;

    @JsonProperty("agent")
    @NotNull
    private HostAndPort agent = HostAndPort.fromParts("localhost", DEFAULT_PORT);


    public Consul build(Environment env) {
        return Consul.newClient(agent.getHostText(), agent.getPortOrDefault(DEFAULT_PORT));
    }

}
