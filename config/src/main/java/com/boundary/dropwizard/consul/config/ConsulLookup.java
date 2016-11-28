package com.boundary.dropwizard.consul.config;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ImmutableCatalogOptions;
import com.orbitz.consul.option.QueryOptions;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.JtwigFunction;
import org.jtwig.functions.SimpleJtwigFunction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ConsulLookup extends ForwardingMap<String, Object> {

    private final Map<String, Object> delegate = Maps.newHashMap();
    private final Consul consul = Consul.newClient();

    public ConsulLookup() {
        delegate.put("subTwo", "foo");
    }

    @Override
    protected Map<String, Object> delegate() {
        return delegate;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        System.out.println("looking for key " + key);

        if (delegate.containsKey(key)) {
            return true;
        }

        if (key != null && key instanceof String) {

            String keyStr = (String) key;
            if (keyStr.startsWith("kv/")) {
                String lookupKey = keyStr.replaceFirst("^kv/", "");
                String lookedup = consul.keyValueClient().getValueAsString(lookupKey).orNull();
                if (lookedup != null) {
                    delegate.put(keyStr, lookedup);
                }
            } else if (keyStr.startsWith("health/service/")) {
                String lookupKey = keyStr.replaceFirst("^health/service/", "");
                ConsulResponse<List<ServiceHealth>> lookedup = consul.healthClient().getAllServiceInstances(lookupKey);
                if (lookedup != null && lookedup.getResponse().size() > 0) {
                    delegate.put(keyStr, lookedup.getResponse());
                }

            }
        }

        return delegate.containsKey(key);
    }

    JtwigFunction health_service() {
        return new SimpleJtwigFunction() {
            @Override
            public String name() {
                return "healthy_services";
            }

            @Override
            public Object execute(FunctionRequest request) {
                // todo TAGS
                request.maximumNumberOfArguments(3);
                request.minimumNumberOfArguments(1);
                Object key = request.getArguments().get(0);
                final HostAndPort defaultHostAndPort;
                if (request.getNumberOfArguments() >= 2) {
                    defaultHostAndPort = HostAndPort.fromString((String)request.getArguments().get(1));
                } else {
                    defaultHostAndPort = null;
                }
                CatalogOptions co = CatalogOptions.BLANK;
                if (request.getNumberOfArguments() >= 3) {
                    String tag = (String) request.getArguments().get(2);
                    co = ImmutableCatalogOptions
                            .builder()
                            .tag(tag)
                            .build();
                }

                List<ServiceHealth> response =
                        consul.healthClient().getHealthyServiceInstances((String) key, co).getResponse();

                List<HostAndPort> entries = response.stream()
                        .map(sh -> {
                            String addr = sh.getService().getAddress().isEmpty() ? sh.getNode().getAddress() : sh.getService().getAddress();
                            int port = getPort(sh.getService(), defaultHostAndPort);
                            return HostAndPort.fromParts(addr, port);
                        })
                        .collect(toList());

                if (entries.isEmpty()) {
                    if (defaultHostAndPort == null) {
                        return ImmutableList.of();
                    }
                    return ImmutableList.of(defaultHostAndPort);
                } else {
                    return entries;
                }
            }

            private int getPort(Service service, HostAndPort defaultHostAndPort) {
                if (service.getPort() > 0) {
                    return service.getPort();
                } else if (defaultHostAndPort != null && defaultHostAndPort.hasPort()) {
                    return defaultHostAndPort.getPort();
                } else {
                    return 0;
                }
            }
        };
    }

    JtwigFunction kv() {
        return new SimpleJtwigFunction() {
            @Override
            public String name() {
                return "kv";
            }

            @Override
            public Object execute(FunctionRequest request) {
                request.maximumNumberOfArguments(2);
                request.minimumNumberOfArguments(1);
                Object key = request.getArguments().get(0);
                Optional<String> response = consul.keyValueClient().getValueAsString((String) key);
                if (request.getNumberOfArguments() == 1) {
                    return response.orNull();
                } else {
                    return response.or(String.valueOf(request.getArguments().get(1)));
                }
            }
        };
    }

/*    @Override
    public Object get(@Nullable Object key) {


        Object existing = delegate.get(key);
        if (existing != null) {
            return existing;
        }




        return delegate.get(key);
    }*/
}
