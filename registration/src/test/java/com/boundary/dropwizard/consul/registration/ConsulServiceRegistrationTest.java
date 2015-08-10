package com.boundary.dropwizard.consul.registration;

import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.model.agent.Registration;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ConsulServiceRegistrationTest {


    public static final String MY_TEST_SERVICE = "my-test-service";
    private ConsulServiceRegistration serviceRegistration;
    private AgentClient client = mock(AgentClient.class);
    private ConsulRegistrationConfig config = new ConsulRegistrationConfig();

    private final Map<String, Integer> services = ImmutableMap.of(
            "service", 8080
            , "admin", 8081
    );

    @Before
    public void setUp() throws Exception {

        config.setServices(services);
        serviceRegistration = new ConsulServiceRegistration(
                client,
                MY_TEST_SERVICE,
                config.getHealthConnectorName(),
                config.getHealthUrl(),
                config.getCheckInterval(),
                config.getTagSeparator(),
                config.getServices()
        );
    }

    @Test
    public void testRegistration() throws Exception {

        serviceRegistration.start();

        final Registration.RegCheck check =
                Registration.RegCheck.http(
                        String.format(config.getHealthUrl(), config.getServices().get(config.getHealthConnectorName())), config.getCheckInterval().toSeconds());

        config.getServices().forEach((tag, port) -> {
            verify(client, times(1)).register(port, check, MY_TEST_SERVICE, MY_TEST_SERVICE + config.getTagSeparator() + tag, tag);

        });

        serviceRegistration.stop();
        config.getServices().forEach((tag, port) -> {
            verify(client, times(1)).deregister(MY_TEST_SERVICE + config.getTagSeparator() + tag);

        });
        verifyNoMoreInteractions(client);
    }
}