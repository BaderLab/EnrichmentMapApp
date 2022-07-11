package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.swing.JFileChooser;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

import com.google.inject.Inject;

/**
 * Because the Cytoscape FileUtil service doesn't let you browse for directories :(
 */
public class FileBrowser {
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	

	public static enum Filter {
		GMT("GMT Files",
			new FileChooserFilter("gmt Files", "gmt"),
			new FileChooserFilter("txt Files", "txt")
		),
		ENRICHMENT("Enrichment Files",
			new FileChooserFilter("gct Files", "xls"),
			new FileChooserFilter("rnk Files", "bgo"),
			new FileChooserFilter("txt Files", "txt"),
			new FileChooserFilter("tsv Files", "tsv"),
			new FileChooserFilter("edb Files", "edb")
		),
		EXPRESSION("Expression Files",
			new FileChooserFilter("gct Files", "gct"),
			new FileChooserFilter("txt Files", "txt")
		), 
		RANK("Rank Files",
			new FileChooserFilter("rnk Files", "rnk"),
			new FileChooserFilter("txt Files", "txt")
		), 
		CLASS("Class (Phenotype) Files",
			new FileChooserFilter("cls Files", "cls"),
			new FileChooserFilter("txt Files", "txt")
		),
		RPT("RPT Files",
			new FileChooserFilter("rpt Files", "rpt")
		);
		
		public final String title;
		private final List<FileChooserFilter> filters;
		
		public List<FileChooserFilter> getFilters() {
			return filters;
		}
		
		private Filter(String title, FileChooserFilter... filters) {
			this.title = title;
			this.filters = Arrays.asList(filters);
		}
	}
	
	
	public static Optional<Path> browse(FileUtil fileUtil, Component parent, Filter filter) {
		File file = fileUtil.getFile(parent, filter.title, FileUtil.LOAD, filter.getFilters());
		return Optional.ofNullable(file).map(File::toPath);
	}
	
	
	public Optional<File> browseForRootFolder(Component parent) {
		final String osName = System.getProperty("os.name");
		if(osName.startsWith("Mac"))
			return browseForRootFolderMac(parent);
		else
			return browseForRootFolderSwing(parent);
	}
	
	
	private Optional<File> browseForRootFolderMac(Component parent) {
		final String property = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		try {
			FileDialog chooser;
			if(parent instanceof Dialog)
				chooser = new FileDialog((Dialog)parent, "Choose Root Folder", FileDialog.LOAD);
			else if(parent instanceof Frame)
				chooser = new FileDialog((Frame)parent, "Choose Root Folder", FileDialog.LOAD);
			else
				throw new IllegalArgumentException("parent must be Dialog or Frame");
			
			chooser.setDirectory(getCurrentDirectory().getAbsolutePath());
			
			chooser.setModal(true);
			chooser.setLocationRelativeTo(parent);
			chooser.setVisible(true);
			
			String file = chooser.getFile();
			String dir = chooser.getDirectory();
			
			if(file == null || dir == null) {
				return Optional.empty();
			}
			
			setCurrentDirectory(new File(dir));
			return Optional.of(new File(dir + File.separator + file));
		} finally {
			if(property != null) {
				System.setProperty("apple.awt.fileDialogForDirectories", property);
			}
		}
	}
	
	private Optional<File> browseForRootFolderSwing(Component parent) {
		JFileChooser chooser = new JFileChooser(getCurrentDirectory());
		chooser.setDialogTitle("Select Root Folder");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			setCurrentDirectory(chooser.getCurrentDirectory());
			return Optional.of(chooser.getSelectedFile());
		}
		return Optional.empty();
	}
	
	
	public static Optional<File> promptForPdfExport(FileUtil fileUtil, Component parent) {
		List<FileChooserFilter> filter = Collections.singletonList(new FileChooserFilter("pdf Files", "pdf"));
		File file = fileUtil.getFile(parent, "Export as PDF File", FileUtil.SAVE, filter);
		if(file != null) {
			String fileName = file.toString();
			if(!endsWithIgnoreCase(fileName, ".pdf")) {
				file = new File(fileName + ".pdf");
			}
		}
		return Optional.ofNullable(file);
	}
	
	public static Optional<File> promptForTXTExport(FileUtil fileUtil, Component parent) {
		List<FileChooserFilter> filter = Collections.singletonList(new FileChooserFilter("txt Files", "txt"));
		File file = fileUtil.getFile(parent, "Export Heatmap as TXT File", FileUtil.SAVE, filter);
		if(file != null) {
			String fileName = file.toString();
			if(!fileName.endsWith(".txt")) {
				fileName += ".txt";
				file = new File(fileName);
			}
		}
		return Optional.ofNullable(file);
	}
	

	private static boolean endsWithIgnoreCase(String str, String suffix) {
		int n = suffix.length();
		return str.regionMatches(true, str.length() - n, suffix, 0, n);
	}
	
	
	/**
	 * This is a copy of CyApplicationManager.getCurrentDirectory() from the cytoscape 3.5 API.
	 * If the API dependency of EnrichmentMap is increased to 3.5 then this code can be removed.
	 */
	public File getCurrentDirectory() {
		final Properties props = (Properties) serviceRegistrar
				.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		String lastDir = props.getProperty(FileUtil.LAST_DIRECTORY);
		File dir = (lastDir != null) ? new File(lastDir) : null;
		
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			dir = new File(System.getProperty("user.dir"));
			
			if (dir != null) // if path exists but is not valid, remove the property
				props.remove(FileUtil.LAST_DIRECTORY);
		}
		
		return dir;
	}
	
	
	/**
	 * This is a copy of CyApplicationManager.setCurrentDirectory() from the cytoscape 3.5 API.
	 * If the API dependency of EnrichmentMap is increased to 3.5 then this code can be removed.
	 */
	public boolean setCurrentDirectory(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return false;
		
		final Properties props = (Properties) serviceRegistrar
				.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		props.setProperty(FileUtil.LAST_DIRECTORY, dir.getAbsolutePath());
		
		return true;
	}
	
}
