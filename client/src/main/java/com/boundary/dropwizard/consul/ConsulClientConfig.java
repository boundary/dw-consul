package com.boundary.dropwizard.consul;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import io.dropwizard.util.Duration;

import javax.validation.constraints.NotNull;

public class ConsulClientConfig {

    private static final int DEFAULT_PORT = 8500;

    @JsonProperty("agent")
    @NotNull
    private HostAndPort agent = HostAndPort.fromParts("localhost", DEFAULT_PORT);


    /**
     * This is deliberately set long
     * to support watches. If you want to
     * set it shorter, be aware that
     * it may cause timeout errors if you are
     * using anything that leverages
     * <a href="https://github.com/OrbitzWorldwide/consul-client/blob/master/src/main/java/com/orbitz/consul/cache/ConsulCache.java">ConsulCache</a>
     * such as the provided loadbalancer
     */
    @JsonProperty("readTimeout")
    @NotNull
    private Duration readTimeout = Duration.seconds(45);

    public Consul build() {
        return Consul.builder()
                .withHostAndPort(agent)
                .withReadTimeoutMillis(readTimeout.toMilliseconds())
                .build();
    }

}
