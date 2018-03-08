package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.baderlab.csplugins.enrichmentmap.LogSilenceRule;
import org.baderlab.csplugins.enrichmentmap.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.commands.TableCommandTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.work.TaskIterator;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
public class TableLoadNetworkTest extends BaseNetworkTest {

	public static class TestModule extends BaseNetworkTest.TestModule { }
		
	private static final String TABLE_NAME = "MyTable";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String PVALUE = "pvalue";
	private static final String GENES = "genes";
	private static final String DESCRIPTION = "description";
	
	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	@Before
	public void createTable(CyTableManager tableManager) {
		TableTestSupport tableTestSupport = new TableTestSupport();
		CyTableFactory tableFactory = tableTestSupport.getTableFactory();
		CyTable table = tableFactory.createTable(TABLE_NAME, ID, Long.class, true, true);
		
		when(tableManager.getAllTables(anyBoolean())).thenReturn(Collections.singleton(table));
		
		table.createColumn(NAME, String.class, true);
		table.createColumn(PVALUE, Double.class, true);
		table.createColumn(DESCRIPTION, String.class, true);
		table.createListColumn(GENES, String.class, true);
		
		CyRow row1 = table.getRow(1L);
		row1.set(NAME, "gs1");
		row1.set(PVALUE, 0.001);
		row1.set(DESCRIPTION, "gs1_d");
		row1.set(GENES, Arrays.asList("A","B","C"));
		
		CyRow row2 = table.getRow(2L);
		row2.set(NAME, "gs2");
		row2.set(PVALUE, 0.001);
		row2.set(DESCRIPTION, "gs2_d");
		row2.set(GENES, Arrays.asList("B","C","D"));
		
		CyRow row3 = table.getRow(3L);
		row3.set(NAME, "gs3");
		row3.set(PVALUE, 0.001);
		row3.set(DESCRIPTION, "gs3_d");
		row3.set(GENES, Arrays.asList("C","D","E"));
		
		CyRow row4 = table.getRow(4L);
		row4.set(NAME, "no_data");
	}
	
	
	@Test
	public void testTableCommand(TableCommandTask command, CyNetworkManager networkManager) {
		command.tableArgs.table = TABLE_NAME;
		command.tableArgs.pvalueColumn = PVALUE;
		command.tableArgs.nameColumn = NAME;
		command.tableArgs.genesColumn = GENES;
		command.tableArgs.descriptionColumn = DESCRIPTION;
		
	   	SerialTestTaskManager testTaskManager = new SerialTestTaskManager();
	   	testTaskManager.ignoreTask(CreateEMViewTask.class);
	   	testTaskManager.execute(new TaskIterator(command));
	   	
	   	CyNetwork network = networkManager.getNetworkSet().iterator().next();
	   	
	   	assertEquals(3, network.getNodeCount());
	   	assertTrue(network.getEdgeCount() > 0);
	   	
	   	CyTable nodeTable = network.getDefaultNodeTable();
	   	
	   	CyRow row1 = nodeTable.getMatchingRows("EM1_Name", "gs1").iterator().next();
	   	assertEquals("gs1", row1.get("EM1_Name", String.class));
		assertEquals((Double)0.001, row1.get("EM1_pvalue (Data Set 1)", Double.class));
		assertEquals("gs1_d", row1.get("EM1_GS_DESCR", String.class));
		assertEquals(Arrays.asList("A","B","C"), row1.getList("EM1_Genes", String.class));
		
		CyRow row2 = nodeTable.getMatchingRows("EM1_Name", "gs2").iterator().next();
	   	assertEquals("gs2", row2.get("EM1_Name", String.class));
		assertEquals((Double)0.001, row2.get("EM1_pvalue (Data Set 1)", Double.class));
		assertEquals("gs2_d", row2.get("EM1_GS_DESCR", String.class));
		assertEquals(Arrays.asList("B","C","D"), row2.getList("EM1_Genes", String.class));
		
		CyRow row3 = nodeTable.getMatchingRows("EM1_Name", "gs3").iterator().next();
	   	assertEquals("gs3", row3.get("EM1_Name", String.class));
		assertEquals((Double)0.001, row3.get("EM1_pvalue (Data Set 1)", Double.class));
		assertEquals("gs3_d", row3.get("EM1_GS_DESCR", String.class));
		assertEquals(Arrays.asList("C","D","E"), row3.getList("EM1_Genes", String.class));
	}
	
}
