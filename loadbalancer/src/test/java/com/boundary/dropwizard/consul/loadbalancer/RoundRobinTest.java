package com.boundary.dropwizard.consul.loadbalancer;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.health.ImmutableNode;
import com.orbitz.consul.model.health.ImmutableService;
import com.orbitz.consul.model.health.ImmutableServiceHealth;
import com.orbitz.consul.model.health.Node;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import org.jooq.lambda.Seq;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class RoundRobinTest {

    private static final String SERVICE_NAME = "DA-SERVICE-YO";
    public static final int PORT = 4040;
    private ServiceHealthCache cache = mock(ServiceHealthCache.class);
    private RoundRobin<FakeClient> loadBalancer;


    private static class FakeClient {
        private final HostAndPort hap;
        private FakeClient(HostAndPort hap) {
            this.hap = hap;
        }
    }
    @Test
    public void testServiceListener() {

        AtomicLong created = new AtomicLong();
        loadBalancer = new RoundRobin<>(cache, hap -> {
            created.incrementAndGet();
            return new FakeClient(hap);
        });

        ImmutableList<String> initialNodes = ImmutableList.of("4.5.6.7", "5.6.7.8");
        loadBalancer.notify(serviceMap(initialNodes));

        List<FakeClient> createdClients = new ArrayList<>();

        IntStream.range(0, initialNodes.size() * 4).forEach(i -> {
            FakeClient client = loadBalancer.getClient();
            if (i < initialNodes.size()) {
                createdClients.add(client);
            }
            assertEquals(createdClients.get(i % initialNodes.size()), client);
            assertEquals(Math.min(i + 1, initialNodes.size()), loadBalancer.getClientCache().size());

        });
        assertEquals(2, created.get());

        ImmutableList<String>  secondaryNodes = ImmutableList.<String>builder().addAll(initialNodes).add("6.7.8.9").build();

        List<FakeClient> secondaryCreatedClients = new ArrayList<>();

        loadBalancer.notify(serviceMap(secondaryNodes));
        IntStream.range(0, secondaryNodes.size() * 4).forEach(i -> {
            FakeClient client = loadBalancer.getClient();
            if (i < secondaryNodes.size()) {
                secondaryCreatedClients.add(client);
            }
            assertEquals(secondaryCreatedClients.get(i % secondaryNodes.size()), client);
        });
        assertEquals(3, created.get());
        assertEquals(3, loadBalancer.getClientCache().size());

        loadBalancer.notify(serviceMap(initialNodes));
        IntStream.range(0, initialNodes.size() * 4).forEach(i -> {
            FakeClient client = loadBalancer.getClient();
            assertEquals(createdClients.get(i % initialNodes.size()), client);
        });


        assertEquals(3, created.get());
        assertEquals(2, loadBalancer.getClientCache().size());
    }

    private Map<HostAndPort, ServiceHealth> serviceMap(ImmutableList<String> nodes) {
        return Seq.seq(nodes)
                .toMap(s -> HostAndPort.fromParts(s, PORT), this::serviceHealth);

    };

    private ServiceHealth serviceHealth(String address) {

        Node n = ImmutableNode.fromAllAttributes(address, address);
        Service s = ImmutableService
                .builder()
                .address(address)
                .service(SERVICE_NAME)
                .port(PORT)
                .id(SERVICE_NAME + "_" + "service")
                .build();

        return ImmutableServiceHealth
                .builder()
                .node(n)
                .service(s)
                .build();
    }

}