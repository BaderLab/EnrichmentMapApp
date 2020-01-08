package org.baderlab.csplugins.enrichmentmap.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.io.ModelSerializer;

import com.google.inject.Inject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags="Apps: EnrichmentMap")
@Path("/enrichmentmap/model")
public class ModelResource {
	
	@Inject private ResourceUtil resourceUtil;
	
	@GET
	@ApiOperation(value="Get enrichment map model data for a given network.", response=EnrichmentMap.class)
	@Path("/{network}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getModelData(
			@ApiParam(value="Network name or SUID") @PathParam("network") String network
	) {
		return
			resourceUtil.getEnrichmentMap(network)
			.map(this::getEnrichmentMapJSON)
			.map(data -> Response.ok(data).build())
			.orElse(Response.status(Status.NOT_FOUND).build());
	}
	
	
	private String getEnrichmentMapJSON(EnrichmentMap map) {
		// Don't rely on the auto json serialization because ModelSerializer needs to customize the GSON serializer.
		return ModelSerializer.serialize(map, true);
	}
	
}
