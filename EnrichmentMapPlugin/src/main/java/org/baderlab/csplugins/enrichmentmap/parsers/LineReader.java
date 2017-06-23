package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineReader {

	// MKTODO Its not very memory efficient to read the entire file into memory. Replace with streams.
	
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
