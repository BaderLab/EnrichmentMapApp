package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.nio.file.Path;

public class GmtFile {

	private final Path filePath;
	private final int size;
	
	public GmtFile(Path filePath, int size) {
		this.filePath = filePath;
		this.size = size;
	}

	public Path getPath() {
		return filePath;
	}

	public int getSize() {
		return size;
	}
	
}
