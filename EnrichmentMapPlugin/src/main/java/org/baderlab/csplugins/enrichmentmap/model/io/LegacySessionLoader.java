package org.baderlab.csplugins.enrichmentmap.model.io;

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
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask;
import org.baderlab.csplugins.enrichmentmap.util.NamingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;

import com.google.inject.Inject;

public class LegacySessionLoader {

	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyNetworkManager cyNetworkManager;
	@Inject private CyApplicationManager cyApplicationManager;
	@Inject private StreamUtil streamUtil;
	@Inject private EnrichmentMapManager emManager;
	@Inject private EnrichmentMapParameters.Factory enrichmentMapParametersFactory;

	
	public static boolean isLegacy(CySession session) {
		Map<String,List<File>> appFileListMap = session.getAppFileListMap();
		if(appFileListMap == null || appFileListMap.isEmpty()) {
			return false;
		}
		List<File> fileList = appFileListMap.get(CyActivator.APP_NAME);
		if(fileList == null || fileList.isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Restore Enrichment maps
	 *
	 * @param pStateFileList - list of files associated with thie session
	 */
	@SuppressWarnings("unchecked")
	public void loadSession(CySession session) {
		Map<Long, EnrichmentMapParameters> paramsMap = new HashMap<>();
		Map<Long, EnrichmentMap> enrichmentMapMap = new HashMap<>();

		List<File> fileList = session.getAppFileListMap().get(CyActivator.APP_NAME);
		
		try {
			//go through the prop files first to create the correct objects to be able to add other files to.
			for (File prop_file : fileList) {
				if (prop_file.getName().contains(".props")) {
					InputStream reader = streamUtil.getInputStream(prop_file.getAbsolutePath());
					String fullText = new Scanner(reader, "UTF-8").useDelimiter("\\A").next();              

					//Given the file with all the parameters create a new parameter
					EnrichmentMapParameters params = enrichmentMapParametersFactory.create(fullText);
					EnrichmentMap em = new EnrichmentMap(params.getCreationParameters(), serviceRegistrar);

					//get the network name
					String param_name = em.getName();

					//TODO:distinguish between GSEA and EM saved sessions
					String props_name = (prop_file.getName().split("\\."))[0];
					String networkName = param_name;

					//check to see if the network name matches the name of the file
					//the network name specified in the props file is different from the name of the props
					//file then assume the name in the props file is wrong and set it to the file name (legacy issue)
					//related to bug ticket #49
					if (!props_name.equalsIgnoreCase(param_name))
						networkName = props_name;

					//after associated the properties with the network
					//initialized each Dataset that we have files for
					HashMap<String, DataSetFiles> files = params.getFiles();

					for (Iterator<String> j = params.getFiles().keySet().iterator(); j.hasNext();) {
						String current_dataset = j.next();
						Method method = EnrichmentMapParameters.stringToMethod(params.getMethod());
						em.createDataSet(current_dataset, method, files.get(current_dataset));
					}

					CyNetwork network = getNetworkByName(networkName);
					Long suid = network.getSUID();
					em.setNetworkID(suid);
					paramsMap.put(suid, params);
					enrichmentMapMap.put(suid, em);
				}
			}
			
			// go through the rest of the files
			for (File propFile : fileList) {
				FileNameParts parts = ParseFileName(propFile);
				if (parts == null || propFile.getName().contains(".props"))
					continue;

				CyNetwork net = getNetworkByName(parts.name);
				EnrichmentMap em = net == null ? null : enrichmentMapMap.get(net.getSUID());
				EnrichmentMapParameters params = paramsMap.get(net.getSUID());
				Method method = EnrichmentMapParameters.stringToMethod(params.getMethod());

				if (em == null) {
					System.out.println("network for file" + propFile.getName() + " does not exist.");
				} else if ((!propFile.getName().contains(".props")) && (!propFile.getName().contains(".expression1.txt"))
						&& (!propFile.getName().contains(".expression2.txt"))) {
					HashMap<String, String> props = params.getProps();
					//if this a dataset specific file make sure there is a dataset object for it
					if(!(parts.dataset == null) && em.getDataSet(parts.dataset) == null && !parts.dataset.equalsIgnoreCase("signature"))
						em.createDataSet(parts.dataset, method, params.getFiles().get(parts.dataset));
					if(parts.type == null)
						System.out.println("Sorry, unable to determine the type of the file: "+ propFile.getName());

					//read the file
					InputStream reader = streamUtil.getInputStream(propFile.getAbsolutePath());     			
					String fullText = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();                        

					//if the file is empty then skip it
					if (fullText == null || fullText.equalsIgnoreCase(""))
						continue;

					if (propFile.getName().contains(".gmt")) {
						HashMap<String, GeneSet> gsMap =
								(HashMap<String, GeneSet>) params.repopulateHashmap(fullText, 1);
						
						if (propFile.getName().contains(".signature.gmt")) {
							// TODO Find a better way to serialize EMSignatureDataSet
							String sdsName = propFile.getName().replace(".signature.gmt", "");
							sdsName = NamingUtil.getUniqueName(sdsName, em.getSignatureDataSets().keySet());
							EMSignatureDataSet sigDataSet = new EMSignatureDataSet(sdsName);
							em.addSignatureDataSet(sigDataSet);
							SetOfGeneSets sigGeneSets = sigDataSet.getGeneSetsOfInterest();
							
							gsMap.forEach((k, v) -> sigGeneSets.addGeneSet(k, v));
						} else if (propFile.getName().contains(".set2.gmt")) {
							// account for legacy session files
							if (em.getAllGeneSets().containsKey(LegacySupport.DATASET2)) {
								SetOfGeneSets gs = new SetOfGeneSets(LegacySupport.DATASET2, props);
								gs.setGeneSets(gsMap);
							}
						} else {
							SetOfGeneSets gs = new SetOfGeneSets(parts.dataset, props);
							gs.setGeneSets(gsMap);
							em.getDataSets().get(parts.dataset).setSetOfGeneSets(gs);
						}
					}
					
					if (propFile.getName().contains(".genes.txt")) {
						HashMap<String, Integer> genes = params.repopulateHashmap(fullText, 2);
						genes.forEach(em::addGene);
						
						//ticket #188 - unable to open session files that have empty enrichment maps.
						if (genes != null && !genes.isEmpty())
							// Ticket #107 : restore also gene count (needed to determine the next free hash in case we do PostAnalysis with a restored session)
							em.setNumberOfGenes( Math.max( em.getNumberOfGenes(), Collections.max(genes.values())+1 ));
					}
					
					if (propFile.getName().contains(".hashkey2genes.txt")) {
						HashMap<Integer,String> hashkey2gene = params.repopulateHashmap(fullText,5);
//						em.setHashkey2gene(hashkey2gene);
						//ticket #188 - unable to open session files that have empty enrichment maps.
						if (hashkey2gene != null && !hashkey2gene.isEmpty() )
							// Ticket #107 : restore also gene count (needed to determine the next free hash in case we do PostAnalysis with a restored session)
							em.setNumberOfGenes( Math.max( em.getNumberOfGenes(), Collections.max(hashkey2gene.keySet())+1 ));
					}

					if ((parts.type != null && (parts.type.equalsIgnoreCase("ENR") || (parts.type.equalsIgnoreCase("SubENR")))) 
							|| propFile.getName().contains(".ENR1.txt") || propFile.getName().contains(".SubENR1.txt")){
						SetOfEnrichmentResults enrichments;
						int temp = 1;
						
						//check to see if this dataset has enrichment results already
						if (parts.dataset != null && em.getDataSet(parts.dataset).getEnrichments() != null) {
							enrichments = em.getDataSet(parts.dataset).getEnrichments();
						} else if (parts.dataset == null){
							enrichments = em.getDataSet(LegacySupport.DATASET1).getEnrichments();
							/*enrichments = new SetOfEnrichmentResults(EnrichmentMap.DATASET1,props);
                			em.getDataset(EnrichmentMap.DATASET1).setEnrichments(enrichments);*/
						} else {
							enrichments = new SetOfEnrichmentResults(parts.dataset, props);
							em.getDataSet(parts.dataset).setEnrichments(enrichments);
						}
						
						if (parts.type.equalsIgnoreCase("ENR") || propFile.getName().contains(".ENR1.txt")) {
							if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
								enrichments.setEnrichments(params.repopulateHashmap(fullText, 3));
							else
								enrichments.setEnrichments(params.repopulateHashmap(fullText, 4));
						}

					}

					//have to keep this method just in case old session files have ranks saved in this way
					//it would only happen for sessions saved with version 0.8
					if (propFile.getName().contains(".RANKS1.txt") || propFile.getName().contains(".RANKS1Genes.txt")){
						Ranking new_ranking;
						//Check to see if there is already GSEARanking
						if (em.getDataSet(LegacySupport.DATASET1).getExpressionSets().getAllRanksNames()
								.contains(Ranking.GSEARanking)) {
							new_ranking = em.getDataSet(LegacySupport.DATASET1).getExpressionSets()
									.getRanksByName(Ranking.GSEARanking);
						} else {
							new_ranking = new Ranking();
							em.getDataSet(LegacySupport.DATASET1).getExpressionSets().addRanks(Ranking.GSEARanking,
									new_ranking);
						}
						
						if (propFile.getName().contains(".RANKS1.txt")) {
							Map<Integer, Rank> ranks = (Map<Integer, Rank>) params.repopulateHashmap(fullText, 7);
							ranks.forEach(new_ranking::addRank);
						}
//						if(prop_file.getName().contains(".RANKS1Genes.txt"))
//							new_ranking.setRank2gene(em.getParams().repopulateHashmap(fullText,7));
//						if(prop_file.getName().contains(".RANKS1.txt"))
//							new_ranking.setRanking(em.getParams().repopulateHashmap(fullText,6));
					}

					if (propFile.getName().contains(".RANKS.txt")) {
						if (parts.ranks_name == null) {
							//we need to get the name of this set of rankings
							// network_name.ranking_name.ranks.txt --> split by "." and get 2

							String[] file_name_tokens = (propFile.getName()).split("\\.");

							if((file_name_tokens.length == 4) && (file_name_tokens[1].equals("Dataset 1 Ranking") || file_name_tokens[1].equals("Dataset 2 Ranking"))
									|| (propFile.getName().contains(Ranking.GSEARanking)))
								parts.ranks_name = Ranking.GSEARanking ;
							
							//this is an extra rank file for backwards compatability.  Ignore it.
							else if ((file_name_tokens.length == 4)
									&& (file_name_tokens[1].equals("Dataset 1")
											|| file_name_tokens[1].equals("Dataset 2"))
									&& file_name_tokens[2].equals("RANKS"))
								continue;
							else //file name is not structured properly --> default to file name
								parts.ranks_name = propFile.getName();
						}
						Ranking new_ranking = new Ranking();
						Map<Integer,Rank> ranks = (Map<Integer,Rank>)params.repopulateHashmap(fullText,6);
						ranks.forEach(new_ranking::addRank);

						if (parts.dataset != null)
							em.getDataSet(parts.dataset).getExpressionSets().addRanks(parts.ranks_name, new_ranking);
						else
							em.getDataSet(LegacySupport.DATASET1).getExpressionSets().addRanks(parts.ranks_name,
									new_ranking);
					}
					
					//Deal with legacy issues                    
					if (params.isTwoDatasets()) {
						//make sure there is a Dataset2
						if (!em.getDataSets().containsKey(LegacySupport.DATASET2))
							em.createDataSet(LegacySupport.DATASET2, method, new DataSetFiles());
						
						if (propFile.getName().contains(".ENR2.txt") || propFile.getName().contains(".SubENR2.txt")) {
							SetOfEnrichmentResults enrichments;
							//check to see if this dataset has enrichment results already
							if (em.getDataSet(LegacySupport.DATASET2).getEnrichments() != null) {
								enrichments = em.getDataSet(LegacySupport.DATASET2).getEnrichments();
							} else {
								enrichments = new SetOfEnrichmentResults(LegacySupport.DATASET2, props);
								em.getDataSet(LegacySupport.DATASET2).setEnrichments(enrichments);
							}
							
							if (propFile.getName().contains(".ENR2.txt")) {
								if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
									enrichments.setEnrichments(params.repopulateHashmap(fullText, 3));
								else
									enrichments.setEnrichments(params.repopulateHashmap(fullText, 4));
							}	
						} 
						
						//have to keep this method just in case old session files have ranks saved in this way
						//it would only happen for sessions saved with version 0.8
						if (propFile.getName().contains(".RANKS2.txt")
								|| propFile.getName().contains(".RANKS2Genes.txt")) {
							Ranking new_ranking;
							
							// Check to see if there is already GSEARanking
							if (em.getDataSet(LegacySupport.DATASET2).getExpressionSets().getAllRanksNames()
									.contains(Ranking.GSEARanking)) {
								new_ranking = em.getDataSet(LegacySupport.DATASET2).getExpressionSets()
										.getRanksByName(Ranking.GSEARanking);
							} else {
								new_ranking = new Ranking();
								em.getDataSet(LegacySupport.DATASET2).getExpressionSets().addRanks(Ranking.GSEARanking,
										new_ranking);
							}
							
							if (propFile.getName().contains(".RANKS2.txt")) {
								Map<Integer, Rank> ranks = (Map<Integer, Rank>) params.repopulateHashmap(fullText, 6);
								ranks.forEach(new_ranking::addRank);
							}
						}
					}
				}
			}

			//load the expression files.  Load them last because they require
			//info from the parameters
			for (int i = 0; i < fileList.size(); i++){
				File prop_file = fileList.get(i);
				FileNameParts parts_exp = ParseFileName(prop_file);
				//unrecognized file
				if((parts_exp == null) || (parts_exp.name == null) )continue;

				CyNetwork net = getNetworkByName(parts_exp.name);
				EnrichmentMap map = net == null ? null : enrichmentMapMap.get(net.getSUID());
				EnrichmentMapParameters params = paramsMap.get(net.getSUID());
				Map<String,String> props = params.getProps();

				if (parts_exp.type != null && parts_exp.type.equalsIgnoreCase("expression")){
					if(map.getDataSets().containsKey(parts_exp.dataset)){
						EMDataSet ds = map.getDataSet(parts_exp.dataset);
						ExpressionFileReaderTask expressionFile1 = new ExpressionFileReaderTask(ds);
						GeneExpressionMatrix matrix = expressionFile1.parse();
						matrix.restoreProps(parts_exp.dataset, props);
					}
				}
				//Deal with legacy session files.
				if (prop_file.getName().contains("expression1.txt")){                  
					EMDataSet ds1 = map.getDataSet(LegacySupport.DATASET1);
					ExpressionFileReaderTask expressionFile1 = new ExpressionFileReaderTask(ds1);
					expressionFile1.parse();

				}
				if (prop_file.getName().contains("expression2.txt")){                    
					EMDataSet ds2 = map.getDataSet(LegacySupport.DATASET2);
					ExpressionFileReaderTask expressionFile2 = new ExpressionFileReaderTask(ds2);
					expressionFile2.parse();
					
					//if there are two expression sets and there is a second set of genesets of interest then we
					//are dealing with two distinct expression files.
					if (map.getDataSet(LegacySupport.DATASET2) != null && map.getDataSet(LegacySupport.DATASET2).getGeneSetsOfInterest() != null && !map.getDataSet(LegacySupport.DATASET2).getGeneSetsOfInterest().getGeneSets().isEmpty() ){
						map.setDistinctExpressionSets(true);
						map.getDataSet(LegacySupport.DATASET1).setDataSetGenes(new HashSet<Integer>((Set<Integer>)map.getDataSet(LegacySupport.DATASET1).getExpressionSets().getGeneIds()));
						map.getDataSet(LegacySupport.DATASET2).setDataSetGenes(new HashSet<Integer>((Set<Integer>)map.getDataSet(LegacySupport.DATASET2).getExpressionSets().getGeneIds()));
					}
				}
			}

			/* else if(params.getDataset1Rankings() != null){
                		params.setRank2geneDataset1(params.getRank2geneDataset(params.getDataset1Rankings()));
			 */

			//register the action listeners for all the networks.

			//iterate over the networks
			for (Iterator<Long> j = enrichmentMapMap.keySet().iterator(); j.hasNext();){
				Long id = j.next();
				EnrichmentMap map = enrichmentMapMap.get(id);
				
				//only initialize objects if there is a map for this network
				if (map != null){
					if(map.getDataSets().size() > 1) {
						Set<Integer> dataset1_genes = map.getDataSets().get(LegacySupport.DATASET1).getDataSetGenes();
						Set<Integer> dataset2_genes = map.getDataSets().get(LegacySupport.DATASET2).getDataSetGenes();
						
						if(!dataset1_genes.equals(dataset2_genes))
							map.setDistinctExpressionSets(true);
					}
					
					//initialize the Genesets (makes sure the leading edge is set correctly)
					//Initialize the set of genesets and GSEA results that we want to compute over
					InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(map);
					genesets_init.setThrowIfMissing(false); // MKTODO really?
					genesets_init.initializeSets(null);

//					//for each map compute the similarity matrix, (easier than storing it) compute the geneset similarities
//					ComputeSimilarityTask similarities = new ComputeSimilarityTask(map, ComputeSimilarityTask.ENRICHMENT);
//					Map<String, GenesetSimilarity> similarity_results = similarities.computeGenesetSimilarities(null);
//					map.setGenesetSimilarity(similarity_results);
//
//					// also compute geneset similarities between Enrichment- and Signature Genesets (if any)
//					if (! map.getSignatureGenesets().isEmpty()){
//						ComputeSimilarityTask sigSimilarities = new ComputeSimilarityTask(map, ComputeSimilarityTask.SIGNATURE);
//						Map<String, GenesetSimilarity> sig_similarity_results = sigSimilarities.computeGenesetSimilarities(null);
//						map.getGenesetSimilarity().putAll(sig_similarity_results);
//					}
				}//end of if(map != null)
			}
			
			for (Iterator<Long> j = enrichmentMapMap.keySet().iterator(); j.hasNext();) {
				Long id = j.next();
				CyNetwork currentNetwork = cyNetworkManager.getNetwork(id);
				EnrichmentMap map = enrichmentMapMap.get(id);
				map.setLegacy(true);
				emManager.registerEnrichmentMap(map);
				
				if (!j.hasNext()) {
					//set the last network to be the one viewed and initialize the parameters panel
					cyApplicationManager.setCurrentNetwork(currentNetwork);
				}
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}	
	}
	
	private FileNameParts ParseFileName(File filename){
		String fullname = (filename.getName());
		String name=null,type=null,dataset=null,ranks_name= null;
		if(fullname != null){                    
			String[] tokens = fullname.split("\\.");
			if((tokens.length ==2) && (tokens[1].equals("gmt"))){
				name = tokens[0];
				dataset = LegacySupport.DATASET1;
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
