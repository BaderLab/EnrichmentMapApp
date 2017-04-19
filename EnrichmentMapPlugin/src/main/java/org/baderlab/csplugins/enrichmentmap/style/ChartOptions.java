package org.baderlab.csplugins.enrichmentmap.style;

public class ChartOptions {
	
	private ChartData data;
	private ChartType type;
	private ColorScheme colorScheme;
	private boolean showLabels;
	
	public ChartOptions(ChartData data, ChartType type, ColorScheme colorScheme, boolean showLabels) {
		this.data = data;
		this.type = type;
		this.colorScheme = colorScheme;
		this.showLabels = showLabels;
	}

	public ChartData getData() {
		return data;
	}
	
	public void setData(ChartData data) {
		this.data = data;
	}

	public ChartType getType() {
		return type;
	}

	public void setType(ChartType type) {
		this.type = type;
	}
	
	public ColorScheme getColorScheme() {
		return colorScheme;
	}

	public void setColorScheme(ColorScheme colorScheme) {
		this.colorScheme = colorScheme;
	}
	
	public boolean isShowLabels() {
		return showLabels;
	}
	
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}
}
