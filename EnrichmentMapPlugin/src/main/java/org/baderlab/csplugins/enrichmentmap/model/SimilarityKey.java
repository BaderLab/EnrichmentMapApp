package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;

public class SimilarityKey {

	public final String geneSet1;
	public final String geneSet2;
	public final String interaction;
	public final String name;
	
	
	public SimilarityKey(String geneSet1, String geneSet2, String interaction, String name) {
		Objects.requireNonNull(geneSet1);
		Objects.requireNonNull(interaction);
		Objects.requireNonNull(geneSet2);
		this.geneSet1 = geneSet1;
		this.geneSet2 = geneSet2;
		this.interaction = interaction;
		this.name = name;
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
	
	public boolean isCompound() {
		return name == null;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		// add the hash codes from the genesets so that we get the same hash code regardless of the order
		return Objects.hash(geneSet1.hashCode() + geneSet2.hashCode(), interaction, name);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SimilarityKey))
			return false;
		SimilarityKey other = (SimilarityKey)o;
		if(name != null && other.name == null)
			return false;
		if(name == null && other.name != null)
			return false;
		if(name != null && other.name != null && !name.equals(other.name))
			return false;
		if(!interaction.equals(other.interaction))
			return false;
		
		return
				(geneSet1.equals(other.geneSet1) && geneSet2.equals(other.geneSet2))
			||	(geneSet1.equals(other.geneSet2) && geneSet2.equals(other.geneSet1));
	}
	
	public String getCompoundName() {
		return String.format("%s (%s) %s", geneSet1, interaction, geneSet2);
	}
	
	@Override
	public String toString() {
		return isCompound() 
			? getCompoundName()
			: String.format("%s (%s_%s) %s", geneSet1, interaction, name, geneSet2);
	}
	
}
