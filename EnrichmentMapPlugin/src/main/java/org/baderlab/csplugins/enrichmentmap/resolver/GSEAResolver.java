package org.baderlab.csplugins.enrichmentmap.resolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.parsers.LineReader;

public class GSEAResolver {

	
	/**
	 * Attempts to create a DataSetParameters object from a GSEA results folder.
	 */
	public static Optional<DataSetParameters> resolveGSEAResultsFolder(Path gseaFolder) {
		try {
			Optional<DataSetParameters> rpt = resolveRPTFromGSEAFolder(gseaFolder);
			if(rpt.isPresent())
				return rpt;
			Optional<DataSetParameters> edb = resolveEDBFromGSEAFolder(gseaFolder);
			if(edb.isPresent())
				return edb;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	
	
	public static Optional<DataSetParameters> resolveRPTFromGSEAFolder(Path gseaFolder) throws IOException {
		Optional<Path> rptFileOpt = getFileEndingWith(gseaFolder, ".rpt");
		if(!rptFileOpt.isPresent())
			return Optional.empty();
		return resolveRPT(gseaFolder, rptFileOpt.get());
	}
		
	public static Optional<DataSetParameters> resolveRPTFile(Path rptFile) throws IOException {
		Path gseaFolder = rptFile.getParent();
		return resolveRPT(gseaFolder, rptFile);
	}
	
	private static Optional<DataSetParameters> resolveRPT(Path gseaFolder, Path rptFile) {
		Optional<Map<String,String>> optParams = parseRPTParameters(rptFile);
		if(!optParams.isPresent())
			return Optional.empty();

		Map<String,String> params = optParams.get();
		String timestamp = params.get("producer_timestamp");

		// Attempt to resolve the files from the RPT
		Optional<Path> gmtPath  = getRptGmt(gseaFolder, params);
		String[] phenotypes     = getRptPhenotypes(params);
		Optional<Path> classes  = getRptClassFile(gseaFolder, params);
		
		String[] extns = { ".xls", ".tsv" }; // .xls was changed to .tsv in GSEA 4.1
		
		String results1BaseName = "gsea_report_for_" + phenotypes[0] + "_" + timestamp;
		String results2BaseName = "gsea_report_for_" + phenotypes[1] + "_" + timestamp;
		
		Optional<Path> results1 = getRptResultsFile(gseaFolder, results1BaseName, extns, params);
		Optional<Path> results2 = getRptResultsFile(gseaFolder, results2BaseName, extns, params);
		
		
		if((!results1.isPresent() || !results2.isPresent()) && classes.isPresent()) {
			// try again using the values from the class file
			Optional<String[]> phenotypesFromClassFile = parseClasses(classes.get());
			if(phenotypesFromClassFile.isPresent()) {
				phenotypes = phenotypesFromClassFile.get();
				
				results1BaseName = "gsea_report_for_" + phenotypes[0] + "_" + timestamp;
				results2BaseName = "gsea_report_for_" + phenotypes[1] + "_" + timestamp;
				
				results1 = getRptResultsFile(gseaFolder, results1BaseName, extns, params);
				results2 = getRptResultsFile(gseaFolder, results2BaseName, extns, params);
			}
		}
		
		String rnkBaseName  = "ranked_gene_list_" + phenotypes[0] + "_versus_" + phenotypes[1] +"_" + timestamp;
		Optional<Path> rnk  = getRptResultsFile(gseaFolder, rnkBaseName, extns, params);
		Optional<Path> expr = getRptExpressionFile(gseaFolder, params);
		
		
		if(!gmtPath.isPresent() && !results1.isPresent() && !results2.isPresent() && !rnk.isPresent() && !expr.isPresent())
			return Optional.empty();
		
		DataSetFiles files = new DataSetFiles();
		files.setPhenotype1(phenotypes[0]);
		files.setPhenotype2(phenotypes[1]);
		gmtPath.ifPresent(path -> files.setGMTFileName(path.toString()));
		results1.ifPresent(path -> files.setEnrichmentFileName1(path.toString()));
		results2.ifPresent(path -> files.setEnrichmentFileName2(path.toString()));
		rnk.ifPresent(path -> files.setRankedFile(path.toString()));
		expr.ifPresent(path -> files.setExpressionFileName(path.toString()));
		classes.ifPresent(path -> files.setClassFile(path.toString()));
		
		String datasetName = getDatasetNameGSEA(gseaFolder, timestamp);
		return Optional.of(new DataSetParameters(datasetName, Method.GSEA, files));
	}
	
	private static Optional<String[]> parseClasses(Path classFile) {
		try {
			List<String> lines = LineReader.readLines(classFile.toString(), 4);
			if(lines.size() >= 2) {
				String[] tokens = lines.get(1).split("\\s");
				if(tokens.length >= 3) {
					return Optional.of(new String[] { tokens[1], tokens[2] });
				}
			}
		} catch (IOException e) { }
		return Optional.empty();
	}
	
	private static Optional<Path> getRptExpressionFile(Path root, Map<String,String> params) {
		String method = params.get("producer_class").split("\\p{Punct}")[2]; // Gsea or GseaPreranked
		String data;
		if(method.equalsIgnoreCase("Gsea")) {
			data = params.get("param res");
		} else if(method.equalsIgnoreCase("GseaPreranked")) {
			data = params.get("param rnk");
			if(params.containsKey("param expressionMatrix")) {
                data = params.get("param expressionMatrix");
            }
		} else {
			return Optional.empty();
		}
		
		if(data == null)
			return Optional.empty();
		
		try {
			Path exprfile = Paths.get(data);
			if(Files.exists(exprfile))
				return Optional.of(exprfile);
			
			Path exprFileName = exprfile.getFileName();
			
			Path exprGuess1 = root.resolve(exprFileName);
			if(Files.exists(exprGuess1))
				return Optional.of(exprGuess1);
			
			Path exprGuess2 = root.getParent().resolve(exprFileName);
			if(Files.exists(exprGuess2))
				return Optional.of(exprGuess2);
			
		} catch(InvalidPathException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	
	private static Optional<Path> getRptResultsFile(Path root, String baseName, String[] extensions, Map<String,String> params) {
		// RPT files contain absolute paths from the machine where the GSEA analysis was run.
		// If the user moves the GSEA folder somewhere else then the paths won't resolve.
		// We can still attempt to find the files in the same folder where the RPT file is located.
		String label     = params.get("param rpt_label");
		String method    = params.get("producer_class").split("\\p{Punct}")[2]; // Gsea or GseaPreranked
		String timestamp = params.get("producer_timestamp");
		String out_dir   = params.get("param out");
		
		String job_dir_name = label + "." + method + "." + timestamp;
		
		// attempt to find the file using the path in the RPT file
		for(String ext : extensions) {
			String fileName = baseName + ext;
			try {
				Path abs = Paths.get(out_dir, job_dir_name, fileName);
				if(Files.exists(abs)) {
					return Optional.of(abs);
				}
			} catch(InvalidPathException e) {
				e.printStackTrace();
			}
		
			try {
				// attempt to find the file under the folder containing the RPT file
				Path rel = root.resolve(fileName);
				if(Files.exists(rel)) {
					return Optional.of(rel);
				}
			} catch(InvalidPathException e) {
				e.printStackTrace();
			}
		}
		
		return Optional.empty();
	}
	
	
	private static Optional<Map<String,String>> parseRPTParameters(Path rptFile) {
		try(Stream<String> stream = Files.lines(rptFile)) {
			Map<String,String> params = new HashMap<>();
			for(String line : (Iterable<String>)stream::iterator) {
				String[] tokens = line.split("\t");
				if(tokens.length == 2)
					params.put(tokens[0] ,tokens[1]);
				else if(tokens.length == 3)
					params.put(tokens[0] + " " + tokens[1], tokens[2]);
			}
			return Optional.of(params);
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
	
	
	private static Optional<Path> getRptGmt(Path root, Map<String,String> params) {
		// Use the original gmt if we can find it.  
		// If we can't find it resort to using the the one from edb directory.
		String gmtParam = params.get("param gmx");
		try {
			Path rptGmtPath = Paths.get(gmtParam);
			if(Files.exists(rptGmtPath))
				return Optional.of(rptGmtPath);
			
			// Search up the tree a few levels
			Path gmtFileName = rptGmtPath.getFileName();
			
			Path gmtGuess1 = root.resolve(gmtFileName);
			if(Files.exists(gmtGuess1))
				return Optional.of(gmtGuess1);
			
			Path gmtGuess2 = root.getParent().resolve(gmtFileName);
			if(Files.exists(gmtGuess2))
				return Optional.of(gmtGuess2);
			
			Path edbGmtPath = root.resolve("edb/gene_sets.gmt");
			if(Files.exists(edbGmtPath))
				return Optional.of(edbGmtPath);
			
		} catch(InvalidPathException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	
	private static String[] getRptPhenotypes(Map<String, String> params) {
		String classes = params.get("param cls");
		String method = params.get("producer_class").split("\\p{Punct}")[2]; // Gsea or GseaPreranked
		String phenotype1 = "na";
		String phenotype2 = "na";

		if(classes != null && method.equalsIgnoreCase("Gsea")) {
			String[] classes_split = classes.split("#");
			// only and try parse classes out of label if they are there
			if(classes_split.length >= 2) {
				String phenotypes = classes_split[1];
				String[] phenotypes_split = phenotypes.split("_versus_");
				if(phenotypes_split.length >= 2) {
					phenotype1 = phenotypes_split[0];
					phenotype2 = phenotypes_split[1];
				}
				else if(phenotypes_split.length == 1) {
					phenotype1 = phenotypes_split[0] + "_pos";
					phenotype2 = phenotypes_split[0] + "_neg";
				}
			}
		} else if(method.equalsIgnoreCase("GseaPreranked")) {
			phenotype1 = "na_pos";
			phenotype2 = "na_neg";
			if(params.containsKey("param phenotypes")) {
				String phenotypes = params.get("param phenotypes");
				String[] phenotypes_split = phenotypes.split("_versus_");
				if (phenotypes_split.length >= 2) {
					phenotype1 = phenotypes_split[0];
					phenotype2 = phenotypes_split[1];
				}
			}
		}
		
		return new String[] {phenotype1, phenotype2};
	}
	
	
	private static Optional<Path> getRptClassFile(Path root, Map<String, String> params) {
		String classes = params.get("param cls");
		String method = params.get("producer_class").split("\\p{Punct}")[2]; // Gsea or GseaPreranked

		if (classes != null && method.equalsIgnoreCase("Gsea")) {
			String[] classes_split = classes.split("#");
			try {
				Path path = Paths.get(classes_split[0]);
				if(Files.exists(path)) {
					return Optional.of(path);
				}
				
				Path clsFileName = path.getFileName();
				
				Path clsGuess1 = root.resolve(clsFileName);
				if(Files.exists(clsGuess1))
					return Optional.of(clsGuess1);
				
				Path edbGuess = root.resolve("edb").resolve(clsFileName);
				if(Files.exists(edbGuess))
					return Optional.of(edbGuess);
				
			} catch(InvalidPathException e) {
				e.printStackTrace();
			}
		}		
		return Optional.empty();
	}
	
	
	
	
	private static Optional<DataSetParameters> resolveEDBFromGSEAFolder(Path root) {
		if(hasEdbData(root))
			return Optional.of(toDataSetParametersEDB(root));
		return Optional.empty();
	}
	
	private static boolean hasEdbData(Path p) {
		Path edbPath = p.resolve("edb");
		try {
			return Files.exists(edbPath)
				&& containsFileEndingWith(edbPath, ".rnk")
				&& containsFileEndingWith(edbPath, ".gmt")
				&& containsFileEndingWith(edbPath, ".edb");
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean isGSEAResultsFolder(Path p) {
		return hasEdbData(p);
	}
	
	
	public static DataSetFiles toDataSetFilesEDB(Path path) {
		DataSetFiles files = new DataSetFiles();
		files.setEnrichmentFileName1(path.resolve(Paths.get("edb/results.edb")).toString());
		files.setGMTFileName(path.resolve(Paths.get("edb/gene_sets.gmt")).toString());
		return files;
	}
	
	
	public static DataSetParameters toDataSetParametersEDB(Path root) {
		return new DataSetParameters(getDatasetNameGSEA(root), Method.GSEA, toDataSetFilesEDB(root));
	}
	
	
	public static String getDatasetNameGSEA(Path folder, String timestamp) {
		String folderName = folder.getFileName().toString();
		if(folderName.endsWith("." + timestamp))
			folderName = folderName.substring(0, folderName.length() - timestamp.length()-1);
		if(folderName.regionMatches(true, folderName.length() - 5, ".gsea", 0, 5))
			folderName = folderName.substring(0, folderName.length() - 5);
		return folderName;
	}
	
	public static String getDatasetNameGSEA(Path folder) {
		String folderName = folder.getFileName().toString();
		int dotIndex = folderName.indexOf('.');
		if(dotIndex == -1)
			return folderName;
		else
			return folderName.substring(0, dotIndex);
	}
	
	
	
	private static boolean containsFileEndingWith(Path p, String suffix) throws IOException {
		return getFileEndingWith(p, suffix).isPresent();
	}
	
	private static Optional<Path> getFileEndingWith(Path p, String suffix) throws IOException {
		return Files.find(p, 1, (path, attrs) ->
			endsWithIgnoreCase(path.getFileName().toString(), suffix)
		).findFirst();
	}
	
	private static boolean endsWithIgnoreCase(String s, String suffix) {
		return s.regionMatches(true, s.length()-suffix.length(), suffix, 0, suffix.length());
	}
}
