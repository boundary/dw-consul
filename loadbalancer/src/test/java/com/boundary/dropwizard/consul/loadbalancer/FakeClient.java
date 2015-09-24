package com.boundary.dropwizard.consul.loadbalancer;

import com.orbitz.consul.model.health.ServiceHealth;

public class FakeClient {
    public final ServiceHealth serviceHealth;
    public FakeClient(ServiceHealth serviceHealth) {
        this.serviceHealth = serviceHealth;
    }
}
