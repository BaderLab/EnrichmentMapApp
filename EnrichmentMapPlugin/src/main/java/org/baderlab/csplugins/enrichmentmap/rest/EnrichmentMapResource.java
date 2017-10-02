package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.io.ModelSerializer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import com.google.inject.Inject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags="Apps: EnrichmentMap")
@Path("/enrichmentmap")
public class EnrichmentMapResource {

	@Inject private EnrichmentMapManager emManager;
	@Inject private CyNetworkManager networkManager;
	
	@GET
	@ApiOperation(value="Get enrichment map model data for a given network.", response=EnrichmentMap.class)
	@Path("/model/{network}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getModelData(@ApiParam(value="Network name or SUID") @PathParam("network") String network) {
		return
			getEnrichmentMap(network)
			.map(this::getEnrichmentMapJSON)
			.map(data -> Response.ok(data).build())
			.orElse(Response.status(Status.BAD_REQUEST).build());
	}
	
	
	@GET
	@ApiOperation(value="Get enrichment map model data for a given network.", response=ExpressionDataResponse.class)
	@Path("/expressions/{network}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExpressionData(@ApiParam(value="Network name or SUID") @PathParam("network") String network) {
		return
			getEnrichmentMap(network)
			.map(ExpressionDataResponse::new)
			.map(data -> Response.ok(data).build())
			.orElse(Response.status(Status.BAD_REQUEST).build());
	}
	
	
	private String getEnrichmentMapJSON(EnrichmentMap map) {
		// Don't rely on the auto json serialization because ModelSerializer needs to customize the GSON serializer.
		return ModelSerializer.serialize(map, true);
	}
	
	
	private Optional<EnrichmentMap> getEnrichmentMap(String network) {
		try {
			long suid = Long.parseLong(network);
			return Optional.ofNullable(emManager.getEnrichmentMap(suid));
		} catch(NumberFormatException e) {
			Optional<Long> suid = getNetworkByName(network);
			return suid.map(emManager::getEnrichmentMap);
		}
	}
	
	
	private Optional<Long> getNetworkByName(String name) {
		for(CyNetwork network : networkManager.getNetworkSet()) {
			String netName = network.getRow(network).get(CyNetwork.NAME, String.class);
			if(name.equals(netName)) {
				return Optional.of(network.getSUID());
			}
		}
		return Optional.empty();
	}

}
