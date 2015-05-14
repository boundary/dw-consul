package com.boundary.dropwizard.consul.registration;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.model.agent.Registration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.util.Duration;
import jersey.repackaged.com.google.common.collect.Maps;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class ConsulServiceRegistration implements ServerLifecycleListener, Managed {


    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulServiceRegistration.class);
    private final AgentClient agentClient;
    private final String serviceName;
    private final String healthConnector;
    private final String healthConnectorUrl;
    private final Duration checkInterval;
    private final Map<String, Integer> additionalServices;
    private CopyOnWriteArrayList<String> serviceIds = new CopyOnWriteArrayList<>();

    public ConsulServiceRegistration(AgentClient agentClient, String serviceName, String healthConnector, String healthConnectorUrl, Duration checkInterval, Map<String, Integer> additionalServices) {
        this.agentClient = agentClient;
        this.serviceName = serviceName;
        this.healthConnector = healthConnector;
        this.healthConnectorUrl = healthConnectorUrl;
        this.checkInterval = checkInterval;
        this.additionalServices = additionalServices;
    }
    @Override
    public void serverStarted(Server server) {

        Map<String, Integer> connectorPortMap = Maps.newHashMap();


        for (final Connector connector : server.getConnectors()) {

            String name = connector.getName();
            final ServerSocketChannel channel = (ServerSocketChannel) connector
                        .getTransport();

                try {
                    final InetSocketAddress socket = (InetSocketAddress) channel
                            .getLocalAddress();

                    connectorPortMap.put(name, socket.getPort());
                } catch (final Exception e) {
                    LOGGER.error("Unable to register services in consul", e);
                    return;
                }
        }

        register(connectorPortMap);
    }

    private void register(Map<String, Integer> connectorPortMap) {

        Integer healthPort = connectorPortMap.get(healthConnector);
        if (healthPort == null) {
            LOGGER.error("Unable to get health connector info. No registration entries will be made");
            return;
        }


        Registration.Check check = new Registration.Check();
        check.setHttp(String.format(healthConnectorUrl, healthPort));
        check.setInterval(checkInterval.toSeconds() + "s");

        BiConsumer<String, Integer> reg = (id, port) -> register(check, id, port);

        connectorPortMap.forEach(reg);
        additionalServices.forEach(reg);

    }

    private void register(Registration.Check check, String tag, Integer port) {
        String serviceId = serviceName + "-" + tag;
        serviceIds.add(serviceId);
        agentClient.register(port, check, serviceName, serviceId, tag);
    }

    @Override
    public void start() throws Exception {
        // do nothing..
    }

    @Override
    public void stop() throws Exception {
        serviceIds.forEach(agentClient::deregister);
    }
}
