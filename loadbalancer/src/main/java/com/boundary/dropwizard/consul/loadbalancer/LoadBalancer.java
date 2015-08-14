package com.boundary.dropwizard.consul.loadbalancer;

@FunctionalInterface
public interface LoadBalancer<CLIENT> {
    CLIENT getClient();
}
