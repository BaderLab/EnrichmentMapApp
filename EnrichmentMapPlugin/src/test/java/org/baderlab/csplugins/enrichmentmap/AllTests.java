/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates, 
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$/**
 
package org.baderlab.csplugins.enrichmentmap;


import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultTest;
import org.baderlab.csplugins.enrichmentmap.model.GeneSetSimilarityTest;
import org.baderlab.csplugins.enrichmentmap.model.GeneSetTest;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGenesetsTest;
import org.baderlab.csplugins.enrichmentmap.parsers.FileReaderTest;
import org.baderlab.csplugins.enrichmentmap.task.EnrichmentMapBuildMapTaskTest;
import org.baderlab.csplugins.enrichmentmap.task.LoadBingoResultsTest;
import org.baderlab.csplugins.enrichmentmap.task.LoadDatasetTaskTest;
import org.baderlab.csplugins.enrichmentmap.task.LoadDavidResultTest;
import org.baderlab.csplugins.enrichmentmap.task.LoadEdbDatasetTest;
import org.baderlab.csplugins.enrichmentmap.task.LoadGMTFileOnlyTest;
import org.baderlab.csplugins.enrichmentmap.util.LinearNumberInterpolatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	
	HypergeometricTest.class,
	MannWhitneyRankSumTest.class,
	
	EnrichmentResultTest.class,
	GeneSetSimilarityTest.class,
	GeneSetTest.class,
	SetOfGenesetsTest.class,
	
	FileReaderTest.class,
	
	LoadBingoResultsTest.class,
	LoadDatasetTaskTest.class,
	LoadDavidResultTest.class,
	LoadEdbDatasetTest.class,
	LoadGMTFileOnlyTest.class,
	EnrichmentMapBuildMapTaskTest.class,
	
	LinearNumberInterpolatorTest.class,
	
})
public class AllTests {
}
