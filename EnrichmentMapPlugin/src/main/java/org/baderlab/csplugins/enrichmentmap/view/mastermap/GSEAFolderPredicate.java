package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class GSEAFolderPredicate implements Predicate<Path> {

	@Override
	public boolean test(Path p) {
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
	
	private boolean containsFileEndingWith(Path p, String suffix) throws IOException {
		return Files.find(p, 1, (path, attributes) -> {
			return path.getFileName().toString().endsWith(suffix);
		}).limit(1).count() > 0;
	}
}
