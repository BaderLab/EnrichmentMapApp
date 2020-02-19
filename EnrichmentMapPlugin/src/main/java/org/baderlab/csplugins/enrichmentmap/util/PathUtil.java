package org.baderlab.csplugins.enrichmentmap.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;

public class PathUtil {

	private PathUtil() {}
	
	public static List<Path> dataSetsRoots(Collection<DataSetParameters> dataSets) {
		return dataSets.stream()
				.map(DataSetParameters::getFilePaths)
				.map(PathUtil::commonRoot)
				.collect(Collectors.toList());
	}
	
	public static Path commonRoot(List<Path> paths) {
		if(paths == null || paths.isEmpty())
			return null;
		if(paths.size() == 1)
			return paths.get(0);
		
		Path common = paths.get(0);
		for(Path path : paths.subList(1, paths.size())) {
			common = commonRoot(common, path);
			if(common == null) {
				return null;
			}
		}
		return common;
	}
	
	
	public static Path commonRoot(Path p1, Path p2) {
		if(p1 == null || p2 == null)
			return null;
		Path common;
	    if(p1.isAbsolute() && p2.isAbsolute() && p1.getRoot().equals(p2.getRoot()))
	    	common = p1.getRoot();
	    else if(!p1.isAbsolute() && !p2.isAbsolute())
	    	common = Paths.get("");
	    else
	    	return null;
	    
		int n = Math.min(p1.getNameCount(), p2.getNameCount());
		for(int i = 0; i < n; i++) {
			Path name1 = p1.getName(i);
			Path name2 = p2.getName(i);
			if(!name1.equals(name2))
				break;
			common = common.resolve(name1);
		}
		return common;
	}
}
