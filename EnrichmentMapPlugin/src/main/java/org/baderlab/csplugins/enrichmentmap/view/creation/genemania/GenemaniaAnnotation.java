package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

public class GenemaniaAnnotation {
	
	private String name;
	private String description;
	private double qValue;
	private int sample;
	private int total;
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public double getqValue() {
		return qValue;
	}
	
	public int getSample() {
		return sample;
	}
	
	public int getTotal() {
		return total;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GenemaniaAnnotation [name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", qValue=");
		builder.append(qValue);
		builder.append(", sample=");
		builder.append(sample);
		builder.append(", total=");
		builder.append(total);
		builder.append("]");
		return builder.toString();
	}
	
}
