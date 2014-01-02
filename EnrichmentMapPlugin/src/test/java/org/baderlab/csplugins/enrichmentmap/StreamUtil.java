package org.baderlab.csplugins.enrichmentmap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StreamUtil implements org.cytoscape.io.util.StreamUtil {
	
	public StreamUtil(){
		
	}
	
	public InputStream getInputStream(String filename) throws FileNotFoundException{
		return new FileInputStream(filename);
	}

	public InputStream getInputStream(URL url) throws IOException {
		
		return new FileInputStream(url.toString());
	}

	public URLConnection getURLConnection(URL arg0) throws IOException {
		
		return null;
	}

}
