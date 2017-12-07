package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class FileSizeCellRenderer extends DefaultTableCellRenderer {

	@Override
	protected void setValue(Object value) {
		if(value instanceof Number) {
			int bytes = ((Number) value).intValue();
			String text = humanReadableByteCount(bytes, true);
			super.setValue(text);
		} else {
			super.setValue(value);
		}
	}
	
	private static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
