package org.baderlab.csplugins.enrichmentmap.actions;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.CyActivator;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.ComputeSimilarityTask;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import com.google.inject.Inject;

public class LegacyEnrichmentMapSessionListener implements SessionLoadedListener  /*, SessionAboutToBeSavedListener */ {

	@Inject private CyNetworkManager cyNetworkManager;
	@Inject private CyApplicationManager cyApplicationManager;
	@Inject private StreamUtil streamUtil;
	@Inject private EnrichmentMapManager emManager;
	@Inject private EnrichmentMapParameters.Factory enrichmentMapParametersFactory;


	/**
	 * Restore Enrichment maps
	 *
	 * @param pStateFileList - list of files associated with thie session
	 */
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		if (e.getLoadedSession().getAppFileListMap() == null || e.getLoadedSession().getAppFileListMap().size() ==0){
			return;
		}

		List<File> pStateFileList = e.getLoadedSession().getAppFileListMap().get(CyActivator.APP_NAME);
		try {
			//go through the prop files first to create the correct objects to be able
			//to add other files to.
			for(int i = 0; i < pStateFileList.size(); i++){

				File prop_file = pStateFileList.get(i);

				if(prop_file.getName().contains(".props")){

					InputStream reader = streamUtil.getInputStream(prop_file.getAbsolutePath());       			
					String fullText = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();                        

					//Given the file with all the parameters create a new parameter
					EnrichmentMapParameters params = enrichmentMapParametersFactory.create(fullText);
					EnrichmentMap em = new EnrichmentMap(params);

					//get the network name
					String param_name = em.getName();

					//TODO:distinguish between GSEA and EM saved sessions
					String props_name = (prop_file.getName().split("\\."))[0];
					String name = param_name;

					//check to see if the network name matches the name of the file
					//the network name specified in the props file is different from the name of the props
					//file then assume the name in the props file is wrong and set it to the file name (legacy issue)
					//related to bug ticket #49
					if(!props_name.equalsIgnoreCase(param_name)){
						name = props_name;
						em.setName(name);
					}

					//after associated the properties with the network
					//initialized each Dataset that we have files for
					HashMap<String, DataSetFiles> files = params.getFiles();
					for(Iterator<String> j = params.getFiles().keySet().iterator();j.hasNext();){
						String current_dataset = j.next();
						em.addDataset(current_dataset, new DataSet(em,current_dataset,files.get(current_dataset)));                    		
					}

					//register network and parameters
					emManager.registerNetwork(getNetworkByName(name),em);
				}
			}
			//go through the rest of the files
			for(int i = 0; i < pStateFileList.size(); i++){

				File prop_file = pStateFileList.get(i);

				FileNameParts parts = ParseFileName(prop_file);
				if(parts == null || prop_file.getName().contains(".props"))
					continue;

				CyNetwork net = getNetworkByName(parts.name);
				EnrichmentMap em  = (net != null) ? emManager.getMap(net.getSUID()) : null;

				if(em == null)
					System.out.println("network for file" + prop_file.getName() + " does not exist.");
				else if((!prop_file.getName().contains(".props"))
						&& (!prop_file.getName().contains(".expression1.txt"))
						&& (!prop_file.getName().contains(".expression2.txt"))
						){
					EnrichmentMapParameters params = em.getParams();
					HashMap<String,String> props = params.getProps();
					//if this a dataset specific file make sure there is a dataset object for it
					if(!(parts.dataset == null) && em.getDataset(parts.dataset) == null && !parts.dataset.equalsIgnoreCase("signature"))
						em.addDataset(parts.dataset, new DataSet(em,parts.name,params.getFiles().get(parts.dataset)));
					if(parts.type == null)
						System.out.println("Sorry, unable to determine the type of the file: "+ prop_file.getName());

					//read the file
					InputStream reader = streamUtil.getInputStream(prop_file.getAbsolutePath());     			
					String fullText = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();                        


					//if the file is empty then skip it
					if(fullText == null || fullText.equalsIgnoreCase(""))
						continue;

					if(prop_file.getName().contains(".gmt")){
						if (prop_file.getName().contains(".signature.gmt"))
							em.setSignatureGenesets((HashMap<String, GeneSet>)params.repopulateHashmap(fullText, 1));
						//account for legacy session files
						else if(prop_file.getName().contains(".set2.gmt")){
							if(em.getAllGenesets().containsKey(EnrichmentMap.DATASET2)){
								SetOfGeneSets gs = new SetOfGeneSets(EnrichmentMap.DATASET2,props);
								gs.setGenesets((HashMap<String, GeneSet>) params.repopulateHashmap(fullText,1));
							}
						}else{
							SetOfGeneSets gs = new SetOfGeneSets(parts.dataset,props);
							gs.setGenesets((HashMap<String, GeneSet>)params.repopulateHashmap(fullText,1));
							em.getDatasets().get(parts.dataset).setSetofgenesets(gs);
						}
					}
					if(prop_file.getName().contains(".genes.txt")){
						HashMap<String, Integer> genes = params.repopulateHashmap(fullText,2);
						em.setGenes(genes);
						//ticket #188 - unable to open session files that have empty enrichment maps.
						if(genes != null && !genes.isEmpty())
							// Ticket #107 : restore also gene count (needed to determine the next free hash in case we do PostAnalysis with a restored session)
							em.setNumberOfGenes( Math.max( em.getNumberOfGenes(), Collections.max(genes.values())+1 ));
					}
					if(prop_file.getName().contains(".hashkey2genes.txt")){
						HashMap<Integer,String> hashkey2gene = params.repopulateHashmap(fullText,5);
						em.setHashkey2gene(hashkey2gene);
						//ticket #188 - unable to open session files that have empty enrichment maps.
						if(hashkey2gene != null && !hashkey2gene.isEmpty() )
							// Ticket #107 : restore also gene count (needed to determine the next free hash in case we do PostAnalysis with a restored session)
							em.setNumberOfGenes( Math.max( em.getNumberOfGenes(), Collections.max(hashkey2gene.keySet())+1 ));
					}



					if((parts.type != null && (parts.type.equalsIgnoreCase("ENR") || (parts.type.equalsIgnoreCase("SubENR")))) 
							|| prop_file.getName().contains(".ENR1.txt") || prop_file.getName().contains(".SubENR1.txt")){
						SetOfEnrichmentResults enrichments;
						int temp = 1;
						//check to see if this dataset has enrichment results already
						if(parts.dataset != null && em.getDataset(parts.dataset).getEnrichments() != null)
							enrichments = em.getDataset(parts.dataset).getEnrichments();
						else	 if (parts.dataset == null){
							enrichments = em.getDataset(EnrichmentMap.DATASET1).getEnrichments();
							/*enrichments = new SetOfEnrichmentResults(EnrichmentMap.DATASET1,props);
							em.getDataset(EnrichmentMap.DATASET1).setEnrichments(enrichments);*/
						}
						else	{
							enrichments = new SetOfEnrichmentResults(parts.dataset,props);
							em.getDataset(parts.dataset).setEnrichments(enrichments);
						}
						if(parts.type.equalsIgnoreCase("ENR") || prop_file.getName().contains(".ENR1.txt")){
							if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
								enrichments.setEnrichments(params.repopulateHashmap(fullText,3));
							else
								enrichments.setEnrichments(params.repopulateHashmap(fullText,4));
						}

					}

					//have to keep this method just in case old session files have ranks saved in this way
					//it would only happen for sessions saved with version 0.8
					if(prop_file.getName().contains(".RANKS1.txt") || prop_file.getName().contains(".RANKS1Genes.txt")){
						Ranking new_ranking;
						//Check to see if there is already GSEARanking
						if(em.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getAllRanksNames().contains(Ranking.GSEARanking))
							new_ranking = em.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getRanksByName(Ranking.GSEARanking);
						else{
							new_ranking = new Ranking();
							em.getDataset(EnrichmentMap.DATASET1).getExpressionSets().addRanks(Ranking.GSEARanking, new_ranking);                			
						}
						if(prop_file.getName().contains(".RANKS1Genes.txt"))
							new_ranking.setRank2gene(em.getParams().repopulateHashmap(fullText,7));
						if(prop_file.getName().contains(".RANKS1.txt"))
							new_ranking.setRanking(em.getParams().repopulateHashmap(fullText,6));
					}

					if(prop_file.getName().contains(".RANKS.txt")){
						if(parts.ranks_name == null){
							//we need to get the name of this set of rankings
							// network_name.ranking_name.ranks.txt --> split by "." and get 2

							String[] file_name_tokens = (prop_file.getName()).split("\\.");

							if((file_name_tokens.length == 4) && (file_name_tokens[1].equals("Dataset 1 Ranking") || file_name_tokens[1].equals("Dataset 2 Ranking"))
									|| (prop_file.getName().contains(Ranking.GSEARanking)))
								parts.ranks_name = Ranking.GSEARanking ;
							
							//this is an extra rank file for backwards compatability.  Ignore it.
							else if((file_name_tokens.length == 4) && (file_name_tokens[1].equals("Dataset 1") || file_name_tokens[1].equals("Dataset 2")) && file_name_tokens[2].equals("RANKS"))
									continue;
							else
								//file name is not structured properly --> default to file name
								parts.ranks_name = prop_file.getName();
						}
						Ranking new_ranking = new Ranking();
						new_ranking.setRanking(em.getParams().repopulateHashmap(fullText,6));

						if(parts.dataset != null)
							em.getDataset(parts.dataset).getExpressionSets().addRanks(parts.ranks_name,new_ranking);
						else
							em.getDataset(EnrichmentMap.DATASET1).getExpressionSets().addRanks(parts.ranks_name,new_ranking);
					}
					//Deal with legacy issues                    
					if(params.isTwoDatasets()){
						//make sure there is a Dataset2
						if(!em.getDatasets().containsKey(EnrichmentMap.DATASET2))
							em.addDataset(EnrichmentMap.DATASET2, null);
						if( prop_file.getName().contains(".ENR2.txt") || prop_file.getName().contains(".SubENR2.txt")){
							SetOfEnrichmentResults enrichments;
							//check to see if this dataset has enrichment results already
							if(em.getDataset(EnrichmentMap.DATASET2).getEnrichments() != null)
								enrichments = em.getDataset(EnrichmentMap.DATASET2).getEnrichments();
							else	{
								enrichments = new SetOfEnrichmentResults(EnrichmentMap.DATASET2,props);
								em.getDataset(EnrichmentMap.DATASET2).setEnrichments(enrichments);
							}
							if(prop_file.getName().contains(".ENR2.txt")){
								if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
									enrichments.setEnrichments(params.repopulateHashmap(fullText,3));
								else
									enrichments.setEnrichments(params.repopulateHashmap(fullText,4));
							}  			
						}                        
						//have to keep this method just in case old session files have ranks saved in this way
						//it would only happen for sessions saved with version 0.8
						if(prop_file.getName().contains(".RANKS2.txt") || prop_file.getName().contains(".RANKS2Genes.txt")){
							Ranking new_ranking;
							//Check to see if there is already GSEARanking
							if(em.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getAllRanksNames().contains(Ranking.GSEARanking))
								new_ranking = em.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getRanksByName(Ranking.GSEARanking);
							else{
								new_ranking = new Ranking();
								em.getDataset(EnrichmentMap.DATASET2).getExpressionSets().addRanks(Ranking.GSEARanking, new_ranking);                			
							}
							if(prop_file.getName().contains(".RANKS2Genes.txt"))
								new_ranking.setRank2gene(em.getParams().repopulateHashmap(fullText,7));
							if(prop_file.getName().contains(".RANKS2.txt"))
								new_ranking.setGene2rank(em.getParams().repopulateHashmap(fullText,6));
						}
					}
				}

			}

			//load the expression files.  Load them last because they require
			//info from the parameters
			for(int i = 0; i < pStateFileList.size(); i++){

				File prop_file = pStateFileList.get(i);
				FileNameParts parts_exp = ParseFileName(prop_file);
				//unrecognized file
				if((parts_exp == null) || (parts_exp.name == null) )continue;

				CyNetwork net = getNetworkByName(parts_exp.name);
				EnrichmentMap map  = (net != null) ? emManager.getMap(net.getSUID()) : null;
				Map<String,String> props = map.getParams().getProps();

				if(parts_exp.type != null && parts_exp.type.equalsIgnoreCase("expression")){
					if(map.getDatasets().containsKey(parts_exp.dataset)){
						DataSet ds = map.getDataset(parts_exp.dataset);
						ds.getDatasetFiles().setExpressionFileName(prop_file.getAbsolutePath());
						ds.getExpressionSets().setFilename(prop_file.getAbsolutePath());
						ExpressionFileReaderTask expressionFile1 = new ExpressionFileReaderTask(ds);
						GeneExpressionMatrix matrix = expressionFile1.parse();
						matrix.restoreProps(parts_exp.dataset, props);
					}
				}
				//Deal with legacy session files.
				if(prop_file.getName().contains("expression1.txt")){                  
					//Load the GCT file
					//get Dataset1
					DataSet ds1 = map.getDataset(EnrichmentMap.DATASET1);
					ds1.getDatasetFiles().setExpressionFileName(prop_file.getAbsolutePath());
					ds1.getExpressionSets().setFilename(prop_file.getAbsolutePath());
					ExpressionFileReaderTask expressionFile1 = new ExpressionFileReaderTask(ds1);
					expressionFile1.parse();

				}
				if(prop_file.getName().contains("expression2.txt")){                    
					DataSet ds2 = map.getDataset(EnrichmentMap.DATASET2);
					ds2.getDatasetFiles().setExpressionFileName(prop_file.getAbsolutePath());
					ds2.getExpressionSets().setFilename(prop_file.getAbsolutePath());
					ExpressionFileReaderTask expressionFile2 = new ExpressionFileReaderTask(ds2);
					expressionFile2.parse();

					//if there are two expression sets and there is a second set of genesets of interest then we
					//are dealing with two distinct expression files.
					if( map.getDataset(EnrichmentMap.DATASET2) != null && map.getDataset(EnrichmentMap.DATASET2).getGenesetsOfInterest() != null && !map.getDataset(EnrichmentMap.DATASET2).getGenesetsOfInterest().getGenesets().isEmpty() ){
						map.getParams().setTwoDistinctExpressionSets(true);
						map.getDataset(EnrichmentMap.DATASET1).setDatasetGenes(new HashSet<Integer>((Set<Integer>)map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getGeneIds()));
						map.getDataset(EnrichmentMap.DATASET2).setDatasetGenes(new HashSet<Integer>((Set<Integer>)map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getGeneIds()));
					}

				}
			}

			/* else if(params.getDataset1Rankings() != null){
				params.setRank2geneDataset1(params.getRank2geneDataset(params.getDataset1Rankings()));
			 */

			//register the action listeners for all the networks.
			emManager.registerServices();
			HashMap<Long, EnrichmentMap> networks = emManager.getCyNetworkList();

			//iterate over the networks
			for(Iterator<Long> j = networks.keySet().iterator();j.hasNext();){
				Long id = j.next();
				CyNetwork currentNetwork = cyNetworkManager.getNetwork(id);
				EnrichmentMap map = emManager.getMap(id);
				//only initialize objects if there is a map for this network
				if(map != null){
					//initialize the Genesets (makes sure the leading edge is set correctly)
					//Initialize the set of genesets and GSEA results that we want to compute over
					InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(map);
					genesets_init.initializeSets(null);

					//for each map compute the similarity matrix, (easier than storing it) compute the geneset similarities
					ComputeSimilarityTask similarities = new ComputeSimilarityTask(map, ComputeSimilarityTask.ENRICHMENT);
					Map<String, GenesetSimilarity> similarity_results = similarities.computeGenesetSimilarities(null);
					map.setGenesetSimilarity(similarity_results);

					// also compute geneset similarities between Enrichment- and Signature Genesets (if any)
					if (! map.getSignatureGenesets().isEmpty()){
						ComputeSimilarityTask sigSimilarities = new ComputeSimilarityTask(map, ComputeSimilarityTask.SIGNATURE);
						Map<String, GenesetSimilarity> sig_similarity_results = sigSimilarities.computeGenesetSimilarities(null);
						map.getGenesetSimilarity().putAll(sig_similarity_results);
					}

					//set the last network to be the one viewed and initialize the parameters panel
					if(!j.hasNext()){
						cyApplicationManager.setCurrentNetwork(currentNetwork);
						ParametersPanel paramPanel = emManager.getParameterPanel();
						paramPanel.updatePanel(map);
						paramPanel.revalidate();
					}
				}//end of if(map != null)
			}

		} catch (Exception ee) {
			ee.printStackTrace();
		}	
	}

//	/**
//	 * SaveSessionStateFiles collects all the data stored in the Enrichment maps
//	 * and creates property files for each network listing the variables needed to rebuild the map.
//	 * All data(Hashmaps) collections needed for the Enrichment map are stored in separate files specified by the name
//	 * of the network with specific file endings to indicate what type of data is stored in the files (i.e. ENR for enrichment,
//	 * genes for genes...).
//	 */
//	@Override
//	public void handleEvent(SessionAboutToBeSavedEvent e) {
//
//		ArrayList<File> pFileList = new ArrayList<File>();
//		String tmpDir = System.getProperty("java.io.tmpdir");
//		System.out.println("java.io.tmpdir: [" + tmpDir + "]");
//
//		String prop_file_content = "";
//
//		//get the networks
//		HashMap<Long, EnrichmentMap> networks = emManager.getCyNetworkList();
//
//		//go through each network
//		for(Iterator<Long> i = networks.keySet().iterator(); i.hasNext();){
//			Long networkId = i.next();
//			EnrichmentMap em = networks.get(networkId);
//			EnrichmentMapParameters params = networks.get(networkId).getParams();
//			//current network
//			CyNetwork network = cyNetworkManager.getNetwork(networkId); 
//
//			String name = network.getRow(network).get(CyNetwork.NAME,String.class);
//
//			//get the network name specified in the parameters
//			String param_name = em.getName();
//
//			//check to see if the name of the network matches the one specified in it parameters
//			//if the two names differ then use the network name specified by the user
//			if(!name.equalsIgnoreCase(param_name))
//				em.setName(name);
//
//			//property file
//			File session_prop_file = new File(tmpDir,name+".props");
//
//			//get the properties to be saved that are associated with the map
//			//genes involved in the analysis
//			//do not need to store the similarities because they are recomputed on reload
//			//geneset file for PostAnalysis Signature Genesets
//			File siggmt = new File(tmpDir,name+".signature.gmt");
//
//			prop_file_content = prop_file_content + "Version\t2.0\n";
//			prop_file_content = prop_file_content + params.toString();
//			try{
//				if (!em.getSignatureGenesets().isEmpty() ) {
//					BufferedWriter sigGmtwriter = new BufferedWriter(new FileWriter(siggmt));
//					sigGmtwriter.write(params.printHashmap(em.getSignatureGenesets()));
//					sigGmtwriter.close();
//					pFileList.add(siggmt);
//				}
//
//				File genes = new File(tmpDir, name+".genes.txt");
//				BufferedWriter geneswriter = new BufferedWriter(new FileWriter(genes));
//				geneswriter.write(params.printHashmap(em.getGenes()));
//				geneswriter.close();
//				pFileList.add(genes);
//
//				File hkgenes = new File(tmpDir, name+".hashkey2genes.txt");
//				BufferedWriter hashkey2geneswriter = new BufferedWriter(new FileWriter(hkgenes));
//				hashkey2geneswriter.write(params.printHashmap(em.getHashkey2gene()));
//				hashkey2geneswriter.close();
//				pFileList.add(hkgenes);
//
//				//get the properties associated with each Dataset
//				if(!em.getDatasets().isEmpty()){
//					HashMap<String, DataSet> all_datasets = em.getDatasets();
//
//					//output to the property file how many datasets we have (so we know on reload)
//					prop_file_content = prop_file_content + "Datasets\t"+  all_datasets.keySet().toString() +"\n";
//
//					for(Iterator<String> k  = all_datasets.keySet().iterator(); k.hasNext();){
//						String dataset_name = k.next().toString();
//						String current = dataset_name ;
//						if(dataset_name .contains("."))
//							dataset_name .replace('.', '_');
//
//						//genesets
//						File gmt = new File(tmpDir, name+ "." + dataset_name +".gmt");
//						BufferedWriter gmtwriter = new BufferedWriter(new FileWriter(gmt));
//						gmtwriter.write(params.printHashmap(em.getDataset(current).getGenesetsOfInterest().getGenesets()));
//						gmtwriter.close();
//						pFileList.add(gmt);
//
//						File enrichmentresults_backcomp = new File(tmpDir, name +".ENR1.txt");
//						if(dataset_name.equals(EnrichmentMap.DATASET1))
//							enrichmentresults_backcomp = new File(tmpDir, name +".ENR1.txt");
//						if(dataset_name.equals(EnrichmentMap.DATASET2))
//							enrichmentresults_backcomp = new File(tmpDir, name +".ENR2.txt");	
//						BufferedWriter enr1writer_backcomp = new BufferedWriter(new FileWriter(enrichmentresults_backcomp));
//						enr1writer_backcomp.write(params.printHashmap(em.getDataset(current).getEnrichments().getEnrichments()));
//						enr1writer_backcomp.close();
//						pFileList.add(enrichmentresults_backcomp);    
//
//						prop_file_content = prop_file_content + em.getDataset(current).getSetofgenesets().toString(current);
//
//						//enrichments
//						File enrichmentresults = new File(tmpDir, name+"." + dataset_name +".ENR.txt");
//
//						BufferedWriter enr1writer = new BufferedWriter(new FileWriter(enrichmentresults));
//						enr1writer.write(params.printHashmap(em.getDataset(current).getEnrichments().getEnrichments()));
//						enr1writer.close();
//						pFileList.add(enrichmentresults);            				
//
//						prop_file_content = prop_file_content + em.getDataset(current).getEnrichments().toString(current);
//
//						//expression
//						if(em.getDataset(current).getExpressionSets() != null){
//
//							File expression = new File(tmpDir, name+"." + dataset_name +".expression.txt");
//							BufferedWriter expression1writer = new BufferedWriter(new FileWriter(expression));
//							expression1writer.write(em.getDataset(current).getExpressionSets().toString());
//							expression1writer.close();
//							pFileList.add(expression);
//
//							//print out the information about the expression files
//							prop_file_content = prop_file_content + em.getDataset(current).getExpressionSets().toString(current);
//
//							//save all the rank files
//							if(!em.getDataset(current).getExpressionSets().getRanks().isEmpty()){
//								HashMap<String, Ranking> all_ranks = em.getDataset(current).getExpressionSets().getRanks();
//
//								for(Iterator j = all_ranks.keySet().iterator(); j.hasNext(); ){
//									String ranks_name = j.next().toString();
//									String current_ranks_name = ranks_name;
//									// as ranks names that contain dots make problems, when restoring a session,
//									// we'll replace them by underscores:
//									if (ranks_name.contains("."))
//										ranks_name.replace('.', '_');
//									File current_ranks = new File(tmpDir, name+"." + dataset_name + "."+ranks_name+".RANKS.txt");
//									BufferedWriter subrank1writer = new BufferedWriter(new FileWriter(current_ranks));
//									subrank1writer.write(params.printHashmap(all_ranks.get(current_ranks_name).getRanking()));
//									subrank1writer.close();
//									pFileList.add(current_ranks);
//
//									//backwards compatibility: add the old file
//									if(dataset_name.equals(EnrichmentMap.DATASET1)){
//										File current_ranks_backcomp = new File(tmpDir, name+"."+ranks_name+".RANKS.txt");
//										BufferedWriter subrank1writer_backcomp = new BufferedWriter(new FileWriter(current_ranks_backcomp));
//										subrank1writer_backcomp.write(params.printHashmap(all_ranks.get(current_ranks_name).getRanking()));
//										subrank1writer_backcomp.close();
//										pFileList.add(current_ranks_backcomp);
//									}
//								}
//							}
//						}
//					}
//					BufferedWriter writer = new BufferedWriter(new FileWriter(session_prop_file));
//					writer.write(prop_file_content);
//					writer.close();
//					pFileList.add(session_prop_file);
//				}
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//
//		}
//
//		//Add the files to be saved
//		try{
//			e.addAppFiles(appName, pFileList);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}
	
	
	private FileNameParts ParseFileName(File filename){
		String fullname = (filename.getName());
		String name=null,type=null,dataset=null,ranks_name= null;
		if(fullname != null){
			String[] tokens = fullname.split("\\.");
			if((tokens.length ==2) && (tokens[1].equals("gmt"))){
				name = tokens[0];
				dataset = EnrichmentMap.DATASET1;
				type = "gmt";
			}
			//if the length is three then the file is associated with whole map
			if((tokens.length == 3) && (!tokens[2].equals("gmt"))){
				name = tokens[0];
				type = tokens[1];
			}
			if((tokens.length == 3) && (tokens[2].equals("gmt"))){
				name = tokens[0];
				dataset = tokens[1];
				type = "gmt";
			}
			//if the length is four or more then it is associated with a specific dataset
			if((tokens.length == 4) && !tokens[2].equals("RANKS")){
				name = tokens[0];
				dataset = tokens[1];
				type = tokens[tokens.length-2];
			}
			//legacy issue with old session files.
			if((tokens.length == 4) && tokens[2].equals("RANKS")){
				name = tokens[0];
				dataset = null;
				type = tokens[tokens.length-2];
			}
			if(tokens.length > 4){
				name = tokens[0];
				dataset = tokens[1];
				ranks_name = tokens[tokens.length-3];
				type = tokens[tokens.length-2];
			}
			FileNameParts parts = new FileNameParts(name,type,dataset,ranks_name);
			return parts;
		}
		return null;	
	}

	//get Network by name
	public CyNetwork getNetworkByName(String name){
		Set<CyNetwork> networks = cyNetworkManager.getNetworkSet();
		for(CyNetwork network:networks){

			String currentName = network.getRow(network).get(CyNetwork.NAME,String.class);
			if(currentName.equals(name))
				return network;
		}
		return null;
	}
	
	//internal class to hold the parts of the file name
	public class FileNameParts{
		public String name;
		public String type;
		public String ranks_name;
		public String dataset;

		public FileNameParts(String name, String type, String dataset, String ranks_name){
			this.name = name;
			this.type = type;
			this.dataset = dataset;
			this.ranks_name = ranks_name;
		}
	}

}
