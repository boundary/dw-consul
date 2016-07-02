package com.boundary.dropwizard.consul.example;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.orbitz.consul.Consul;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Example extends Application<ExampleConfig> {

    @Override
    public void initialize(Bootstrap<ExampleConfig> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.getObjectMapper().registerModule(new Jdk8Module());
    }

    @Override
    public void run(ExampleConfig configuration, Environment environment) throws Exception {

        Consul consul = configuration.getConsul().buildAndRegisterServices(environment);

        environment.jersey().register(new ExampleResource(consul.keyValueClient()));

    }

    public static void main(String[] args) throws Exception {
        new Example().run(args);
    }
}
