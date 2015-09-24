package com.boundary.dropwizard.consul.loadbalancer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.health.ServiceHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Bridges consul {@link ServiceHealth} entries with CLIENT instances,
 * providing a round-robin client supplier where clients are lazily created and removed when
 * not needed.
 */
public class RoundRobin<CLIENT> implements ConsulCache.Listener<HostAndPort, ServiceHealth>, LoadBalancer<CLIENT> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RoundRobin.class);

    private final ClientFactory<CLIENT> clientFactory;
    private final ConcurrentMap<HostAndPort, CLIENT> clientCache = Maps.newConcurrentMap();
    private ImmutableList<CLIENT> currentList = ImmutableList.of();
    private volatile int idx = 0;

    /**
     * Creates the {@link LoadBalancer} impl
     *
     * @param healthCache   a configured {@link ServiceHealthCache} for your service entries
     * @param clientFactory function to create CLIENT instances
     */
    public RoundRobin(ServiceHealthCache healthCache,
                      ClientFactory<CLIENT> clientFactory) {
        this.clientFactory = clientFactory;
        healthCache.addListener(this);
    }

    @Override
    public void notify(Map<HostAndPort, ServiceHealth> newValues) {

        Sets.SetView<HostAndPort> toRemove = Sets.difference(clientCache.keySet(), newValues.keySet());

        newValues.forEach(this::createAndStore);

        if (!toRemove.isEmpty()) {
            toRemove.forEach(clientCache::remove);
        }
        synchronized (this) {
            currentList = ImmutableList.copyOf(clientCache.values());
            idx=0;
        }
    }

    private void createAndStore(HostAndPort hostAndPort, ServiceHealth serviceHealth) {
        clientCache.computeIfAbsent(hostAndPort, k -> {
            try {
                return clientFactory.create(serviceHealth);
            } catch (Exception e) {
                LOGGER.error("Error creating a client", e);
                return null;
            }
        });
    }

    @Override
    public CLIENT getClient() {
        if (size() == 0) {
            LOGGER.warn("no clients available");
            return null;
        }
        return currentList.get(++idx % size());
    }

    private int size() {
        return currentList.size();
    }

    @VisibleForTesting
    protected ConcurrentMap<HostAndPort, CLIENT> getClientCache() {
        return clientCache;
    }
}
