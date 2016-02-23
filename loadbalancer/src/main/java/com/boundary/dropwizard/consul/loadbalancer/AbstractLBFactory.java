package com.boundary.dropwizard.consul.loadbalancer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ImmutableCatalogOptions;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class AbstractLBFactory implements LBFactory {

    @JsonProperty
    @NotBlank
    private String serviceName;

    @JsonProperty
    @NotNull
    private Optional<String> serviceTag = Optional.empty();

    @JsonProperty
    @Min(1)
    private int watchSeconds = 30;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Optional<String> getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(Optional<String> serviceTag) {
        this.serviceTag = serviceTag;
    }

    public int getWatchSeconds() {
        return watchSeconds;
    }

    public void setWatchSeconds(int watchSeconds) {
        this.watchSeconds = watchSeconds;
    }

    protected ServiceHealthCache buildCache(Environment env, HealthClient healthClient) {
        final CatalogOptions catalogOptions;
        if (getServiceTag().isPresent()) {
            catalogOptions =  ImmutableCatalogOptions.builder()
                    .tag(getServiceTag().get())
                    .build();
        } else {
            catalogOptions = CatalogOptions.BLANK;
        }

        final ServiceHealthCache cache = ServiceHealthCache.newCache(
                healthClient,
                getServiceName(),
                true,
                catalogOptions,
                getWatchSeconds()
        );

        env.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                cache.start();
                if (!cache.awaitInitialized(10, TimeUnit.SECONDS)) {
                    throw new Exception("load balancer init timeout");
                }
            }

            @Override
            public void stop() throws Exception {
                // todo add cache.shutdown
            }
        });

        return cache;
    }
}
