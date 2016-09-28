package org.baderlab.csplugins.enrichmentmap.heatmap.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.baderlab.csplugins.brainlib.AvgLinkHierarchicalClustering;
import org.baderlab.csplugins.brainlib.DistanceMatrix;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class HeatMapHierarchicalClusterTask extends AbstractTask implements ObservableTask {

	private int numConditions = 0;
	private int numConditions2 = 0;
	//current subset of expression data from dataset 1 expression set
	private HashMap<Integer, GeneExpression> currentExpressionSet;
	//current subset of expression data from dataset 2 expression set
	private HashMap<Integer, GeneExpression> currentExpressionSet2;

	private HeatMapPanel heatmapPanel;
	private EnrichmentMap map;
	private EnrichmentMapParameters params;
	private HeatMapParameters hmParams;

	//data that we are hoping to populate
	private Ranking ranks = null;

	private boolean shownPearsonErrorMsg = false;

	private TaskMonitor taskMonitor;

	public HeatMapHierarchicalClusterTask(int numConditions, int numConditions2, HeatMapPanel heatmapPanel, EnrichmentMap map) {
		this.numConditions = numConditions;
		this.numConditions2 = numConditions2;
		this.heatmapPanel = heatmapPanel;
		this.currentExpressionSet = this.heatmapPanel.getCurrentExpressionSet();
		this.currentExpressionSet2 = this.heatmapPanel.getCurrentExpressionSet2();
		this.map = map;
		this.params = map.getParams();
		this.hmParams = this.params.getHmParams();
	}

	/**
	 * Hierarchical clusters the current expression set using pearson
	 * correlation and generates ranks based on the the clustering output.
	 *
	 * in the heatmap panel it sets the set of ranks based on the hierarchical
	 * clustering of the current expression set.
	 * 
	 * @throws Exception
	 */
	private void calculateRanksByClustering() throws Exception {

		//The number of conditions includes the name and description
		//compute the number of data columns we have.  If there is only one data
		//column we can not cluster data
		int numdatacolumns = 0;
		int numdatacolumns2 = 0;

		int set1_size = 0;
		int set2_size = 0;
		if(currentExpressionSet != null)
			set1_size = currentExpressionSet.keySet().size();
		if(currentExpressionSet2 != null)
			set2_size = currentExpressionSet2.keySet().size();

		boolean cluster = true;

		if(numConditions > 0) {
			numdatacolumns = numConditions - 2;
		}

		if(numConditions2 > 0) {
			numdatacolumns2 = numConditions2 - 2;
		}
		//only create a ranking if there are genes in the expression set and there
		//is more than one column of data
		if(((set1_size > 1) || (set2_size > 1)) && ((numdatacolumns + numdatacolumns2) > 1)) {

			if((cluster)/* &&(!params.isTwoDistinctExpressionSets()) */) {

				try {
					this.taskMonitor.setProgress(0);
					this.taskMonitor.setStatusMessage("Preparing data to cluster");
					//hmParams.setSortbyHC(true);
					hmParams.setSort(HeatMapParameters.Sort.CLUSTER);

					//create an array-list of the expression subset.
					ArrayList<Double[]> clustering_expressionset = new ArrayList<Double[]>();
					ArrayList<Integer> labels = new ArrayList<Integer>();
					int j = 0;

					/*
					 * Need to take into account all the different combinations
					 * of data when we are dealing with 2 expression files that
					 * don't match (as created with two different species, but
					 * can also happen if two different platforms are used)
					 */

					//if the two data sets have the same number genes we can cluster them together
					if(set1_size == set2_size && set1_size != 0) {

						//go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
						for(Iterator<Integer> i = currentExpressionSet.keySet().iterator(); i.hasNext();) {
							Integer key = i.next();

							Double[] x = ((GeneExpression) currentExpressionSet.get(key)).getExpression();
							Double[] z;
							if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null
									&& currentExpressionSet2.containsKey(key)
									&& !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename()
											.equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets()
													.getFilename())) {
								Double[] y = ((GeneExpression) currentExpressionSet2.get(key)).getExpression();
								z = new Double[x.length + y.length];
								System.arraycopy(x, 0, z, 0, x.length);
								System.arraycopy(y, 0, z, x.length, y.length);

							} else {
								z = x;
							}

							//add the expression-set
							clustering_expressionset.add(j, z);

							//add the key to the labels
							labels.add(j, key);

							j++;
							this.taskMonitor.setProgress((int) (((double) j / currentExpressionSet.size()) * 100));
						}
					}
					//if they are both non zero we need to make sure to include all the genes
					else if(set1_size > 0 && set2_size > 0) {

						Double[] dummyexpression1 = new Double[numdatacolumns];
						Double[] dummyexpression2 = new Double[numdatacolumns2];

						for(int k = 0; k < numdatacolumns; k++)
							dummyexpression1[k] = 0.0;/* Double.NaN */
						for(int k = 0; k < numdatacolumns2; k++)
							dummyexpression2[k] = 0.0;/* Double.NaN */

						int total = numdatacolumns + numdatacolumns2;
						//go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
						for(Iterator<Integer> i = currentExpressionSet.keySet().iterator(); i.hasNext();) {
							Integer key = i.next();

							Double[] x = ((GeneExpression) currentExpressionSet.get(key)).getExpression();
							Double[] z;
							if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null
									&& currentExpressionSet2.containsKey(key)
									&& !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename()
											.equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets()
													.getFilename())) {
								Double[] y = ((GeneExpression) currentExpressionSet2.get(key)).getExpression();
								z = new Double[x.length + y.length];
								System.arraycopy(x, 0, z, 0, x.length);
								System.arraycopy(y, 0, z, x.length, y.length);

							} else {

								//add a dummy value for the missing data
								z = new Double[x.length + dummyexpression2.length];
								System.arraycopy(x, 0, z, 0, x.length);
								System.arraycopy(dummyexpression2, 0, z, x.length, dummyexpression2.length);
							}

							//add the expression-set
							clustering_expressionset.add(j, z);

							//add the key to the labels
							labels.add(j, key);

							j++;
							this.taskMonitor.setProgress((int) (((double) j / total) * 100));

						}
						//go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
						for(Iterator<Integer> i = currentExpressionSet2.keySet().iterator(); i.hasNext();) {
							Integer key = i.next();

							Double[] y = ((GeneExpression) currentExpressionSet2.get(key)).getExpression();
							Double[] z;
							if(currentExpressionSet.containsKey(key)) {
								Double[] x = ((GeneExpression) currentExpressionSet.get(key)).getExpression();
								z = new Double[x.length + y.length];
								System.arraycopy(x, 0, z, 0, x.length);
								System.arraycopy(y, 0, z, x.length, y.length);

							} else {

								//add a dummy value for the missing data
								z = new Double[y.length + dummyexpression1.length];
								System.arraycopy(dummyexpression1, 0, z, 0, dummyexpression1.length);
								System.arraycopy(y, 0, z, dummyexpression1.length, y.length);
							}

							//add the expression-set
							clustering_expressionset.add(j, z);

							//add the key to the labels
							labels.add(j, key);

							j++;
							this.taskMonitor.setProgress((int) ((double) (j + numdatacolumns / total) * 100));

						}

					}
					//if one of the sets is zero
					else if((set1_size > 0) && (set2_size == 0)) {

						Double[] dummyexpression1 = new Double[numdatacolumns];
						Double[] dummyexpression2 = new Double[numdatacolumns2];

						for(int k = 0; k < numdatacolumns; k++)
							dummyexpression1[k] = 0.0; /* Double.NaN */
						for(int k = 0; k < numdatacolumns2; k++)
							dummyexpression2[k] = 0.0;/* Double.NaN */

						//go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
						for(Iterator<Integer> i = currentExpressionSet.keySet().iterator(); i.hasNext();) {
							Integer key = i.next();

							Double[] x = ((GeneExpression) currentExpressionSet.get(key)).getExpression();
							Double[] z;
							if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null
									&& currentExpressionSet2.containsKey(key)
									&& !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename()
											.equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets()
													.getFilename())) {
								Double[] y = ((GeneExpression) currentExpressionSet2.get(key)).getExpression();
								z = new Double[x.length + y.length];
								System.arraycopy(x, 0, z, 0, x.length);
								System.arraycopy(y, 0, z, x.length, y.length);

							} else {
								//add a dummy value for the missing data
								z = new Double[x.length + dummyexpression2.length];
								System.arraycopy(x, 0, z, 0, x.length);
								System.arraycopy(dummyexpression2, 0, z, x.length, dummyexpression2.length);
							}

							//add the expression-set
							clustering_expressionset.add(j, z);

							//add the key to the labels
							labels.add(j, key);

							j++;
							this.taskMonitor.setProgress((int) (((double) j / currentExpressionSet.size()) * 100));
						}
					} else if((set2_size > 0) && (set1_size == 0)) {

						Double[] dummyexpression1 = new Double[numdatacolumns];
						Double[] dummyexpression2 = new Double[numdatacolumns2];

						for(int k = 0; k < numdatacolumns; k++)
							dummyexpression1[k] = 0.0;/* Double.NaN */
						for(int k = 0; k < numdatacolumns2; k++)
							dummyexpression2[k] = 0.0;/* Double.NaN */

						//go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
						for(Iterator<Integer> i = currentExpressionSet2.keySet().iterator(); i.hasNext();) {
							Integer key = i.next();

							Double[] y = ((GeneExpression) currentExpressionSet2.get(key)).getExpression();
							Double[] z;
							if(currentExpressionSet.containsKey(key)) {
								Double[] x = ((GeneExpression) currentExpressionSet.get(key)).getExpression();
								z = new Double[x.length + y.length];
								System.arraycopy(x, 0, z, 0, x.length);
								System.arraycopy(y, 0, z, x.length, y.length);

							} else {
								//add a dummy value for the missing data
								z = new Double[y.length + dummyexpression1.length];
								System.arraycopy(dummyexpression1, 0, z, 0, dummyexpression1.length);
								System.arraycopy(y, 0, z, dummyexpression1.length, y.length);
							}

							//add the expression-set
							clustering_expressionset.add(j, z);

							//add the key to the labels
							labels.add(j, key);

							j++;
							this.taskMonitor.setProgress((int) (((double) j / currentExpressionSet2.size()) * 100));

						}
					}

					//create a distance matrix the size of the expression set
					this.taskMonitor.setProgress(0.0);
					this.taskMonitor.setStatusMessage("Calculating Distance");
					DistanceMatrix distanceMatrix;
					if(set1_size == set2_size)
						distanceMatrix = new DistanceMatrix(currentExpressionSet.keySet().size());
					else if(set1_size == 0)
						distanceMatrix = new DistanceMatrix(currentExpressionSet2.keySet().size());
					else if(set2_size == 0)
						distanceMatrix = new DistanceMatrix(currentExpressionSet.keySet().size());
					else
						distanceMatrix = new DistanceMatrix(
								currentExpressionSet2.keySet().size() + currentExpressionSet.keySet().size());
					//calculate the distance metric based on the user choice of distance metric
					if(params.getDefaultDistanceMetric().equalsIgnoreCase(HeatMapParameters.pearson_correlation)) {
						//if the user choice is pearson still have to check to make sure
						//there are no errors with pearson calculation.  If can't calculate pearson
						//then it calculates the cosine.
						try {
							distanceMatrix.calcDistances(clustering_expressionset, new PearsonCorrelation());
						} catch(RuntimeException e) {
							try {
								if(!shownPearsonErrorMsg) {
									//TODO - figure out how to warn users about chance of variable.
									//(this,"Unable to compute Pearson Correlation for this expression Set.\n  Cosine distance used for this set instead.\n To switch distance metric used for all hierarchical clustering \nPlease change setting under Advance Preferences in the Results Panel.");
									shownPearsonErrorMsg = true;
								}
								distanceMatrix.calcDistances(clustering_expressionset, new CosineDistance());
							} catch(RuntimeException ex) {
								distanceMatrix.calcDistances(clustering_expressionset, new EuclideanDistance());
							}
						}
					} else if(params.getDefaultDistanceMetric().equalsIgnoreCase(HeatMapParameters.cosine))
						distanceMatrix.calcDistances(clustering_expressionset, new CosineDistance());
					else if(params.getDefaultDistanceMetric().equalsIgnoreCase(HeatMapParameters.euclidean))
						distanceMatrix.calcDistances(clustering_expressionset, new EuclideanDistance());

					distanceMatrix.setLabels(labels);

					//cluster
					AvgLinkHierarchicalClustering cluster_result = new AvgLinkHierarchicalClustering(distanceMatrix);

					//check to see if there more than 1000 genes, if there are use eisen ordering otherwise use bar-joseph
					if((set1_size + set2_size) > 1000)
						cluster_result.setOptimalLeafOrdering(false);
					else
						cluster_result.setOptimalLeafOrdering(true);
					cluster_result.run();

					this.taskMonitor.setStatusMessage("Caculating Ranks");
					this.taskMonitor.setProgress(0);
					int[] order = cluster_result.getLeafOrder();
					ranks = new Ranking();
					for(int i = 0; i < order.length; i++) {
						//get the label
						Integer label = (Integer) labels.get(order[i]);

						GeneExpression exp;
						//check for the expression in expression set 1
						if(currentExpressionSet.containsKey(label))
							exp = (GeneExpression) currentExpressionSet.get(label);
						else if(currentExpressionSet2.containsKey(label))
							exp = (GeneExpression) currentExpressionSet2.get(label);
						else
							exp = null;

						Rank temp = new Rank(exp.getName(), 0.0, i);
						ranks.addRank(label, temp);
						this.taskMonitor.setProgress((int) (((double) i / order.length) * 100));

					}
				} catch(OutOfMemoryError e) {
					throw new Exception("Unable to complete clustering of genes due to insufficient memory.", e);
				}
			} else if(cluster && params.isTwoDistinctExpressionSets()) {
				cluster = false;
				hmParams.setSort(HeatMapParameters.Sort.NONE);
			}
		}

		if((currentExpressionSet.keySet().size() == 1) || ((numdatacolumns + numdatacolumns2) <= 1) || !(cluster)) {
			//hmParams.setNoSort(true);
			hmParams.setSort(HeatMapParameters.Sort.NONE);
			ranks = new Ranking();
			for(Iterator<Integer> i = currentExpressionSet.keySet().iterator(); i.hasNext();) {
				Integer key = i.next();
				Rank temp = new Rank(((GeneExpression) currentExpressionSet.get(key)).getName(), 0.0, 0);
				ranks.addRank(key, temp);
			}
		}
		this.heatmapPanel.setRanks(ranks);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setStatusMessage("Clustering the expression set");
		this.taskMonitor.setTitle("Clustering the expression set");
		calculateRanksByClustering();
	}

	public void run() throws Exception {
		calculateRanksByClustering();
	}

	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
