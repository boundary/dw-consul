package com.boundary.dropwizard.consul.loadbalancer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @UnwrapValidatedValue(false)
    private Optional<String> serviceTag = Optional.empty();

    @JsonProperty
    @Min(1)
    private int watchSeconds = 30;

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractLBFactory.class);

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

    protected ServiceHealthCache buildCache(Environment env, HealthClient healthClient) throws Exception {
        final QueryOptions catalogOptions;
        if (getServiceTag().isPresent()) {
            catalogOptions =  ImmutableQueryOptions.builder()
                    .addTag(getServiceTag().get())
                    .build();
        } else {
            catalogOptions = QueryOptions.BLANK;
        }

        final ServiceHealthCache cache = ServiceHealthCache.newCache(
                healthClient,
                getServiceName(),
                true,
                catalogOptions,
                getWatchSeconds()
        );

        if (env != null) {
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
                    cache.stop();
                }
            });
        } else {
            try {
                cache.start();
                if (!cache.awaitInitialized(10, TimeUnit.SECONDS)) {
                    LOGGER.error("load balancer init timeout without dropwizard Environment in buildCache");
                    throw new Exception("load balancer init timeout without dropwizard Environment in buildCache");
                }
            } catch (InterruptedException iE) {
                LOGGER.error("caught InterruptedException in buildCache: {}", iE);
                throw iE;
            } catch (Exception e) {
                LOGGER.error("caught Exception in buildCache: {}", e);
                throw e;
            }
        }

        return cache;
    }
}
