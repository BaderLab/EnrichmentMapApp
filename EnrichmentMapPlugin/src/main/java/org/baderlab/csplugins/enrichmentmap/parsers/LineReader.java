package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineReader implements AutoCloseable {
	
	private int lineNumber = -1;
	private final BufferedReader reader;
	
	
	private LineReader(String fileName) throws IOException {
		reader = getBufferedReader(fileName);
	}
	
	private static BufferedReader getBufferedReader(String fileName) throws IOException {
		return new BufferedReader(new FileReader(fileName));
	}
	
	public boolean hasMoreLines() throws IOException {
		return reader.ready();
	}
	
	public String nextLine() throws IOException {
		String line = reader.readLine();
		lineNumber++;
		return line;
	}
	
	public void skip(int n) throws IOException {
		while(n-- > 0 && hasMoreLines()) {
			nextLine();
		}
	}
	
	@Override
	public void close() throws IOException {
		reader.close();
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
