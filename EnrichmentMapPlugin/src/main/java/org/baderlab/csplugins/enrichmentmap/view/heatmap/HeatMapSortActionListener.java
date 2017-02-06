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

// $Id: HeatMapActionListener.java 368 2009-09-25 14:49:59Z risserlin $
// $LastChangedDate: 2009-09-25 10:49:59 -0400 (Fri, 25 Sep 2009) $
// $LastChangedRevision: 368 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/HeatMapActionListener.java $

package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Edges;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Nodes;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParameters.Sort;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * Heat map action listener
 */
@Deprecated
public class HeatMapSortActionListener implements ActionListener {

	@Inject private FileUtil fileUtil;
	@Inject private CySwingApplication application;

	@Inject private @Nodes Provider<HeatMapPanel> nodesPanelProvider;
	@Inject private @Edges Provider<HeatMapPanel> edgesPanelProvider;
	
	private HeatMapParameters hmParams;
	private EnrichmentMap map;

	public interface Factory {
		HeatMapSortActionListener create(HeatMapParameters hmParams, EnrichmentMap map);
	}
	
	@Inject
	public HeatMapSortActionListener(@Assisted HeatMapParameters hmParams, @Assisted EnrichmentMap map) {
		this.hmParams = hmParams;
		this.map = map;
	}

	/**
	 * Update heat map according to action selection
	 */
	public void actionPerformed(ActionEvent evt) {
		HeatMapPanel edgeOverlapPanel = edgesPanelProvider.get();
		HeatMapPanel nodeOverlapPanel = nodesPanelProvider.get();
		
		boolean updateAscendingButton = false;

		edgeOverlapPanel.clearPanel();
		nodeOverlapPanel.clearPanel();
		
		JComboBox box = (JComboBox) evt.getSource();
		String select = (String) box.getSelectedItem();

		Sort sort = Sort.fromDisplay(select);
		
		if(sort == Sort.CLUSTER) {
			hmParams.setSort(HeatMapParameters.Sort.CLUSTER);
			hmParams.setSortIndex(-1);
		}
		//Add a ranking file
		else if(select.equalsIgnoreCase("Add Rankings ... ")) {
			Map<String, Ranking> all_ranks = map.getAllRanks();
			FileChooserFilter filter_rnk = new FileChooserFilter("rnk Files", "rnk");
			FileChooserFilter filter_txt = new FileChooserFilter("txt Files", "txt");
			FileChooserFilter filter_xls = new FileChooserFilter("xls Files", "xls");

			//the set of filter (required by the file util method
			List<FileChooserFilter> all_filters = Arrays.asList(filter_rnk, filter_txt, filter_xls);

			// Get the file name
			File file = fileUtil.getFile(application.getJFrame(), "Import rank File", FileUtil.LOAD, all_filters);

			if(file != null) {
				//find out from the user what they want to name these ranking

				String ranks_name = JOptionPane.showInputDialog(nodeOverlapPanel, "What would you like to name these Rankings?", "My Rankings");
				boolean noname = true;
				while(noname) {
					noname = false;
					//make sure the name is not already in the rankings
					for(String current_name : all_ranks.keySet()) {
						if(current_name.equalsIgnoreCase(ranks_name)) {
							noname = true;
							ranks_name = JOptionPane.showInputDialog(nodeOverlapPanel, "Sorry that name already exists.Please choose another name.");
						}
					}
				}

				//load the new ranks file
				//the new rank file is not associated with a dataset.
				//simply add it to Dataset 1
				RanksFileReaderTask ranking1 = new RanksFileReaderTask(file.getAbsolutePath(), map.getDataset(LegacySupport.DATASET1), ranks_name, true);
				try {
					ranking1.parse(null);
				} catch(IOException e) {
					e.printStackTrace();
				}

				//add an index to the ascending array for the new rank file.
				boolean[] ascending = hmParams.getAscending();
				boolean[] new_ascending = new boolean[ascending.length + 1];

				//copy old ascending in to the new ascending array
				System.arraycopy(ascending, 0, new_ascending, 0, ascending.length);

				//set ordering for rank fall to ascending
				new_ascending[new_ascending.length - 1] = true;

				hmParams.setAscending(new_ascending);
			}
		} else if(sort == Sort.NONE) {
			hmParams.setSort(HeatMapParameters.Sort.NONE);
			hmParams.setSortIndex(-1);
			hmParams.setRankFileIndex("");
		} else if(sort == Sort.COLUMN) {
			hmParams.setRankFileIndex("");
			hmParams.setSort(HeatMapParameters.Sort.COLUMN);
			if(hmParams.isSortbycolumn_event_triggered()) {
				//reset sort column trigger
				hmParams.setSortbycolumn_event_triggered(false);

				//change the ascending boolean flag for the column we are about to sort by
				hmParams.flipAscending(hmParams.getSortIndex());
			}
		} else {
			Set<String> ranks = map.getAllRankNames();

			//iterate through all the rank files.
			//the order should always be the same get a counter to find which index to
			//associate this rank file for the ascending and descending order
			int i = 0;
			int columns = 0;
			//calculate the number of indexes used for the column names
			if(map.getDataset(LegacySupport.DATASET2) != null && map.getDataset(LegacySupport.DATASET2).getExpressionSets() != null
					&& !map.getDataset(LegacySupport.DATASET1).getExpressionSets().getFilename()
							.equalsIgnoreCase(map.getDataset(LegacySupport.DATASET2).getExpressionSets().getFilename()))
				columns = map.getDataset(LegacySupport.DATASET1).getExpressionSets().getColumnNames().length
						+ map.getDataset(LegacySupport.DATASET2).getExpressionSets().getColumnNames().length - 2;
			else
				columns = map.getDataset(LegacySupport.DATASET1).getExpressionSets().getColumnNames().length;

			for(String ranks_name : ranks) {
				if(ranks_name.equalsIgnoreCase(select)) {
					hmParams.setSort(HeatMapParameters.Sort.RANK);
					hmParams.setRankFileIndex(ranks_name);
					hmParams.setSortIndex(columns + i);

					updateAscendingButton = true;
				}
				i++;
			}
		}

		//if the object selected is associated with a sorting order update the sort direction button
		if(updateAscendingButton) {
			//the two types that can be associated with a direction as columns and ranks
			/*
			 * if(hmParams.getSort() == HeatMapParameters.Sort.COLUMN)
			 * hmParams.setCurrent_ascending(hmParams.isAscending(hmParams.
			 * getSortIndex())); else if(hmParams.getSort() ==
			 * HeatMapParameters.Sort.RANK)
			 * hmParams.setCurrent_ascending(hmParams.isAscending(1)); //TODO:
			 * need to specify column for rank files.
			 */
			//only swap the direction of the sort if a column sort action was triggered
			if(hmParams.isSortbycolumn_event_triggered()) {
				//reset sort column trigger
				hmParams.setSortbycolumn_event_triggered(false);

				//change the ascending boolean flag for the column we are about to sort by
				hmParams.flipAscending(hmParams.getSortIndex());
			}
		}

		hmParams.ResetColorGradient_ds1();

		if(map.getDataset(LegacySupport.DATASET2) != null && map.getDataset(LegacySupport.DATASET2).getExpressionSets() != null
				&& !map.getDataset(LegacySupport.DATASET1).getExpressionSets().getFilename()
						.equalsIgnoreCase(map.getDataset(LegacySupport.DATASET2).getExpressionSets().getFilename()))
			hmParams.ResetColorGradient_ds2();

		edgeOverlapPanel.updatePanel();
		nodeOverlapPanel.updatePanel();

	}
}
