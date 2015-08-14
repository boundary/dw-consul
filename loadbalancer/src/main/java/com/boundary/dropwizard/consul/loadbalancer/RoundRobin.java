package com.boundary.dropwizard.consul.loadbalancer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.health.ServiceHealth;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Bridges consul {@link ServiceHealth} entries with CLIENT instances,
 * providing a round-robin client supplier where clients are lazily created and removed when
 * not needed.
 */
public class RoundRobin<CLIENT> implements ConsulCache.Listener<ServiceHealth>, LoadBalancer<CLIENT> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RoundRobin.class);

    private final Function<String, CLIENT> clientFactory;
    private final ConcurrentMap<String, CLIENT> clientCache = Maps.newConcurrentMap();
    private Iterator<CLIENT> clientIterator = Collections.emptyIterator();

    /**
     * Creates the {@link LoadBalancer} impl
     *
     * @param healthCache a configured {@link ServiceHealthCache} for your service entries
     * @param clientFactory function to create CLIENT instances
     */
    public RoundRobin(ServiceHealthCache healthCache,
                      Function<String, CLIENT> clientFactory) {
        this.clientFactory = clientFactory;
        healthCache.addListener(this);
    }

    @Override
    public void notify(Map<String, ServiceHealth> newValues) {
        synchronized (this) {
            // clear entries if needed
            Sets.difference(clientCache.keySet(), newValues.keySet())
                    .forEach(clientCache::remove);

            // setup a new iterator
            clientIterator = Seq.seq(newValues).cycle()
                    .map(this::createAndStore)
                    .iterator();
        }
    }

    private CLIENT createAndStore(Tuple2<String, ServiceHealth> entry) {
        return clientCache.computeIfAbsent(entry.v1(), k -> {
            try {
                return clientFactory.apply(entry.v1());
            } catch (Exception e) {
                LOGGER.error("Error creating a client", e);
                return null;
            }
        });
    }

    @Override
    public CLIENT getClient() {
        return clientIterator.hasNext() ? clientIterator.next() : null;
    }

    @VisibleForTesting
    protected ConcurrentMap<String, CLIENT> getClientCache() {
        return clientCache;
    }
}
