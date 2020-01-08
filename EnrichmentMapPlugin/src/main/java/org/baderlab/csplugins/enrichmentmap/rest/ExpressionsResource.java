package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.rest.response.ExpressionDataResponse;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

import com.google.inject.Inject;
import com.google.inject.Provider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags="Apps: EnrichmentMap")
@Path("/enrichmentmap/expressions")
public class ExpressionsResource {

	@Inject private ResourceUtil resourceUtil;
	@Inject private CyNetworkManager networkManager;
	@Inject private Provider<HeatMapMediator> heatMapMediatorProvider;
	
	
	@GET
	@ApiOperation(value="Get expression data for a given network.", response=ExpressionDataResponse.class)
	@Path("/{network}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExpressionDataForNetwork(
			@ApiParam(value="Network name or SUID") @PathParam("network") String network
	) {
		return
			resourceUtil.getEnrichmentMap(network)
			.map(ExpressionDataResponse::new)
			.map(data -> Response.ok(data).build())
			.orElse(Response.status(Status.NOT_FOUND).build());
	}
	
	
	@GET
	@ApiOperation(value="Get expression data for a given node (gene set).", response=ExpressionDataResponse.class)
	@Path("/{network}/{node}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExpressionDataForNode(
			@ApiParam(value="Network name or SUID") @PathParam("network") String network,
			@ApiParam(value="Node SUID") @PathParam("node") long nodeID
	) {
		Optional<EnrichmentMap> mapOpt = resourceUtil.getEnrichmentMap(network);
		if(mapOpt.isPresent()) {
			EnrichmentMap map = mapOpt.get();
			String prefix = map.getParams().getAttributePrefix();
			CyNetwork cyNetwork = networkManager.getNetwork(map.getNetworkID());
			CyNode node = cyNetwork.getNode(nodeID);
			if(node != null) {
				CyRow row = cyNetwork.getRow(node);
				List<String> c = Columns.NODE_GENES.get(row, prefix);
				Set<String> genes = new HashSet<>(c);
				ExpressionDataResponse data = new ExpressionDataResponse(map, Optional.of(genes));
				return Response.ok(data).build();
			}
		}
		return Response.status(Status.NOT_FOUND).build();
	}
	
	
	@GET
	@ApiOperation(value="Get expression data currently shown in the heat map panel.", response=ExpressionDataResponse.class)
	@Path("/heatmap")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExpressionDataForHeatMap() {
		HeatMapMediator heatMapMediator = heatMapMediatorProvider.get();
		List<String> genes = heatMapMediator.getGenes();
		EnrichmentMap map  = heatMapMediator.getEnrichmentMap();
		
		if(genes == null || genes.isEmpty() || map == null)
			return Response.status(Status.NOT_FOUND).build();
		
		ExpressionDataResponse response = new ExpressionDataResponse(map, Optional.of(new HashSet<>(genes)));
		return Response.ok(response).build();
	}
	

}
