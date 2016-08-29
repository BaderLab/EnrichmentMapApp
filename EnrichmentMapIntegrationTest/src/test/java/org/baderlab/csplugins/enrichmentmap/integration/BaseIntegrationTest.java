package org.baderlab.csplugins.enrichmentmap.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

/**
 * Most of this was copied from the gui-distribution/integration-test project.
 */
@RunWith(PaxExam.class)
public abstract class BaseIntegrationTest extends PaxExamConfiguration {

	@Inject private LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	@Inject private CyNetworkManager networkManager;

	protected File createTempFile(String path, String fileName) throws IOException {
		int dot = fileName.indexOf('.');
		String prefix = fileName.substring(0, dot);
		String suffix = fileName.substring(dot+1);
		File tempFile = File.createTempFile(prefix, suffix);
		InputStream in = getClass().getResourceAsStream(path + prefix + "." + suffix);
		assertNotNull(in);
		Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return tempFile;
	}
	
	protected CyNetwork importNetworkFromFile(String path, String fileName) throws IOException {
		Set<CyNetwork> existingNetworks = networkManager.getNetworkSet();
		// import the network
		File file = createTempFile(path, fileName);
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
		assertTablesEqual(expectedNetwork.getDefaultNodeTable(), actualNetwork.getDefaultNodeTable());
		assertTablesEqual(expectedNetwork.getDefaultEdgeTable(), actualNetwork.getDefaultEdgeTable());
	}
	
	
	public void assertTablesEqual(CyTable expectedTable, CyTable actualTable) {
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
		
		// need to sort both Lists 
		sort(expectedColumns, expectedRows);
		sort(expectedColumns, actualRows);
		
//		System.out.println("Expected Rows:");
//		printList(expectedRows);
//		System.out.println("\nActual Rows:");
//		printList(actualRows);
//		System.out.println();
		
		// Assert that all the values in all the rows are the same
		for(int i = 0; i < expectedRows.size(); i++) {
			for(CyColumn column : expectedColumns) {
				String name = column.getName();
				Class<?> type = column.getType();
				if(!CyNetwork.SUID.equals(name)) {
					Object expectedValue = expectedRows.get(i).get(name, type);
					Object actualValue   = actualRows.get(i).get(name, type);
					String message = "Row: " + i + ", Col: " + name + ",";
					
					if(type.equals(List.class)) { // because CyListImpl doesn't implement equals()
						assertArrayEquals(message, ((List<?>)expectedValue).toArray(), ((List<?>)actualValue).toArray() );
					}
					else {
						assertEquals(message, expectedValue, actualValue);
					}
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





