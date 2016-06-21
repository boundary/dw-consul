package com.boundary.dropwizard.consul.registration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.model.agent.Registration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.ValidationMethod;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ConsulRegistrationConfig {

    /**
     * Our service name. Will default to
     * application name if not set.
     */
    @NotNull
    @JsonProperty
    @UnwrapValidatedValue(false)
    private Optional<String> serviceName = Optional.empty();

    @NotNull
    @JsonProperty
    private String tagSeparator = "_";

    @NotEmpty
    @JsonProperty
    private List<ServiceConfig> services;

    public void register(Environment environment, AgentClient client) {

        ConsulServiceRegistration csr = new ConsulServiceRegistration(
                client,
                getServiceName().orElse(environment.getName()),
                getTagSeparator(),
                getServices()
        );

        environment.lifecycle().manage(csr);

    }


    public Optional<String> getServiceName() {
        return serviceName;
    }

    public void setServiceName(Optional<String> serviceName) {
        this.serviceName = serviceName;
    }

    public String getTagSeparator() {
        return tagSeparator;
    }

    public void setTagSeparator(String tagSeparator) {
        this.tagSeparator = tagSeparator;
    }

    public void setServices(List<ServiceConfig> services) {
        this.services = services;
    }

    public List<ServiceConfig> getServices() {
        return services;
    }

    public static class ServiceConfig {

        /**
         * If you want health checks to be run for this service,
         * provide the url here, eg:
         *
         * "http://localhost:8081/healthcheck"
         *
         * Generally this should be the
         * main application service, using the
         * admin port to access the standard dropwizard
         * healthcheck URL.
         *
         * No health checks will be associated with the service if left empty.
         */
        @NotEmpty
        @JsonProperty
        @UnwrapValidatedValue(true)
        private Optional<String> healthCheckUrl = Optional.empty();

        /**
         * Health check interval. Is only used if healthCheckUrl is
         * not empty
         */
        @MinDuration(value = 1, unit = TimeUnit.SECONDS)
        @NotNull
        @UnwrapValidatedValue(true)
        @JsonProperty
        private Optional<Duration> checkInterval = Optional.of(Duration.seconds(10));

        /**
         * The tag to register the service under.
         *
         * Usually should map to the application
         * connector name - eg "service", "admin", "jmx", etc
         */
        @JsonProperty
        @NotEmpty
        private String serviceTag;

        /**
         * The service port numbers
         */
        @JsonProperty
        @NotNull
        private Integer port;

        @ValidationMethod(message="If healthCheckUrl is specified, checkInterval must also be specified")
        @JsonIgnore
        public boolean hasCorrectHealthConfig() {
            if (healthCheckUrl.isPresent()) {
                return  checkInterval.isPresent();
            }
            return true;
        }

        /**
         * Build the regCheck for this service, or null
         * if not present.
         */
        public Registration.RegCheck getHealthCheck() {
            if (getHealthCheckUrl().isPresent()) {
                return Registration.RegCheck.http(getHealthCheckUrl().get(), getCheckInterval().get().toSeconds());
            } else {
                return null;
            }
        }

        public Optional<String> getHealthCheckUrl() {
            return healthCheckUrl;
        }

        public void setHealthCheckUrl(Optional<String> healthCheckUrl) {
            this.healthCheckUrl = healthCheckUrl;
        }

        public Optional<Duration> getCheckInterval() {
            return checkInterval;
        }

        public void setCheckInterval(Optional<Duration> checkInterval) {
            this.checkInterval = checkInterval;
        }

        public String getServiceTag() {
            return serviceTag;
        }

        public void setServiceTag(String serviceTag) {
            this.serviceTag = serviceTag;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

    }
}
