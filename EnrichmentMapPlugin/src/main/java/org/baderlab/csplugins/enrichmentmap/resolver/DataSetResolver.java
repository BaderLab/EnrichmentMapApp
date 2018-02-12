package org.baderlab.csplugins.enrichmentmap.resolver;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;

import com.google.common.collect.ImmutableList;

public class DataSetResolver {
	
	public static enum Type {
		ENRICHMENT_BINGO,
		ENRICHMENT_DAVID,
		ENRICHMENT_GENERIC,
		ENRICHMENT_GREAT,
		ENRICHMENT_GSEA,
		GSEA_FOLDER,
		EXPRESSION,
		RANKS,
		CLASS,
		GENE_SETS,
		IGNORE;
		
		public boolean isEnrichmentFile() {
			return this.name().startsWith("ENRICHMENT");
		}
	}
	
	
	public static List<DataSetParameters> guessDataSets(Path rootFolder, CancelStatus cancelStatus) {
		if(cancelStatus == null) {
			cancelStatus = CancelStatus.notCancelable();
		}
		
		// First test if rootFolder is itself a GSEA results folder
		Optional<DataSetParameters> dataset = GSEAResolver.resolveGSEAResultsFolder(rootFolder);
		if(dataset.isPresent())
			return ImmutableList.of(dataset.get());
		
		if(cancelStatus.isCancelled())
			return Collections.emptyList();
		
		try(Stream<Path> contents = Files.list(rootFolder)) {
			Map<Type,List<Path>> types = new EnumMap<>(Type.class);
			for(Type type : Type.values()) {
				types.put(type, new ArrayList<>());
			}
			
			for(Path path : (Iterable<Path>)contents::iterator) {
				if(cancelStatus.isCancelled())
					return Collections.emptyList();
				
				Type type = guessType(path);
				types.get(type).add(path);
			}
			
			if(cancelStatus.isCancelled())
				return Collections.emptyList();
			
			return createDataSets(types);
		} catch(IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	
	private static List<DataSetParameters> createDataSets(Map<Type,List<Path>> types) {
		List<DataSetParameters> dataSets = new ArrayList<>();
		
		// All GSEA results are fine
		for(Path gseaFolder : types.get(Type.GSEA_FOLDER)) {
			Optional<DataSetParameters> gseaDataSet = GSEAResolver.resolveGSEAResultsFolder(gseaFolder);
			if(gseaDataSet.isPresent())
				dataSets.add(gseaDataSet.get());
		}
		
		// Now, iterate over Enrichments, and try to pair up with Ranks and Expressions
		// MKTODO add other enrichment types
		List<Path> exprFiles = new ArrayList<>(types.get(Type.EXPRESSION));
		List<Path> rankFiles = new ArrayList<>(types.get(Type.RANKS));
		List<Path> clasFiles = new ArrayList<>(types.get(Type.CLASS));
		List<Path> gmtFiles  = new ArrayList<>(types.get(Type.GENE_SETS));
		
		// MKTODO what about other enrichment types?
		for(Path enrichment : types.get(Type.ENRICHMENT_GENERIC)) {
			DataSetFiles files = new DataSetFiles();
			files.setEnrichmentFileName1(enrichment.toAbsolutePath().toString());
			
			Optional<Path> closestExpr  = findClosestMatch(enrichment, exprFiles);
			Optional<Path> closestRanks = findClosestMatch(enrichment, rankFiles);
			Optional<Path> closestClass = findClosestMatch(enrichment, clasFiles);
			Optional<Path> closestGmt   = findClosestMatch(enrichment, gmtFiles);
			
			closestExpr.ifPresent(path -> {
				exprFiles.remove(path);
				files.setExpressionFileName(path.toAbsolutePath().toString());
			});
			closestRanks.ifPresent(path -> {
				rankFiles.remove(path);
				files.setRankedFile(path.toAbsolutePath().toString());
			});
			closestClass.ifPresent(path -> {
				clasFiles.remove(path);
				files.setClassFile(path.toAbsolutePath().toString());
			});
			closestGmt.ifPresent(path -> {
				gmtFiles.remove(path);
				files.setGMTFileName(path.toAbsolutePath().toString());
			});
			
			String name = getDatasetNameGeneric(enrichment.getFileName());
			dataSets.add(new DataSetParameters(name, Method.Generic, files));
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
//		if(closest.isPresent()) {
//			int score = scores.get(closest.get());
//			if(score == 0) {
//				return Optional.empty();
//			}
//		}
		
		return closest;
	}
	
	
	
	public static Type guessType(Path path) {
		try {
			if(Files.isHidden(path)) 
				return Type.IGNORE;
		} catch (IOException e) {
			e.printStackTrace();
			return Type.IGNORE;
		}
		if(Files.isDirectory(path)) {
//			if(GSEAResolver.isGSEAResultsFolder(path)) {
//				return Type.GSEA_FOLDER;
//			} else {
				return Type.IGNORE;
//			}
		}
		
		return guess(path);
	}
	
	
	private static Type guess(Path path) {
		Map<Type,Integer> scores = new EnumMap<>(Type.class);
		
		String fileName = path.getFileName().toString();
		Optional<String> firstLine = getFirstDataLine(path);
		
		if(firstLine.isPresent() && isTabSeparated(firstLine.get())) {
			// Guess based on extension and/or first line of file
			if(hasExtension(path, "gct")) {
				addScore(scores, Type.RANKS, 1);
			}
			if(hasExtension(path, "gmt")) {
				addScore(scores, Type.GENE_SETS, 1);
			}
			if(hasExtension(path, "rnk")) {
				addScore(scores, Type.RANKS, 1);
				addScore(scores, Type.EXPRESSION, 1);
			}
			if(hasExtension(path, "xls", "bgo", "tsv", "txt")) {
				Type type = guessEnrichmentType(path);
				if(type == Type.IGNORE) {
					addScore(scores, Type.ENRICHMENT_GENERIC, 1);
					addScore(scores, Type.EXPRESSION, 1);
				} else {
					addScore(scores, type, 2); // this is a lot of evidence
				}
			}
			
			// Test first line
			if(!isRankLine(firstLine.get())) {
				addScore(scores, Type.RANKS, -1);
			}
			if(!isExpressionLine(firstLine.get())) {
				addScore(scores, Type.EXPRESSION, -1);
			}
			
			// Guess based on file name	
			
			if(matches(fileName, ".*expr(ession)?.*")) {
				addScore(scores, Type.EXPRESSION, 3);
			}
			if(matches(fileName, ".*rank.*")) {
				addScore(scores, Type.RANKS, 3);
			}
		}
		
		// class files are not tab separated
		if(matches(fileName, ".*class.*")) {
			addScore(scores, Type.CLASS, 3);
		}
		
		// Not adding score here for enrichment files because guessEnrichmentType should be enough evidence
		Set<Type> possibleTypes = typesWithHighestScore(scores);
		
		if(possibleTypes.isEmpty()) {
			return Type.IGNORE;
		}
		if(possibleTypes.size() == 1) {
			return possibleTypes.iterator().next();
		}
		
		// Here we hardcode a tiebreaker
		if(possibleTypes.contains(Type.EXPRESSION)) {
			return Type.EXPRESSION;
		}
		if(possibleTypes.contains(Type.RANKS)) {
			return Type.RANKS;
		}
		return possibleTypes.iterator().next();
	}
	
	private static <K> void addScore(Map<K,Integer> map, K key, int add) {
		map.merge(key, add, (x,y) -> x + y);
	}
	
	private static Set<Type> typesWithHighestScore(Map<Type,Integer> scores) {
		if(scores.isEmpty())
			return Collections.emptySet();
		
		int highestScore = Integer.MIN_VALUE;
		Set<Type> highest = new HashSet<>();
		
		for(Type type : scores.keySet()) {
			int score = scores.get(type);
			if(score == highestScore) {
				highest.add(type);
			}
			if(score > highestScore) {
				highestScore = score;
				highest = new HashSet<>();
				highest.add(type);
			}
		}
		return highest;
	}
	
	
	private static boolean matches(String s, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}
	
	
	private static boolean isDouble(String x) {
		try {
			Double.parseDouble(x);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	
	private static boolean hasExtension(Path path, String... extensions) {
		String fileName = path.getFileName().toString();
		for(String ext : extensions) {
			if(fileName.endsWith("." + ext.toLowerCase()) || fileName.endsWith("." + ext.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
	
	
	private static Optional<String> getFirstDataLine(Path path) {
		try(Stream<String> lines = Files.lines(path)) {
			return lines
					.filter(l -> !l.startsWith("#")) // filter out comment lines
					.skip(1)                         // skip header line
					.findFirst();
		} catch(IOException | UncheckedIOException e) {
			return Optional.empty();
		}
	}
	
	private static boolean isExpressionLine(String line) {
		String[] tokens = line.split("\t");
		if(tokens.length > 2) {
			return Arrays.stream(tokens).skip(2).allMatch(DataSetResolver::isDouble);
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
	
	private static boolean isTabSeparated(String line) {
		return line.indexOf("\t") != -1;
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
	
	
	public static String getDatasetNameGeneric(Path file) {
		String name = file.getFileName().toString();
		if(name.contains(".")) 
			return name.substring(0, name.lastIndexOf('.'));
		else
			return name;
	}
	
}
