package com.boundary.dropwizard.consul.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class TemplatedConfig {

    @JsonProperty
    @NotNull
    String subOne;


    @JsonProperty
    @NotNull
    String subTwo;

    @JsonProperty
    @NotNull
    @NotEmpty
    List<String> serviceIps;

    @Override
    public String toString() {
        return "TemplatedConfig{" +
                "subOne='" + subOne + '\'' +
                ", subTwo='" + subTwo + '\'' +
                ", serviceIps=" + serviceIps +
                '}';
    }
}
