package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;

public class SimilarityKey {

	private final String geneSet1;
	private final String geneSet2;
	private final String interaction;
	// a set of zero indicates a compound edge
	private final int set;
	
	
	public SimilarityKey(String geneSet1, String geneSet2, String interaction, int set) {
		Objects.requireNonNull(geneSet1);
		Objects.requireNonNull(interaction);
		Objects.requireNonNull(geneSet2);
		this.geneSet1 = geneSet1;
		this.geneSet2 = geneSet2;
		this.interaction = interaction;
		this.set = set;
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
	
	public int getSet() {
		return set;
	}
	
	public boolean isCompound() {
		return set == 0;
	}
	
	@Override
	public int hashCode() {
		// add the hash codes from the genesets so that we get the same hash code regardless of the order
		return Objects.hash(geneSet1.hashCode() + geneSet2.hashCode(), interaction, set);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SimilarityKey))
			return false;
		SimilarityKey other = (SimilarityKey)o;
		
		if(set != other.set)
			return false;
		if(!interaction.equals(other.interaction))
			return false;
		
		return
				(geneSet1.equals(other.geneSet1) && geneSet2.equals(other.geneSet2))
			||	(geneSet1.equals(other.geneSet2) && geneSet2.equals(other.geneSet1));
	}
	
	@Override
	public String toString() {
		if(set == 0)
			return String.format("%s (%s) %s", geneSet1, interaction, geneSet2);
		else
			return String.format("%s (%s_set%d) %s", geneSet1, interaction, set, geneSet2);
	}
	
}
