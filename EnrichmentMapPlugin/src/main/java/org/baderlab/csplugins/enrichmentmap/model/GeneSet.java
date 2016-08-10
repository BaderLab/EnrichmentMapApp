/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.inferred.freebuilder.FreeBuilder;

/**
 * This is an abstract class only so we can override toString(), otherwise it would be an interface
 * @author mkucera
 *
 */
@FreeBuilder
public abstract class GeneSet {

	public abstract String getName();

	public abstract String getDescription();

	public abstract Set<Integer> getGenes();

	public abstract Optional<String> getSource();
	
	
	public static class Builder extends GeneSet_Builder {
		
		public static Builder from(GeneSet gs) {
			return new Builder().mergeFrom(gs);
		}
		
		private Builder() {
		}
		
		public Builder(String name, String descrip) {
			setName(name);
			setDescription(descrip);
			
			//if you can split the name using '|', take the second token to be the gene set type
			String[] name_tokens = name.split("%");
			if(name_tokens.length > 1)
				setSource(name_tokens[1]);
		}
		
		public Builder(String[] tokens) {
			this(tokens[1], tokens[2]);
			if(tokens.length < 3)
				return;
			for(int i = 3; i < tokens.length; i++)
				addGenes(Integer.parseInt(tokens[i]));
		}
	}
	

	@Override
	public  String toString() {
		StringBuffer geneset = new StringBuffer();

		geneset.append(getName() + "\t" + getDescription() + "\t");

		for(Iterator<Integer> i = getGenes().iterator(); i.hasNext();)
			geneset.append(i.next().toString() + "\t");

		return geneset.toString();
	}


}
