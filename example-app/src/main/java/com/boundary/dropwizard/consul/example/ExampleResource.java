package com.boundary.dropwizard.consul.example;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Produces("application/json")
@Path("/v1/kv")
public class ExampleResource {

    private final KeyValueClient keyValueClient;

    public ExampleResource(KeyValueClient keyValueClient) {
        this.keyValueClient = keyValueClient;
    }

    @Path("/{key}")
    @GET
    public Map<String, List<String>> getKey(@PathParam("key") String key) {
        List<String> values = keyValueClient.getValues(key)
                .stream()
                .map(Value::getValueAsString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        return ImmutableMap.of("key", values);
    }

    @Path("/{key}")
    @PUT
    public boolean putKey(@PathParam("key") String key,
                          String value){
        return keyValueClient.putValue(key, value);
    }
}
