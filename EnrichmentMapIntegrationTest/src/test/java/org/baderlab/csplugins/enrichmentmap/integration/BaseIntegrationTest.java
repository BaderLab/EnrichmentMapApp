package org.baderlab.csplugins.enrichmentmap.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

import com.google.common.collect.ImmutableSet;

/**
 * Most of this was copied from the gui-distribution/integration-test project.
 */
@RunWith(PaxExam.class)
public abstract class BaseIntegrationTest extends PaxExamConfiguration {

	@Inject private LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	@Inject private CyNetworkManager networkManager;

	
	
	protected CyNetwork importNetworkFromFile(String path, String fileName) throws IOException {
		Set<CyNetwork> existingNetworks = networkManager.getNetworkSet();
		// import the network
		File file = TestUtils.createTempFile(path, fileName);
		TaskIterator taskIterator = loadNetworkFileTaskFactory.createTaskIterator(file);
		SerialTestTaskManager taskManager = new SerialTestTaskManager();
		taskManager.execute(taskIterator);
		// get a reference to the imported network
		Set<CyNetwork> networksAfterImport = networkManager.getNetworkSet();
		networksAfterImport.removeAll(existingNetworks);
		assertEquals(1, networksAfterImport.size());
		return networksAfterImport.iterator().next();
	}
	
	/**
	 * If there is only one network it will be returned, otherwise the test will fail.
	 */
	protected CyNetwork assertAndGetOnlyNetwork() {
		Set<CyNetwork> networks = networkManager.getNetworkSet();
		assertEquals(1, networks.size());
		return networks.iterator().next();
	}
	
	
	public void assertNetworksEqual(CyNetwork expectedNetwork, CyNetwork actualNetwork) {
		assertTablesEqual(expectedNetwork.getDefaultNodeTable(), actualNetwork.getDefaultNodeTable(), false);
		assertTablesEqual(expectedNetwork.getDefaultEdgeTable(), actualNetwork.getDefaultEdgeTable(), true);
	}
	
	
	public void assertTablesEqual(CyTable expectedTable, CyTable actualTable, boolean edgeTable) {
		List<CyColumn> expectedColumns = new ArrayList<>(expectedTable.getColumns());
		
		// Test columns are the same
		for(CyColumn expectedColumn : expectedColumns) {
			String name = expectedColumn.getName();
			if(!CyNetwork.SUID.equals(name)) {
				CyColumn actualColumn = actualTable.getColumn(name);
				assertNotNull("Column '" + name + "' does not exist", actualColumn);
				assertEquals ("Column '" + name + "' is wrong type", expectedColumn.getType(), actualColumn.getType());
			}
		}
		
		List<CyRow> expectedRows = expectedTable.getAllRows();
		List<CyRow> actualRows   = actualTable.getAllRows();
		
		assertEquals("Tables are not the same size", expectedRows.size(), actualRows.size());
		
		if(edgeTable) {
			assertEdgeTableRowsEqual(expectedColumns, expectedRows, actualRows);
		}
		else { // node table or other table
			// need to sort both Lists 
			sort(expectedColumns, expectedRows);
			sort(expectedColumns, actualRows);
			
			for(int i = 0; i < expectedRows.size(); i++) {
				CyRow expectedRow = expectedRows.get(i);
				CyRow actualRow   = actualRows.get(i);
				assertAttributesEqual(expectedColumns, expectedRow, actualRow);
			}
		}
	}

	/**
	 * For edges we need to ignore the directionality.
	 */
	private void assertEdgeTableRowsEqual(List<CyColumn> expectedColumns, List<CyRow> expectedRows, List<CyRow> actualRows) {
		Set<String> columnsToIgnore = ImmutableSet.of(CyNetwork.NAME, CyRootNetwork.SHARED_NAME);
		List<CyColumn> columnsToTest = expectedColumns.stream().filter(c->!columnsToIgnore.contains(c.getName())).collect(Collectors.toList());
		
		Map<SimilarityKey, CyRow> expectedRowsByKey = new HashMap<>();
		for(CyRow row : expectedRows) {
			SimilarityKey key = SimilarityKey.parse(row.get(CyNetwork.NAME, String.class));
			expectedRowsByKey.put(key, row);
		}
		
		for(CyRow actual : actualRows) {
			SimilarityKey key = SimilarityKey.parse(actual.get(CyNetwork.NAME, String.class));
			CyRow expected = expectedRowsByKey.remove(key);
			assertNotNull(key.toString(), expected);
			assertAttributesEqual(columnsToTest, expected, actual);
			
		}
		assertTrue(expectedRowsByKey.isEmpty());
	}
	
	
	private void assertAttributesEqual(List<CyColumn> columnsToTest, CyRow expected, CyRow actual) {
		for(CyColumn column : columnsToTest) {
			String name = column.getName();
			Class<?> type = column.getType();
			if(!CyNetwork.SUID.equals(name)) {
				Object expectedValue = expected.get(name, type);
				Object actualValue   = actual.get(name, type);
				String message = "Col: " + name + ",";
				
				if(type.equals(List.class)) { // because CyListImpl doesn't implement equals()
					assertArrayEquals(message, ((List<?>)expectedValue).toArray(), ((List<?>)actualValue).toArray() );
				}
				else {
					assertEquals(message, expectedValue, actualValue);
				}
			}
		}
	}
	
	
	/**
	 * I don't need to actually sort the rows in a meaningful way, just make it deterministic.
	 */
	public void sort(List<CyColumn> columns, List<CyRow> rows) {
		Comparator<CyRow> rowComparator = null;
		for(CyColumn column : columns) {
			if(!CyNetwork.SUID.equals(column.getName())) {
				Comparator<CyRow> c = Comparator.comparing((CyRow row) -> row.get(column.getName(), column.getType()).toString());
				rowComparator = rowComparator == null ? c : rowComparator.thenComparing(c);
			}
		}
		Collections.sort(rows, rowComparator);
	}
	
	/**
	 * This method exists because:
	 * - The JUnit 4.11 version of assertArrayEquals actually has a bug in it.
	 * - Need to sort the arrays.
	 */
	private static void assertArrayEquals(String message, Object[] expected, Object[] actual) {
		Arrays.sort(expected);
		Arrays.sort(actual);
		assertTrue(message, Arrays.equals(expected, actual));
	}
	
	
	public void printList(List<CyRow> xs) {
		xs.stream().limit(20).forEach(row -> System.out.println(row.getAllValues()));
	}
	
}





