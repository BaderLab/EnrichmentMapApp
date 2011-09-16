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

package org.baderlab.csplugins.enrichmentmap;

import cytoscape.visual.*;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.mappings.*;
import cytoscape.CyNetwork;

import java.awt.*;

/**
 * Created by
 * User: risserlin
 * Date: Jan 26, 2009
 * Time: 3:23:52 PM
 * <p>
 * Class defining all the attributes of the Enrichment map Visual style
 */
public class EnrichmentMapVisualStyle {

    EnrichmentMapParameters params;

    public static final int maxNodeLabelLength = 15;

    //Attribute Names - prefix is appended to each one of these names in order to associated these
    //attributes to a particular enrichment map.  This allows for multiple enrichment maps in an
    //individual session.
    public static String NAME = "Name";
    public static String GS_DESCR = "GS_DESCR";
    public static String FORMATTED_NAME = "Formatted_name";
    public static String GS_SOURCE = "GS_Source";
    public static String GENES = "Genes";
    public static String ENR_GENES = "Enrichment_Genes";
    public static String GS_TYPE = "GS_Type";

    public static String ES_DATASET1 = "ES_dataset1";
    public static String NES_DATASET1 = "NES_dataset1";
    public static String GS_SIZE_DATASET1 = "gs_size_dataset1";
    public static String PVALUE_DATASET1 = "pvalue_dataset1";
    public static String FDR_QVALUE_DATASET1 = "fdr_qvalue_dataset1";
    public static String FWER_QVALUE_DATASET1 = "fwer_qvalue_dataset1";
    public static String COLOURING_DATASET1 = "Colouring_dataset1";

    public static String ES_DATASET2 = "ES_dataset2";
    public static String NES_DATASET2 = "NES_dataset2";
    public static String GS_SIZE_DATASET2 = "gs_size_dataset2";
    public static String PVALUE_DATASET2 = "pvalue_dataset2";
    public static String FDR_QVALUE_DATASET2 = "fdr_qvalue_dataset2";
    public static String FWER_QVALUE_DATASET2 = "fwer_qvalue_dataset2";
    public static String COLOURING_DATASET2 = "Colouring_dataset2";

    public static String GS_SIZE_SIGNATURE = "gs_size_signature";
    
    public static String GS_TYPE_ENRICHMENT = "ENR";
    public static String GS_TYPE_SIGNATURE = "SIG";
    
    public static String OVERLAP_SIZE = "Overlap_size";
    public static String SIMILARITY_COEFFECIENT = "similarity_coefficient";
    public static String OVERLAP_GENES = "Overlap_genes";
    public static String HYPERGEOM_PVALUE = "Overlap_Hypergeom_pVal";
    public static String ENRICHMENT_SET = "ENRICHMENT_SET";

    public static String NUMBER_OF_ENRICHMENT_GENES = "# of Enrichment Genes";
    
    public static String NETW_REPORT1_DIR = "GSEA_Report_Dataset1_folder";
    public static String NETW_REPORT2_DIR = "GSEA_Report_Dataset2_folder";
    
    //default colours
    public static Color max_phenotype1 = new Color(255,0,0);
    public static Color lighter_phenotype1 = new Color(255,102,102);
    public static Color lightest_phenotype1 = new Color(255,179,179);
    public static Color max_phenotype2 = new Color(0,100,255);
    public static Color lighter_phenotype2 = new Color(102,162,255);
    public static Color lightest_phenotype2 = new Color(179,208,255);
    public static Color overColor = Color.WHITE;

    private VisualStyle vs;

    /**
     * Constructor
     *
     * @param string - name of visual style
     * @param params - enrichment map parameters associated with this visual style.
     */
    public EnrichmentMapVisualStyle(String string, EnrichmentMapParameters params) {
        this.params = params;
        vs = new VisualStyle(string);
    }

    /**
     * Create visual style for this enrichment map
     *
     * @param network - network to apply this visual style
     * @param prefix - prefix to be appended to each of the attribute names
     * @return visual style
     */
    public VisualStyle createVisualStyle(CyNetwork network, String prefix){

        GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();
        globalAppCalc.setDefaultBackgroundColor(new Color(205,205,235));

        vs.setGlobalAppearanceCalculator(globalAppCalc);

        createEdgeAppearance(network, prefix);
        createNodeAppearance(network, prefix);

        return vs;
    }

    /**
     * Create edge appearances for this enrichment map, specify the edge thicknes mapped to the number of genes
     * in the overlap, and default colour
     *
     * @param network - network to apply this visual style
     * @param prefix - prefix to be appended to each of the attribute names
     */
    private void createEdgeAppearance(CyNetwork network, String prefix){
        EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();

        //set the default edge appearance
        EdgeAppearance edgeAppear = new EdgeAppearance();
        edgeAppear.set(VisualPropertyType.EDGE_COLOR, new Color(100,200,000) );
        edgeAppCalc.setDefaultAppearance(edgeAppear);


        //create a discrete mapper to map the colour of the edge based on the enrichment set
        DiscreteMapping disMapping = new DiscreteMapping(new Color(100,200,000) , ObjectMapping.EDGE_MAPPING);
        disMapping.setControllingAttributeName(prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET,network,false);
        disMapping.putMapValue(new Integer(0),new Color(100,200,000));
        disMapping.putMapValue(new Integer(1),new Color(100,200,000));
        disMapping.putMapValue(new Integer(2),new Color(100,149,237));

        Calculator colourCalculator = new BasicCalculator(prefix + "edgecolor", disMapping,VisualPropertyType.EDGE_COLOR);
        edgeAppCalc.setCalculator(colourCalculator);


        //Continous Mapping - set edge line thickness based on the number of genes in the overlap
        //ContinuousMapping continuousMapping_edgewidth = new ContinuousMapping((new Integer(1)).getClass(),prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT);
        ContinuousMapping continuousMapping_edgewidth = new ContinuousMapping(1, ObjectMapping.EDGE_MAPPING);
        continuousMapping_edgewidth.setControllingAttributeName(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, network, false);
        Interpolator numTonum2 = new LinearNumberToNumberInterpolator();
        continuousMapping_edgewidth.setInterpolator(numTonum2);

        Double under_width = 0.5;
        Double min_width = 1.0;
        Double max_width = 5.0;
        Double over_width = 6.0;

        // Create boundary conditions                  less than,   equals,  greater than
        BoundaryRangeValues bv4 = new BoundaryRangeValues(under_width, min_width, min_width);
        BoundaryRangeValues bv5 = new BoundaryRangeValues(max_width, max_width, over_width);
        continuousMapping_edgewidth.addPoint(params.getSimilarityCutOff(), bv4);
        continuousMapping_edgewidth.addPoint(1.0, bv5);
        Calculator edgeWidthCalculator = new BasicCalculator(prefix + "edgesize", continuousMapping_edgewidth, VisualPropertyType.EDGE_LINE_WIDTH);
        edgeAppCalc.setCalculator(edgeWidthCalculator);

        vs.setEdgeAppearanceCalculator(edgeAppCalc);


    }

    /**
     * Create node appearances for this enrichment map, set node label based of gene set name,
     * set node size based on number of genes in gene set, set node colour based on enrichment of gene set
     * in dataset 1, set node border colour based on enrichment of gene set in dataset 2
     *
     * @param network - network to apply this visual style
     * @param prefix - prefix to be appended to each of the attribute names
     */
    private void createNodeAppearance(CyNetwork network,String prefix){

        // Create boundary conditions                  less than,   equals,  greater than
        BoundaryRangeValues bv3a = new BoundaryRangeValues(max_phenotype2,max_phenotype2,max_phenotype2);
        BoundaryRangeValues bv3b = new BoundaryRangeValues(lighter_phenotype2, lighter_phenotype2, max_phenotype2);
        BoundaryRangeValues bv3c = new BoundaryRangeValues(lightest_phenotype2, lightest_phenotype2,lighter_phenotype2);
        BoundaryRangeValues bv3d = new BoundaryRangeValues(lightest_phenotype2, overColor, overColor);
        BoundaryRangeValues bv3e = new BoundaryRangeValues(overColor, overColor,overColor);
        BoundaryRangeValues bv3f = new BoundaryRangeValues(overColor, overColor, lightest_phenotype1);
        BoundaryRangeValues bv3g = new BoundaryRangeValues(lightest_phenotype1, lightest_phenotype1, lighter_phenotype1);
        BoundaryRangeValues bv3h = new BoundaryRangeValues(lighter_phenotype1, lighter_phenotype1, max_phenotype1);
        BoundaryRangeValues bv3i = new BoundaryRangeValues(max_phenotype1, max_phenotype1, max_phenotype1);


        NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();

        //set the default node appearance
        NodeAppearance nodeAppear = new NodeAppearance();
        nodeAppear.set(VisualPropertyType.NODE_FILL_COLOR, new Color(190,190,190) /* a lighter grey*/);
        nodeAppear.set(VisualPropertyType.NODE_BORDER_COLOR,new Color(190,190,190) /* a lighter grey*/);
        nodeAppear.set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);

        //change the default node and border size only when using two distinct dataset to be more equal.
        if(params.isTwoDistinctExpressionSets()){
            nodeAppear.set(VisualPropertyType.NODE_SIZE, new Double(15.0));
            nodeAppear.set(VisualPropertyType.NODE_LINE_WIDTH, new Double(15.0));
        }
        else{
            nodeAppear.set(VisualPropertyType.NODE_SIZE, new Double(20.0));
            nodeAppear.set(VisualPropertyType.NODE_LINE_WIDTH, new Double(4.0));
        }
        nodeAppCalc.setDefaultAppearance(nodeAppear);
        
        // Passthrough Mapping - set node label
        PassThroughMapping pm = new PassThroughMapping(new String(), prefix + EnrichmentMapVisualStyle.FORMATTED_NAME);

        //if it is an EMgmt then we want the node label to be the description.
        if(params.isEMgmt()){
            pm = new PassThroughMapping(new String(), prefix + EnrichmentMapVisualStyle.GS_DESCR);
        }

        Calculator nlc = new BasicCalculator(prefix +"nodeLabel",
                                                          pm, VisualPropertyType.NODE_LABEL);
               nodeAppCalc.setCalculator(nlc);


        //Continuous Mapping - set node size based on the size of the geneset
        ContinuousMapping continuousMapping_size = new ContinuousMapping(35, ObjectMapping.NODE_MAPPING);
        continuousMapping_size.setControllingAttributeName(prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, network, false);
        Interpolator numTonum = new LinearNumberToNumberInterpolator();
        continuousMapping_size.setInterpolator(numTonum);

        Integer min = 20;
        Integer max = 65;

           // Create boundary conditions                  less than,   equals,  greater than
         BoundaryRangeValues bv0 = new BoundaryRangeValues(min, min, min);
         BoundaryRangeValues bv1 = new BoundaryRangeValues(max, max, max);
        continuousMapping_size.addPoint(10.0, bv0);
        continuousMapping_size.addPoint(474.0, bv1);
        Calculator nodeSizeCalculator = new BasicCalculator(prefix+"size2size", continuousMapping_size, VisualPropertyType.NODE_SIZE);
         nodeAppCalc.setCalculator(nodeSizeCalculator);

        if(params.isTwoDatasets()){


            //Continuous Mapping - set node size based on the size of the geneset
            ContinuousMapping continuousMapping_size_dataset2 = new ContinuousMapping(35, ObjectMapping.NODE_MAPPING);
		continuousMapping_size_dataset2.setControllingAttributeName(prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, network, false);
            Interpolator numTonum3 = new LinearNumberToNumberInterpolator();
            continuousMapping_size_dataset2.setInterpolator(numTonum3);

            Integer min_line = 4;
            Integer max_line = 15;
               // Create boundary conditions                  less than,   equals,  greater than
             BoundaryRangeValues bv0a = new BoundaryRangeValues(min_line, min_line, min_line);
             BoundaryRangeValues bv1a = new BoundaryRangeValues(max_line, max_line, max_line);
            continuousMapping_size_dataset2.addPoint(10.0, bv0a);
            continuousMapping_size_dataset2.addPoint(474.0, bv1a);
            Calculator nodelineSizeCalculator = new BasicCalculator(prefix + "size2size", continuousMapping_size_dataset2, VisualPropertyType.NODE_LINE_WIDTH);
             nodeAppCalc.setCalculator(nodelineSizeCalculator);



            //Continuous Mapping - set node line colour based on the sign of the ES score of second dataset
            //Color scale and mapper used by node colour and node line colour
            ContinuousMapping continuousMapping_width_col = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
            continuousMapping_width_col.setControllingAttributeName(prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2, network, false);
            Interpolator numToColor2 = new LinearNumberToColorInterpolator();
            continuousMapping_width_col.setInterpolator(numToColor2);

            // Set the attribute point values associated with the boundary values
            continuousMapping_width_col.addPoint(-1.0, bv3a);
            continuousMapping_width_col.addPoint(-0.995, bv3b);
            continuousMapping_width_col.addPoint(-0.95, bv3c);
            continuousMapping_width_col.addPoint(-0.9, bv3d);
            continuousMapping_width_col.addPoint(0.0, bv3e);
            continuousMapping_width_col.addPoint(0.9, bv3f);
            continuousMapping_width_col.addPoint(0.95, bv3g);
            continuousMapping_width_col.addPoint(0.995, bv3h);
            continuousMapping_width_col.addPoint(1.0, bv3i);

            Calculator nodeColorCalculator_width_col = new BasicCalculator(prefix + "ES2Colour", continuousMapping_width_col, VisualPropertyType.NODE_BORDER_COLOR);
             nodeAppCalc.setCalculator(nodeColorCalculator_width_col);


        }

        //Continuous Mapping - set node colour based on the sign of the ES score of first dataset

        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);  
	   continuousMapping.setControllingAttributeName(prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1, network, false);
        
        Interpolator numToColor = new LinearNumberToColorInterpolator();

        continuousMapping.setInterpolator(numToColor);

         // Set the attribute point values associated with the boundary values
         // Set the attribute point values associated with the boundary values
        continuousMapping.addPoint(-1.0, bv3a);
        continuousMapping.addPoint(-0.995, bv3b);
        continuousMapping.addPoint(-0.95, bv3c);
        continuousMapping.addPoint(-0.9, bv3d);
        continuousMapping.addPoint(0.0, bv3e);
        continuousMapping.addPoint(0.9, bv3f);
        continuousMapping.addPoint(0.95, bv3g);
        continuousMapping.addPoint(0.995, bv3h);
        continuousMapping.addPoint(1.0, bv3i);

        Calculator nodeColorCalculator = new BasicCalculator(prefix + "ES2Colour", continuousMapping, VisualPropertyType.NODE_FILL_COLOR);
         nodeAppCalc.setCalculator(nodeColorCalculator);


        //if it is an EM geneset file and there are more than one Geneset type, map the types to shapes.
        if(params.isEMgmt() && params.getGenesetTypes().size() > 1){
            DiscreteMapping disMapping = new DiscreteMapping( new NodeShape, ObjectMapping.NODE_MAPPING);
            disMapping.setControllingAttributeName(prefix + EnrichmentMapVisualStyle.);
        }


       vs.setNodeAppearanceCalculator(nodeAppCalc);


    }
}