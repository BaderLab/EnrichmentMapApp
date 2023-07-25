package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class LineReader implements AutoCloseable {
	
	private Stream<String> lineStream;
	private Iterator<String> lineIterator;
	
	private int lineNumber = -1;
	
	
	private LineReader(String fileName) throws IOException {
		Path path = new File(fileName).toPath();
		
		lineStream = 
			Files.lines(path)
				.map(line -> {
					lineNumber++;
					return line;
				})
				.filter(line -> !line.isBlank());
				
		lineIterator = lineStream.iterator();
	}
	
	public boolean hasMoreLines() {
		return lineIterator.hasNext();
	}
	
	public String nextLine() {
		return lineIterator.next();
	}
	
	public void skip(int n) {
		while(n-- > 0 && hasMoreLines()) {
			nextLine();
		}
	}
	
	@Override
	public void close() {
		lineStream.close();
	}
	
	/**
	 * Returns the number of the line that was last read, starting from 1.
	 */
	public int getLineNumber() {
		return lineNumber + 1;
	}
	
	
	public static LineReader create(String fileName) throws IOException {
		return new LineReader(fileName);
	}
	
	
	// MKTODO Temporary
	public static List<String> readAllLines(String fileName, int limit) throws IOException {
		try(var fileReader = new FileReader(fileName);
			var reader = new BufferedReader(fileReader)) {
			
			List<String> lines = new ArrayList<>();
			int count = 0;
            for(String line; (line = reader.readLine()) != null;) {
                lines.add(line);
                count++;
                if(count >= limit) {
                	break;
                }
            }
            return lines;
        }
	}
	
	
	// Todo remove the below methods
	
	public static List<String> readLines(String fileName, int limit) throws IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			List<String> lines = new ArrayList<>();
			int count = 0;
            for(String line; (line = reader.readLine()) != null;) {
                lines.add(line);
                count++;
                if(count >= limit) {
                	break;
                }
            }
            return lines;
        }
	}
	
	public static List<String> readLines(String fileName) throws IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			List<String> lines = new ArrayList<>();
            for(String line; (line = reader.readLine()) != null;) {
                lines.add(line);
            }
            return lines;
        }
	}

	
}
