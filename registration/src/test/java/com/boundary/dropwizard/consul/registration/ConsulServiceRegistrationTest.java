package com.boundary.dropwizard.consul.registration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.model.agent.Registration;
import io.dropwizard.util.Duration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ConsulServiceRegistrationTest {


    public static final String MY_TEST_SERVICE = "my-test-service";
    private ConsulServiceRegistration serviceRegistration;
    private AgentClient client = mock(AgentClient.class);
    private ConsulRegistrationConfig config = new ConsulRegistrationConfig();

    private List<ConsulRegistrationConfig.ServiceConfig> services;

    @Before
    public void setUp() throws Exception {

        ConsulRegistrationConfig.ServiceConfig service = new ConsulRegistrationConfig.ServiceConfig();
        service.setPort(8080);
        service.setServiceTag("service");
        service.setHealthCheckUrl(Optional.of("http://localhost:8081/healthcheck"));
        service.setCheckInterval(Optional.of(Duration.seconds(10)));
        ConsulRegistrationConfig.ServiceConfig adminService = new ConsulRegistrationConfig.ServiceConfig();
        adminService.setPort(8081);
        adminService.setServiceTag("admin");

        services = ImmutableList.of(service, adminService);

        config.setServices(services);
        serviceRegistration = new ConsulServiceRegistration(
                client,
                MY_TEST_SERVICE,
                config.getTagSeparator(),
                config.getServices()
        );
    }

    @Test
    public void testRegistration() throws Exception {

        serviceRegistration.start();


        config.getServices()
                .forEach((serviceConfig) -> {
                    final Registration.RegCheck check;
                    if (serviceConfig.getServiceTag().equals("service")) {
                        check = Registration.RegCheck.http("http://localhost:8081/healthcheck", 10);
                    } else {
                        check = null;
                    }
                    verify(client, times(1))
                            .register(
                                    serviceConfig.getPort(),
                                    check,
                                    MY_TEST_SERVICE,
                                    MY_TEST_SERVICE + config.getTagSeparator() + serviceConfig.getServiceTag(),
                                    serviceConfig.getServiceTag());

                });

        serviceRegistration.stop();
        config.getServices().forEach(serviceConfig -> {
            verify(client, times(1)).deregister(MY_TEST_SERVICE + config.getTagSeparator() + serviceConfig.getServiceTag());
        });
        verifyNoMoreInteractions(client);
    }
}