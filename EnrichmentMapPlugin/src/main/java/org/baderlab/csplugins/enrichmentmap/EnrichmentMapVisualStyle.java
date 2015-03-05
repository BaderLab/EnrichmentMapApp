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

import java.awt.*;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 * Created by
 * User: risserlin
 * Date: Jan 26, 2009
 * Time: 3:23:52 PM
 * <p>
 * Class defining all the attributes of the Enrichment map Visual style
 */
public class EnrichmentMapVisualStyle {

	private EnrichmentMapParameters params;
	
	//services required for setting up visualStyle
	//we will need all three mappers
    private VisualMappingFunctionFactory vmfFactoryContinuous;
    private VisualMappingFunctionFactory vmfFactoryDiscrete;
    private VisualMappingFunctionFactory vmfFactoryPassthrough;
       
	
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
    public static String SIMILARITY_COEFFICIENT = "similarity_coefficient";
    public static String OVERLAP_GENES = "Overlap_genes";
    public static String HYPERGEOM_PVALUE = "Overlap_Hypergeom_pVal";
    public static String MANN_WHIT_PVALUE = "Overlap_Mann_Whit_pVal";
    public static String ENRICHMENT_SET = "ENRICHMENT_SET";
    
    // Related to Hypergeometric Test
    public static String HYPERGEOM_N = "HyperGeom_N_Universe";
    public static String HYPERGEOM_n = "HyperGeom_n_Sig_Universe";
    public static String HYPERGEOM_k = "k_Intersection";
    public static String HYPERGEOM_m = "m_Enr_Genes";
    
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

    /**
     * Constructor
     *
     * @param string - name of visual style
     * @param params - enrichment map parameters associated with this visual style.
     */
    public EnrichmentMapVisualStyle(EnrichmentMapParameters params,VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete,
    	     VisualMappingFunctionFactory vmfFactoryPassthrough) {
        this.params = params;
        
        this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;   
    }

    /**
     * Create visual style for this enrichment map
     *
     * @param network - network to apply this visual style
     * @param prefix - prefix to be appended to each of the attribute names
     * @return visual style
     */
    public VisualStyle createVisualStyle(VisualStyle vs, String prefix){
    	
    	//set default background colour
    	vs.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, new Color(205,205,235));    	        

        vs = createEdgeAppearance(vs, prefix);
        vs = createNodeAppearance(vs, prefix);

        return vs;
    }

    /**
     * Create edge appearances for this enrichment map, specify the edge thicknes mapped to the number of genes
     * in the overlap, and default colour
     *
     * @param network - network to apply this visual style
     * @param prefix - prefix to be appended to each of the attribute names
     */
    private VisualStyle createEdgeAppearance(VisualStyle vs, String prefix){
                      
        //add the discrete mapper for edge colour:        
        //can't just update edge_paint -- need to do the same for all the type of edge paints
        DiscreteMapping<Integer,Paint> disMapping_edge2 = (DiscreteMapping<Integer,Paint>)this.vmfFactoryDiscrete.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET, Integer.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
        disMapping_edge2.putMapValue(new Integer(0),new Color(100,200,000));
        disMapping_edge2.putMapValue(new Integer(1),new Color(100,200,000));
        disMapping_edge2.putMapValue(new Integer(2),new Color(100,149,237));
        vs.addVisualMappingFunction(disMapping_edge2);
        
        DiscreteMapping<Integer,Paint> disMapping_edge4 = (DiscreteMapping<Integer,Paint>)this.vmfFactoryDiscrete.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET, Integer.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        disMapping_edge4.putMapValue(new Integer(0),new Color(100,200,000));
        disMapping_edge4.putMapValue(new Integer(1),new Color(100,200,000));
        disMapping_edge4.putMapValue(new Integer(2),new Color(100,149,237));
        vs.addVisualMappingFunction(disMapping_edge4);


        //Continous Mapping - set edge line thickness based on the number of genes in the overlap
        ContinuousMapping<Double,Double> conmapping_edgewidth = (ContinuousMapping<Double,Double>) this.vmfFactoryContinuous.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT, Double.class, BasicVisualLexicon.EDGE_WIDTH);
                
        Double under_width = 0.5;
        Double min_width = 1.0;
        Double max_width = 5.0;
        Double over_width = 6.0;

        // Create boundary conditions                  less than,   equals,  greater than
        BoundaryRangeValues<Double> bv4 = new BoundaryRangeValues<Double>(under_width, min_width, min_width);
        BoundaryRangeValues<Double> bv5 = new BoundaryRangeValues<Double>(max_width, max_width, over_width);
        conmapping_edgewidth.addPoint(params.getSimilarityCutOff(), bv4);
        conmapping_edgewidth.addPoint(1.0, bv5);
        
        vs.addVisualMappingFunction(conmapping_edgewidth);
        
        return vs;

    }

    /**
     * Create node appearances for this enrichment map, set node label based of gene set name,
     * set node size based on number of genes in gene set, set node colour based on enrichment of gene set
     * in dataset 1, set node border colour based on enrichment of gene set in dataset 2
     *
     * @param network - network to apply this visual style
     * @param prefix - prefix to be appended to each of the attribute names
     */
    private VisualStyle createNodeAppearance(VisualStyle vs,String prefix){

        // Create boundary conditions                  less than,   equals,  greater than
        BoundaryRangeValues<Paint> bv3a = new BoundaryRangeValues<Paint>(max_phenotype2,max_phenotype2,max_phenotype2);
        BoundaryRangeValues<Paint> bv3b = new BoundaryRangeValues<Paint>(lighter_phenotype2, lighter_phenotype2, max_phenotype2);
        BoundaryRangeValues<Paint> bv3c = new BoundaryRangeValues<Paint>(lightest_phenotype2, lightest_phenotype2,lighter_phenotype2);
        BoundaryRangeValues<Paint> bv3d = new BoundaryRangeValues<Paint>(lightest_phenotype2, overColor, overColor);
        BoundaryRangeValues<Paint> bv3e = new BoundaryRangeValues<Paint>(overColor, overColor,overColor);
        BoundaryRangeValues<Paint> bv3f = new BoundaryRangeValues<Paint>(overColor, overColor, lightest_phenotype1);
        BoundaryRangeValues<Paint> bv3g = new BoundaryRangeValues<Paint>(lightest_phenotype1, lightest_phenotype1, lighter_phenotype1);
        BoundaryRangeValues<Paint> bv3h = new BoundaryRangeValues<Paint>(lighter_phenotype1, lighter_phenotype1, max_phenotype1);
        BoundaryRangeValues<Paint> bv3i = new BoundaryRangeValues<Paint>(max_phenotype1, max_phenotype1, max_phenotype1);


        //set the default node appearance
        vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(190,190,190) /* a lighter grey*/);
        vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, new Color(190,190,190) /* a lighter grey*/);
        vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE,NodeShapeVisualProperty.ELLIPSE );

        //change the default node and border size only when using two distinct dataset to be more equal.
        if(params.isTwoDistinctExpressionSets()){
        	vs.setDefaultValue(BasicVisualLexicon.NODE_SIZE, new Double(15.0));
        	vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, new Double(15.0));
        }
        else{
        	vs.setDefaultValue(BasicVisualLexicon.NODE_SIZE, new Double(20.0));
        	vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, new Double(4.0));
        }        
        
        // Passthrough Mapping - set node label
        PassthroughMapping<String,String> pm = (PassthroughMapping<String,String>)this.vmfFactoryPassthrough.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.FORMATTED_NAME,String.class,BasicVisualLexicon.NODE_LABEL);

        //if it is an EMgmt then we want the node label to be the description.
        if(params.isEMgmt()){
        	pm = (PassthroughMapping<String,String>)this.vmfFactoryPassthrough.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.GS_DESCR,String.class,BasicVisualLexicon.NODE_LABEL);            
        }

        vs.addVisualMappingFunction(pm);


        //Continuous Mapping - set node size based on the size of the geneset
        ContinuousMapping<Integer,Double> continuousMapping_size = (ContinuousMapping<Integer,Double>)this.vmfFactoryContinuous.createVisualMappingFunction(prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, Integer.class, BasicVisualLexicon.NODE_SIZE);
        
        Double min = 20.0;
        Double max = 65.0;

           // Create boundary conditions                  less than,   equals,  greater than
         BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(min, min, min);
         BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(max, max, max);
        continuousMapping_size.addPoint(10, bv0);
        continuousMapping_size.addPoint(474, bv1);
        
        vs.addVisualMappingFunction(continuousMapping_size);

        if(params.isTwoDatasets()){

            //Continuous Mapping - set node size based on the size of the geneset
        		ContinuousMapping<Integer,Double> continuousMapping_size_dataset2 = (ContinuousMapping<Integer,Double>)this.vmfFactoryContinuous.createVisualMappingFunction(prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, Integer.class, BasicVisualLexicon.NODE_BORDER_WIDTH);
                        
            Double min_line = 4.0;
            Double max_line = 15.0;
               // Create boundary conditions                  less than,   equals,  greater than
             BoundaryRangeValues<Double> bv0a = new BoundaryRangeValues<Double>(min_line, min_line, min_line);
             BoundaryRangeValues<Double> bv1a = new BoundaryRangeValues<Double>(max_line, max_line, max_line);
            continuousMapping_size_dataset2.addPoint(10, bv0a);
            continuousMapping_size_dataset2.addPoint(474, bv1a);
            
            vs.addVisualMappingFunction(continuousMapping_size_dataset2);

            //Continuous Mapping - set node line colour based on the sign of the ES score of second dataset
            //Color scale and mapper used by node colour and node line colour
            ContinuousMapping<Double,Paint> continuousMapping_width_col = (ContinuousMapping<Double,Paint>)this.vmfFactoryContinuous.createVisualMappingFunction(prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2, Double.class, BasicVisualLexicon.NODE_BORDER_PAINT);
            
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

            vs.addVisualMappingFunction(continuousMapping_width_col);

        }

        //Continuous Mapping - set node colour based on the sign of the ES score of first dataset

        ContinuousMapping<Double,Paint> continuousMapping = (ContinuousMapping<Double,Paint>)this.vmfFactoryContinuous.createVisualMappingFunction(prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1, Double.class, BasicVisualLexicon.NODE_FILL_COLOR);
        	         
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

        vs.addVisualMappingFunction(continuousMapping);
        
         //TODO: Add visual style geneset type support
        //if it is an EM geneset file and there are more than one Geneset type, map the types to shapes.
/*        if(.size() > 1){
            DiscreteMapping disMapping = new DiscreteMapping( NodeShape.ELLIPSE, ObjectMapping.NODE_MAPPING);
            disMapping.setControllingAttributeName(prefix + EnrichmentMapVisualStyle.GS_SOURCE);

            HashSet<String> genesetTypes = params.getGenesetTypes();
            String[] shapes = NodeShape.valuesAsString();
            //make sure we only use the number of shapes there are
            int count = 0;
            String previousShape = "none";
            for(Iterator i= genesetTypes.iterator(); i.hasNext(); ){
                String current_Set = (String) i.next();

                //if the previous shape is similar to this shape skip it
                if(shapes[count].contains(previousShape)){
                    count++;
                    //because there are multiple rectangle make sure the next shape isn't another one
                    //3D rectangle is actually mis-spelt so we need to hack this.
                    if(shapes[count].contains(previousShape.substring(0,4)))
                        count++;
                }


                if(count >= shapes.length)
                    count = 0;

                disMapping.putMapValue(current_Set,NodeShape.parseNodeShapeText(shapes[count]));
                previousShape = shapes[count];
                count++;

            }

            Calculator shapeCalculator = new BasicCalculator(prefix + "nodeshape", disMapping,VisualPropertyType.NODE_SHAPE);
            nodeAppCalc.setCalculator(shapeCalculator);

        }

*/      
       return vs;

    }
}
