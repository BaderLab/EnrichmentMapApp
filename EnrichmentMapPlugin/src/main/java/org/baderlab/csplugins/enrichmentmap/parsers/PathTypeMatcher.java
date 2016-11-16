package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.DataSetParameters;

public class PathTypeMatcher {
	
	public static enum Type {
		ENRICHMENT_BINGO,
		ENRICHMENT_DAVID,
		ENRICHMENT_GENERIC,
		ENRICHMENT_GREAT,
		ENRICHMENT_GSEA,
		GSEA_FOLDER,
		EXPRESSION,
		RANKS,
		IGNORE;
		
		public boolean isEnrichmentFile() {
			return this.name().startsWith("ENRICHMENT");
		}
	}
	
	
	public static List<DataSetParameters> guessDataSets(Path rootFolder) {
		try(Stream<Path> contents = Files.list(rootFolder)) {
			
			Map<Type,List<Path>> types = new EnumMap<>(Type.class);
			for(Type type : Type.values()) {
				types.put(type, new ArrayList<>());
			}
			
			for(Path path : (Iterable<Path>)contents::iterator) {
				Type type = guessType(path);
				types.get(type).add(path);
			}
			
			return createDataSets(types);
		} catch(IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	
	private static List<DataSetParameters> createDataSets(Map<Type,List<Path>> types) {
		System.out.println(types);
		
		List<DataSetParameters> dataSets = new ArrayList<>();
		
		// All GSEA results are fine
		for(Path gseaFolder : types.get(Type.GSEA_FOLDER)) {
			DataSetParameters gseaDataSet = toDataSetParametersGSEA(gseaFolder);
			dataSets.add(gseaDataSet);
		}
		
		// Now, iterate over Enrichments, and try to pair up with Ranks and Expressions
		// MKTODO add other enrichment types
		List<Path> expressionFiles = new ArrayList<>(types.get(Type.EXPRESSION));
		List<Path> rankFiles       = new ArrayList<>(types.get(Type.RANKS));
		
		for(Path enrichment : types.get(Type.ENRICHMENT_GENERIC)) {
			DataSetFiles files = new DataSetFiles();
			files.setEnrichmentFileName1(enrichment.toAbsolutePath().toString());
			
			Optional<Path> closestExpression = findClosestMatch(enrichment, expressionFiles);
			Optional<Path> closestRanks      = findClosestMatch(enrichment, rankFiles);
			
			closestExpression.ifPresent(path -> {
				expressionFiles.remove(path);
				files.setExpressionFileName(path.toAbsolutePath().toString());
			});
			closestRanks.ifPresent(path -> {
				rankFiles.remove(path);
				files.setRankedFile(path.toAbsolutePath().toString());
			});
			
			String name = getDatasetNameGeneric(enrichment.getFileName());
			dataSets.add(new DataSetParameters(name, files));
		}
		
		return dataSets;
	}
	
	
	private static Optional<Path> findClosestMatch(Path p, List<Path> candidates) {
		String pf = p.getFileName().toString();
		
		Map<Path,Integer> scores = new HashMap<>();
		for(Path candidate : candidates) {
			String filename = candidate.getFileName().toString();
			int score1 = StringUtils.getFuzzyDistance(pf, filename, Locale.getDefault());
			scores.put(candidate, score1);
		}
		
		// Find closest match by using edit distance on file name;
		Optional<Path> closest = candidates.stream().reduce(BinaryOperator.maxBy(Comparator.comparing(scores::get)));
		
		// There should be a threshold for considering the path a match
		// MKTODO can this heuristic be improved?
		if(closest.isPresent()) {
			int score = scores.get(closest.get());
			if(score == 0) {
				return Optional.empty();
			}
		}
		
		return closest;
	}
	
	
	
	public static Type guessType(Path path) {
		if(Files.isDirectory(path)) {
			if(isGSEAResultsFolder(path)) {
				return Type.GSEA_FOLDER;
			} else {
				return Type.IGNORE;
			}
		}
		if(maybeRankFile(path))
			return Type.RANKS;
		if(maybeExpressionFile(path))
			return Type.EXPRESSION;
		return guessEnrichmentType(path);
	}
	
	
	private static boolean isDouble(String x) {
		try {
			Double.parseDouble(x);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	private static boolean maybeExpressionFile(Path path) {
		if(path.getFileName().toString().toLowerCase().contains("expr"))
			return true;
		return matchFirstDataLine(path, PathTypeMatcher::isRankLine);
	}
	
	private static boolean maybeRankFile(Path path) {
		if(path.getFileName().endsWith(".rnk"))
			return true;
		if(path.getFileName().toString().toLowerCase().contains("rank"))
			return true;
		return matchFirstDataLine(path, PathTypeMatcher::isExpressionLine);
	}
	
	private static boolean matchFirstDataLine(Path path, Function<String,Boolean> matcher) {
		try(Stream<String> lines = Files.lines(path)) {
			return lines
				.filter(l -> !l.startsWith("#")) // filter out comment lines
				.skip(1)                         // skip the header line
				.findFirst()                     // take the first real line of the file
				.map(matcher)                    // check if it matches
				.orElse(false);                  // return result, or false if the file has no real lines
			
		} catch(IOException | UncheckedIOException e) {
			return false;
		}
	}
	
	private static boolean isExpressionLine(String line) {
		String[] tokens = line.split("\t");
		if(tokens.length > 2) {
			return Arrays.stream(tokens).skip(2).allMatch(PathTypeMatcher::isDouble);
		} else if(tokens.length == 2) {
			return isDouble(tokens[1]);
		} else {
			return false;
		}
	}
	
	private static boolean isRankLine(String line) {
		String[] tokens = line.split("\t");
		if(tokens.length == 5) {
			return isDouble(tokens[4]);
		} else if(tokens.length == 2) {
			return isDouble(tokens[1]);
		} else {
			return false;
		}
	}
	
	
	public static Type guessEnrichmentType(String path) {
		return guessEnrichmentType(Paths.get(path));
	}
	
	/*
	 * This logic was moved here from {@link DetermineEnrichmentResultFileReader}
	 */
	public static Type guessEnrichmentType(Path path) {
		try {
			String firstLine = com.google.common.io.Files.readFirstLine(path.toFile(), Charset.defaultCharset());
			
			String[] tokens = firstLine.split("\t");

			//check to see if there are exactly 11 columns - = GSEA results
			if(tokens.length == 11) {
				//check to see if the ES is the 5th column and that NES is the 6th column
				if((tokens[4].equalsIgnoreCase("ES")) && (tokens[5].equalsIgnoreCase("NES")))
					return Type.ENRICHMENT_GSEA;
				//it is possible that the file can have 11 columns but that it is still a generic file
				//if it doesn't specify ES and NES in the 5 and 6th columns
				else
					return Type.ENRICHMENT_GENERIC;
			}
			//check to see if there are exactly 13 columns - = DAVID results
			else if(tokens.length == 13) {
				//check to see that the 6th column is called Genes and that the 12th column is called "Benjamini"
				if((tokens[5].equalsIgnoreCase("Genes")) && tokens[11].equalsIgnoreCase("Benjamini"))
					return Type.ENRICHMENT_DAVID;
				else
					return Type.ENRICHMENT_GENERIC;

			}
			//fix bug with new version of bingo plugin change the case of the header file.
			else if(firstLine.toLowerCase().contains("File created with BiNGO".toLowerCase())) {
				return Type.ENRICHMENT_BINGO;
			} else if(firstLine.contains("GREAT version")) {
				return Type.ENRICHMENT_GREAT;
			} else {
				return Type.ENRICHMENT_GENERIC;
			}
		}
		catch(IOException e) {
			// MKTODO log the exception
		}
		
		return Type.IGNORE;
	}
	

	public static boolean isGSEAResultsFolder(Path p) {
		Path edbPath = p.resolve("edb");
		
		try {
			if(!Files.exists(edbPath)) {
				return false;
			}
			if(!containsFileEndingWith(edbPath, ".rnk")) {
				return false;
			}
			if(!containsFileEndingWith(edbPath, ".gmt")) {
				return false;
			}
			if(!containsFileEndingWith(edbPath, ".edb")) {
				return false;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private static boolean containsFileEndingWith(Path p, String suffix) throws IOException {
		return Files.find(p, 1, (path, attributes) -> {
			return path.getFileName().toString().endsWith(suffix);
		}).limit(1).count() > 0;
	}
	
	
	
	public static String getDatasetNameGSEA(Path folder) {
		String folderName = folder.getFileName().toString();
		int dotIndex = folderName.indexOf('.');
		if(dotIndex == -1)
			return folderName;
		else
			return folderName.substring(0, dotIndex);
	}
	
	public static String getDatasetNameGeneric(Path file) {
		String name = file.getFileName().toString();
		if(name.contains(".")) 
			return name.substring(0, name.lastIndexOf('.'));
		else
			return name;
	}
	
	public static DataSetFiles toDataSetFilesGSEA(Path path) {
		DataSetFiles files = new DataSetFiles();
		files.setEnrichmentFileName1(path.resolve(Paths.get("edb/results.edb")).toString());
		files.setGMTFileName(path.resolve(Paths.get("edb/gene_sets.gmt")).toString());
		return files;
	}
	
	
	public static DataSetParameters toDataSetParametersGSEA(Path path) {
		return new DataSetParameters(getDatasetNameGSEA(path), toDataSetFilesGSEA(path));
	}
	
}
