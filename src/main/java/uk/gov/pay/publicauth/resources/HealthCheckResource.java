package uk.gov.pay.publicauth.resources;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.setup.Environment;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;

@Path("/")
public class HealthCheckResource {
    public static final String HEALTHCHECK = "healthcheck";
    public static final String HEALTHY = "healthy";

    Environment environment;

    public HealthCheckResource(Environment environment) {
        this.environment = environment;
    }

    @GET
    @Path(HEALTHCHECK)
    @Produces(APPLICATION_JSON)
    public Response healthCheck() throws JsonProcessingException {
        SortedMap<String, HealthCheck.Result> results = environment.healthChecks().runHealthChecks();

        Map<String, Map<String, Boolean>> response = getResponse(results);

        boolean healthy = results.size() == results.values()
                .stream()
                .filter(HealthCheck.Result::isHealthy)
                .count();

        if(healthy) {
            return Response.ok().entity(response).build();
        }
        return status(503).entity(response).build();
    }

    private Map<String, Map<String, Boolean>> getResponse(SortedMap<String, HealthCheck.Result> results) {
        Map<String, Map<String, Boolean>> response = new HashMap<>();
        for (SortedMap.Entry<String, HealthCheck.Result> entry : results.entrySet() ) {
            response.put(entry.getKey(), ImmutableMap.of(HEALTHY, entry.getValue().isHealthy()));
        }
        return response;
    }
}
