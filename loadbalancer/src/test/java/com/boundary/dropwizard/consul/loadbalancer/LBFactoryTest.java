package com.boundary.dropwizard.consul.loadbalancer;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.io.Resources;
import com.orbitz.consul.HealthClient;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.UrlConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThat;

public class LBFactoryTest {


    @Mock
    private HealthClient healthClient;

    private final ClientFactory<FakeClient> factory = FakeClient::new;

    private Environment environment;
    private ObjectMapper mapper;
    private Validator validator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mapper = Jackson.newObjectMapper().registerModule(new Jdk8Module());
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.environment = new Environment("dw-consul-test", mapper,
                validator,
                new MetricRegistry(),
                ClassLoader.getSystemClassLoader());
    }

    @Test
    public void testRoundRobin() throws Exception {

        Config config = loadConfig("round-robin-config.yml");
        LoadBalancer<FakeClient> balancer = config.lb.build(environment, healthClient, factory);
        assertThat(balancer).isInstanceOf(RoundRobin.class);
    }

    @Test
    public void testRoundRobinDefault() throws Exception {
        Config config = loadConfig("round-robin-default-config.yml");
        LoadBalancer<FakeClient> balancer = config.lb.build(environment, healthClient, factory);
        assertThat(balancer).isInstanceOf(RoundRobin.class);
    }

    public static class Config {
        @JsonProperty
        @NotNull
        @Valid
        LBFactory lb;
    }


    private Config loadConfig(String filename) throws Exception {

        final DefaultConfigurationFactoryFactory<Config> configurationFactoryFactory =
                new DefaultConfigurationFactoryFactory<>();

        final ConfigurationFactory<Config> configurationFactory =
                configurationFactoryFactory.create(Config.class, validator, mapper, "test");

        final String resource = Resources.getResource(filename).toExternalForm();
        return configurationFactory.build(new UrlConfigurationSourceProvider(), resource);
    }

}
