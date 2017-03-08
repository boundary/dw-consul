package com.boundary.dropwizard.consul.loadbalancer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.orbitz.consul.HealthClient;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.setup.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = RoundRobinFactory.class)
public interface LBFactory extends Discoverable {
    <CLIENT> LoadBalancer<CLIENT> build(Environment env, HealthClient healthClient, ClientFactory<CLIENT> clientFactory)
            throws Exception;

    <CLIENT> LoadBalancer<CLIENT> build(HealthClient healthClient, ClientFactory<CLIENT> clientFactory) throws Exception;
}
