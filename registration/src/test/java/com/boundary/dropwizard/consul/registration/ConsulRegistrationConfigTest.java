package com.boundary.dropwizard.consul.registration;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConsulRegistrationConfigTest {

    @Test
    public void  serviceConfigRequiresHealthUrl() throws Exception {

        ConsulRegistrationConfig.ServiceConfig service = new ConsulRegistrationConfig.ServiceConfig();

        assertThat(service.hasCorrectHealthConfig(), is(true));
        assertThat(service.getHealthCheckUrl().isPresent(), is(false));

        service.setHealthCheckUrl(Optional.of("http://localhost:8081/healthcheck"));

        assertThat(service.hasCorrectHealthConfig(), is(true));

        service.setCheckInterval(Optional.empty());

        assertThat(service.hasCorrectHealthConfig(), is(false));
    }


}