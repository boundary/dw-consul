package com.boundary.dropwizard.consul.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExampleConfig extends Configuration {

    @JsonProperty
    @Valid
    @NotNull
    private ConsulConfig consul = new ConsulConfig();

    public ConsulConfig getConsul() {
        return consul;
    }
}
