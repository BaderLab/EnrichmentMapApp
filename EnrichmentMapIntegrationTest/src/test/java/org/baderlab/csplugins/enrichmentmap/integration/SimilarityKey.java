package org.baderlab.csplugins.enrichmentmap.integration;

import java.util.Objects;

public class SimilarityKey {

	private final String geneSet1;
	private final String interaction;
	private final String geneSet2;
	
	public SimilarityKey(String geneSet1, String interaction, String geneSet2) {
		Objects.requireNonNull(geneSet1);
		Objects.requireNonNull(interaction);
		Objects.requireNonNull(geneSet2);
		this.geneSet1 = geneSet1;
		this.interaction = interaction;
		this.geneSet2 = geneSet2;
	}
	
	public static SimilarityKey parse(String name) {
		int open  = name.indexOf("(");
		int close = name.indexOf(")", open);
		String name1 = name.substring(0, open).trim();
		String name2 = name.substring(close+1, name.length()).trim();
		String interaction = name.substring(open+1, close);
		return new SimilarityKey(name1, interaction, name2);
	}
	
	public String getGeneSet1() {
		return geneSet1;
	}
	
	public String getGeneSet2() {
		return geneSet2;
	}
	
	public String getInteraction() {
		return interaction;
	}
	
	public SimilarityKey swap() {
		return new SimilarityKey(geneSet2, interaction, geneSet1);
	}
	
	@Override
	public int hashCode() {
		return geneSet1.hashCode() + interaction.hashCode() + geneSet2.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SimilarityKey))
			return false;
		SimilarityKey other = (SimilarityKey)o;
		
		if(!interaction.equals(other.interaction))
			return false;
		
		return
				(geneSet1.equals(other.geneSet1) && geneSet2.equals(other.geneSet2))
			||	(geneSet1.equals(other.geneSet2) && geneSet2.equals(other.geneSet1));
	}
	
	@Override
	public String toString() {
		return geneSet1 + " (" + interaction + ") " + geneSet2;
	}
	
}
