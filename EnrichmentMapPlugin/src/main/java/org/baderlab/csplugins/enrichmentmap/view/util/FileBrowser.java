package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.JFileChooser;

import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

/**
 * Because the Cytoscape FileUtil service doesn't let you browse for directories :(
 */
public class FileBrowser {

	public static enum Filter {
		GMT("GMT Files") {
			public List<FileChooserFilter> getFilters() {
				return Arrays.asList(
					new FileChooserFilter("gmt Files", "gmt")
		        );
			}
		},
		ENRICHMENT("Enrichment Files") {
			public List<FileChooserFilter> getFilters() {
				return Arrays.asList(
					new FileChooserFilter("gct Files", "xls"),  
		        	new FileChooserFilter("rnk Files", "bgo"),
		        	new FileChooserFilter("txt Files", "txt"),
		        	new FileChooserFilter("tsv Files", "tsv")
		        );
			}
		}, 
		EXPRESSION("Expression Files") {
			public List<FileChooserFilter> getFilters() {
				return Arrays.asList(
					new FileChooserFilter("gct Files", "gct"),          
				    new FileChooserFilter("txt Files", "txt")
		        );
			}
		}, 
		RANK("Rank Files") {
			public List<FileChooserFilter> getFilters() {
				return Arrays.asList(
					new FileChooserFilter("rnk Files", "rnk"),          
				    new FileChooserFilter("txt Files", "txt")
		        );
			}
		}, 
		CLASS("Class (Phenotype) Files") {
			public List<FileChooserFilter> getFilters() {
				return Arrays.asList(
					new FileChooserFilter("cls Files", "cls"),          
				    new FileChooserFilter("txt Files", "txt")
		        );
			}
		};
		
		public final String title;
		
		public abstract List<FileChooserFilter> getFilters();
		
		private Filter(String title) {
			this.title = title;
		}
	}
	
	
	
	public static Optional<Path> browse(FileUtil fileUtil, Component parent, Filter filter) {
		File file = fileUtil.getFile(parent, filter.title, FileUtil.LOAD, filter.getFilters());
		return Optional.ofNullable(file).map(File::toPath);
	}
	
	
	public static Optional<File> browseForRootFolder(Dialog parent) {
		final String osName = System.getProperty("os.name");
		if(osName.startsWith("Mac"))
			return browseForRootFolderMac(parent);
		else
			return browseForRootFolderSwing(parent);
	}
	
	
	private static Optional<File> browseForRootFolderMac(Dialog parent) {
		final String property = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		try {
			FileDialog chooser = new FileDialog(parent, "Choose Root Folder", FileDialog.LOAD);
			chooser.setModal(true);
			chooser.setLocationRelativeTo(parent);
			chooser.setVisible(true);
			
			String file = chooser.getFile();
			String dir = chooser.getDirectory();
			
			if(file == null || dir == null) {
				return Optional.empty();
			}
			return Optional.of(new File(dir + File.separator + file));
		} finally {
			if(property != null) {
				System.setProperty("apple.awt.fileDialogForDirectories", property);
			}
		}
	}
	
	private static Optional<File> browseForRootFolderSwing(Dialog parent) {
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setDialogTitle("Select Root Folder");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) { 
	    	return Optional.of(chooser.getSelectedFile());
	    }
	    return Optional.empty();
	}
	
}
