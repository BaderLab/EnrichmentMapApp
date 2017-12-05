package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.function.Consumer;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * The reason this is a Task and doesn't just use Files.copy(...) is because
 * I want the download to be cancellable.
 */
public class DownloadGMTFileTask extends AbstractTask {

	private final URL url;
	private final Consumer<String> filePathConsumer;
	
	public DownloadGMTFileTask(URL url, Consumer<String> filePathConsumer) {
		this.url = url;
		this.filePathConsumer = filePathConsumer;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Downloading GMT file");
		
		try {
			File tempFile = File.createTempFile("baderlab-download-gmt", ".tmp");
			tempFile.deleteOnExit();
			download(tempFile);
			
			if(filePathConsumer != null)
				filePathConsumer.accept(tempFile.toString());
			
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error while downloading from " + url, e);
		}
	}

	
	private void download(File file) throws IOException {
		try (
			InputStream in = new BufferedInputStream(url.openStream());
			OutputStream out = new FileOutputStream(file)
		) {
			byte[] bytes = new byte[1024];
			int count;
			while((count = in.read(bytes)) != -1) {
				if(cancelled) {
					return;
				}
				out.write(bytes, 0, count);
			}
		}
	}


}
