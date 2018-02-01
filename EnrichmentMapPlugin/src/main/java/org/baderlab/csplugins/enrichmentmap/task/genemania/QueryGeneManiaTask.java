package org.baderlab.csplugins.enrichmentmap.task.genemania;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class QueryGeneManiaTask extends AbstractTask {
	
	public static final String GENEMANIA_NAMESPACE = "genemania";
	public static final String GENEMANIA_ORGANISMS_COMMAND = "organisms";
	public static final String GENEMANIA_SEARCH_COMMAND = "search";
	
	public static final String EM_NETWORK_SUID = "EM_Network.SUID";
	
	private final Pattern NES_COL_PATTERN = Pattern.compile("EM\\d+_NES.*");
	private final Pattern PVALUE_COL_PATTERN = Pattern.compile("EM\\d+_pvalue.*");
	private final Pattern QVALUE_COL_PATTERN = Pattern.compile("EM\\d+_fdr_qvalue.*");

	@Tunable(description = "Organism:")
	public ListSingleSelection<GeneManiaOrganism> organisms;
	
	private final EnrichmentMap map;
	private final String genes;
	private final Component parentComponent;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	
	public static interface Factory {
		QueryGeneManiaTask create(EnrichmentMap map, List<String> geneList, Component parentComponent);
	}
	
	@Inject
	public QueryGeneManiaTask(@Assisted EnrichmentMap map, @Assisted List<String> geneList, @Assisted Component parentComponent) {
		this.map = map;
		genes = String.join("|", geneList);
		organisms = new ListSingleSelection<>();
		this.parentComponent = parentComponent;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Select an Organism";
	}
	
	public void updatetOrganisms(List<GeneManiaOrganism> orgValues) {
		organisms.setPossibleValues(orgValues);
		
		if (!orgValues.isEmpty())
			organisms.setSelectedValue(orgValues.get(0));
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (organisms.getSelectedValue() != null) {
			tm.setTitle("EnrichmentMap");
			tm.setStatusMessage("Querying GeneMANIA...");
			
			Map<String, Object> args = new HashMap<>();
			args.put("organism", "" + organisms.getSelectedValue().getTaxonomyId());
			args.put("genes", genes);
			args.put("geneLimit", 0); // Do not find more genes
			
			TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
					GENEMANIA_NAMESPACE, GENEMANIA_SEARCH_COMMAND, args, new TaskObserver() {
				
				@Override
				public void taskFinished(ObservableTask task) {
					if (task instanceof ObservableTask) {
						if (((ObservableTask) task).getResultClasses().contains(CyNetwork.class)) {
							CyNetwork maniaNet = ((ObservableTask) task).getResults(CyNetwork.class);
							
							if (maniaNet == null) {
								SwingUtilities.invokeLater(() -> {
									JOptionPane.showMessageDialog(
											parentComponent,
											"The GeneMANIA search returned no results.",
											"No Results",
											JOptionPane.INFORMATION_MESSAGE
									);
								});
							} else {
								// Add EM Network SUID to genemania network's table.
								CyTable tgtNetTable = maniaNet.getDefaultNetworkTable();
								
								if (tgtNetTable.getColumn(EM_NETWORK_SUID) == null)
									tgtNetTable.createColumn(EM_NETWORK_SUID, Long.class, true);
								
								tgtNetTable.getRow(maniaNet.getSUID()).set(EM_NETWORK_SUID, map.getNetworkID());
								
								// Copy some EM columns to genemania's Node table
								CyNetwork emNet = networkManager.getNetwork(map.getNetworkID());
								CyTable srcNodeTable = emNet.getDefaultNodeTable();
								CyTable tgtNodeTable = maniaNet.getDefaultNodeTable();
								Collection<CyColumn> srcNodeColumns = new ArrayList<>(srcNodeTable.getColumns());
								
								for (CyColumn col : srcNodeColumns) {
									String colName = col.getName();

									if (NES_COL_PATTERN.matcher(colName).matches()
											|| PVALUE_COL_PATTERN.matcher(colName).matches()
											|| QVALUE_COL_PATTERN.matcher(colName).matches()) {
										if (tgtNodeTable.getColumn(colName) == null) {
											if (col.getListElementType() == null)
												tgtNodeTable.createColumn(colName, col.getType(), col.isImmutable());
											else
												tgtNodeTable.createListColumn(colName, col.getListElementType(), col.isImmutable());
											
											for (CyRow tgtRow : tgtNodeTable.getAllRows()) {
//												Object pk = tgtRow.get(tgtNodeTable.getPrimaryKey().getName(), tgtNodeTable.getPrimaryKey().getType());
//												CyRow srcRow = srcNodeTable.getRow(pk);
//												
//												if (srcRow != null) {
//													Object value = srcRow.getRaw(colName);
//													tgtRow.set(colName, value);
//												}
											}
										}
									}
								}
								
								// TODO Update genemania's style, etc...
							}
						}
					}
				}
				
				@Override
				public void allFinished(FinishStatus finishStatus) {
					// FIXME Why isn't this called by Cytoscape?
				}
			});
			getTaskIterator().append(ti);
		}
	}
	
	public class GeneManiaOrganism implements Serializable {

		private static final long serialVersionUID = -4488165932347985569L;
		
		private long taxonomyId;
		private String scientificName;
		private String abbreviatedName;
		private String commonName;

		public GeneManiaOrganism() {
		}

		public long getTaxonomyId() {
			return taxonomyId;
		}

		public void setTaxonomyId(long taxonomyId) {
			this.taxonomyId = taxonomyId;
		}

		public String getScientificName() {
			return scientificName;
		}

		public void setScientificName(String scientificName) {
			this.scientificName = scientificName;
		}

		public String getAbbreviatedName() {
			return abbreviatedName;
		}

		public void setAbbreviatedName(String abbreviatedName) {
			this.abbreviatedName = abbreviatedName;
		}

		public String getCommonName() {
			return commonName;
		}

		public void setCommonName(String commonName) {
			this.commonName = commonName;
		}

		@Override
		public String toString() {
			return scientificName;
		}
	}
	
	public class GeneManiaOrganismsResult implements Serializable {
		
		private static final long serialVersionUID = 8454506417358350512L;
		
		private List<GeneManiaOrganism> organisms;
		
		public List<GeneManiaOrganism> getOrganisms() {
			return organisms;
		}
		
		public void setOrganisms(List<GeneManiaOrganism> organisms) {
			this.organisms = organisms;
		}
	}
}
