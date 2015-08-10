package com.boundary.dropwizard.consul.registration;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConsulRegistrationConfigTest {

    private ConsulRegistrationConfig config = new ConsulRegistrationConfig();

    private final Map<String, Integer> services = ImmutableMap.of(
            "service", 8080
            , "admin", 8081
    );

    @Before
    public void setUp() {
        config.setServices(services);
    }

    @Test
    public void testGetServicesJmxFalse() {
        config.setRegisterJmx(false);
        assertThat(config.getServices(), is(services));
    }


    @Test
    public void testGetServicesJmxTrueNotEnabled() {
        config.setRegisterJmx(true);
        System.clearProperty("com.sun.management.jmxremote.port");

        assertThat(config.getServices(), is(services));

    }

    @Test
    public void testGetServicesJmxTrueEnabled() {

        System.setProperty("com.sun.management.jmxremote.port", "8082");

        Map<String, Integer> withJmxMap = ImmutableMap.<String, Integer>builder().putAll(services).put("jmx", 8082).build();

        config.setRegisterJmx(true);
        assertThat(config.getServices(), is(withJmxMap));

    }
}