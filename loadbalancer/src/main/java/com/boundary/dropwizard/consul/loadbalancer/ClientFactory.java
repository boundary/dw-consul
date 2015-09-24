package com.boundary.dropwizard.consul.loadbalancer;

import com.orbitz.consul.model.health.ServiceHealth;

@FunctionalInterface
public interface ClientFactory<CLIENT> {
    CLIENT create(ServiceHealth serviceHealth);
}
