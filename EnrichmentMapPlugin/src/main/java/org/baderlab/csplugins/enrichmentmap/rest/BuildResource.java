package org.baderlab.csplugins.enrichmentmap.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.FilterTunables;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags="Apps: EnrichmentMap")
@Path("/enrichmentmap/build")
public class BuildResource {
	
	private static final String 
		NAME = "name",
		GENES = "genes",
		PVALUE = "pvalue",
		QVALUE = "qvalue",
		NES = "nes",
		DESC = "description";
	
	
	@Inject private SynchronousTaskManager<?> taskManager;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private CyTableFactory tableFactory;
	@Inject private Provider<LegacySupport> legacySupport;
	
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create an EnrichmentMap network.",
		notes=""
	)
	public Response createEnrichmentMap(
			@ApiParam(value="Body JSON data.") InputStream is
	) {
		EMJsonData emJson;
		try(Reader reader = new InputStreamReader(is)) {
			Gson gson = new GsonBuilder().create();
			emJson = gson.fromJson(reader, EMJsonData.class);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		CyTable enrichmentTable = createEnrichmentTable();
		fillEnrichmentTable(enrichmentTable, emJson.enrichments);
		
		TableParameters tableParams = new TableParameters(enrichmentTable, NAME, GENES, PVALUE, QVALUE, NES, DESC, null);
		
		DataSetParameters dsParams = new DataSetParameters("DataSet1", tableParams, null);
		List<DataSetParameters> dataSets = Collections.singletonList(dsParams);
		
		FilterTunables filter = emJson.filter;
		filter.setLegacySupport(legacySupport.get());
		
		EMCreationParameters creationParams = filter.getCreationParameters();
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(creationParams, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator();
		taskManager.execute(tasks);
		
		return Response.ok().build();
	}
	
	
	private CyTable createEnrichmentTable() {
		CyTable enrichmentTable = tableFactory.createTable("tempEnrichmentEM", "pk", Long.class, false, false);
		enrichmentTable.createColumn(NAME, String.class, false);
		enrichmentTable.createListColumn(GENES, String.class, false);
		enrichmentTable.createColumn(PVALUE, Double.class, false);
		enrichmentTable.createColumn(QVALUE, Double.class, false);
		enrichmentTable.createColumn(NES, Double.class, false);
		enrichmentTable.createColumn(DESC, String.class, false);
		return enrichmentTable;
	}
	
	private void fillEnrichmentTable(CyTable table, List<EnrichmentEntry> enrichments) {
		long i = 0;
		for(EnrichmentEntry enrichment : enrichments) {
			CyRow row = table.getRow(i++);
			row.set(NAME, enrichment.name);
			row.set(GENES, enrichment.genes);
			row.set(PVALUE, enrichment.pvalue);
			row.set(QVALUE, enrichment.qvalue);
			row.set(NES, enrichment.nes);
			row.set(DESC, enrichment.description);
		}
	}
}
