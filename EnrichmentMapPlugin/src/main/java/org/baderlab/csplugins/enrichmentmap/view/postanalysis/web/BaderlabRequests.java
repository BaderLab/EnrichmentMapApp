package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BaderlabRequests {
	
	private static final String DATE_FOLDER_URL = "http://download.baderlab.org/EM_Genesets/cytoscape_list_dirs.php";
	private static final String FILE_LIST_URL   = "http://download.baderlab.org/EM_Genesets/cytoscape_list_genesets.php?folder=";
	
	
	public static List<DateDir> requestDateFolders() throws IOException {
		URL url = new URL(DATE_FOLDER_URL);
		return requestAndParse(url, new TypeToken<List<DateDir>>(){}.getType());
	}
	
	
	public static List<String> requestFiles(String dateFolder) throws IOException {
		URL url = new URL(FILE_LIST_URL + URLEncoder.encode(dateFolder));
		return requestAndParse(url, new TypeToken<List<String>>(){}.getType());
	}

	
	private static <T> T requestAndParse(URL url, Type type) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(60000);
		connection.setReadTimeout(60000);
		
		int code = connection.getResponseCode();
		if(code != 200) {
			throw new RuntimeException("Request error, status: " + code);
		}
		
		try(InputStream in = connection.getInputStream()) {
			Reader reader = new BufferedReader(new InputStreamReader(in));
			T results = new Gson().fromJson(reader, type);
			return results;
		}
	}
	
	public static void downloadFile(String dateFolder, String file) {
		
	}
}
