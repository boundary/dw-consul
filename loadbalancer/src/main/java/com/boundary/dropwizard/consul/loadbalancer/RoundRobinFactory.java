package com.boundary.dropwizard.consul.loadbalancer;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.orbitz.consul.HealthClient;
import io.dropwizard.setup.Environment;

@JsonTypeName("round-robin")
public class RoundRobinFactory extends AbstractLBFactory {

    @Override
    public <CLIENT> LoadBalancer<CLIENT> build(Environment env, HealthClient healthClient, ClientFactory<CLIENT> clientFactory) throws Exception {
        return new RoundRobin<>(buildCache(env, healthClient), clientFactory);
    }
}
