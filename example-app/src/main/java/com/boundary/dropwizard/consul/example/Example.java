package com.boundary.dropwizard.consul.example;

import com.boundary.dropwizard.consul.config.ConsulLookup;
import com.boundary.dropwizard.consul.config.TemplateSourceProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.orbitz.consul.Consul;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.dropwizard.Application;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example extends Application<ExampleConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Example.class);

    @Override
    public void initialize(Bootstrap<ExampleConfig> bootstrap) {
        ConfigurationSourceProvider original = bootstrap.getConfigurationSourceProvider();

        bootstrap.setConfigurationSourceProvider(new TemplateSourceProvider(original, new ConsulLookup()));
        super.initialize(bootstrap);
    }

    @Override
    public void run(ExampleConfig configuration, Environment environment) throws Exception {

        Consul consul = configuration.getConsul().buildAndRegisterServices(environment);

        environment.jersey().register(new ExampleResource(consul.keyValueClient()));

        LOGGER.info("templated config: {}", configuration.getTemplatedConfig());

    }

    public static void main(String[] args) throws Exception {
        new Example().run(args);
    }
}
