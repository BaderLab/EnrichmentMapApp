
import cytoscape.visual.*;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.mappings.*;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;

import java.awt.*;

/**
 * Created by
 * User: risserlin
 * Date: Jan 26, 2009
 * Time: 3:23:52 PM
 */
public class EnrichmentMapVisualStyle {

    EnrichmentMapParameters params;

    public static final int maxNodeLabelLength = 25;

    public static String NAME = "Name";
    public static String FORMATTED_NAME = "Formatted_name";

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

    public static String OVERLAP_SIZE = "Overlap_size";
    public static String JACCARD_COEFFECIENT= "jaccard_coeffecient";

    private VisualStyle vs;

    public EnrichmentMapVisualStyle(String string, EnrichmentMapParameters params) {
        this.params = params;
        vs = new VisualStyle(string);
    }

    public VisualStyle createVisualStyle(CyNetwork network, String prefix){

        GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();
        globalAppCalc.setDefaultBackgroundColor(new Color(205,205,235));

        vs.setGlobalAppearanceCalculator(globalAppCalc);

        createEdgeAppearance(network, prefix);
        createNodeAppearance(network, prefix);

        return vs;
    }


    private void createEdgeAppearance(CyNetwork network, String prefix){
        EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();

        //set the default edge appearance
        EdgeAppearance edgeAppear = new EdgeAppearance();
        edgeAppear.set(VisualPropertyType.EDGE_COLOR, new Color(100,200,000) );


        //Continous Mapping - set edge line thickness based on the number of genes in the overlap
        ContinuousMapping continuousMapping_edgewidth = new ContinuousMapping(1, ObjectMapping.EDGE_MAPPING);
        continuousMapping_edgewidth.setControllingAttributeName(prefix + EnrichmentMapVisualStyle.JACCARD_COEFFECIENT, network, false);
        Interpolator numTonum2 = new LinearNumberToNumberInterpolator();
        continuousMapping_edgewidth.setInterpolator(numTonum2);

        Double under_width = 0.5;
        Double min_width = 1.0;
        Double max_width = 5.0;
        Double over_width = 6.0;

        // Create boundary conditions                  less than,   equals,  greater than
        BoundaryRangeValues bv4 = new BoundaryRangeValues(under_width, min_width, min_width);
        BoundaryRangeValues bv5 = new BoundaryRangeValues(max_width, max_width, over_width);
        continuousMapping_edgewidth.addPoint(params.getJaccardCutOff(), bv4);
        continuousMapping_edgewidth.addPoint(1.0, bv5);
        Calculator edgeWidthCalculator = new BasicCalculator(prefix + "edgesize", continuousMapping_edgewidth, VisualPropertyType.EDGE_LINE_WIDTH);
        edgeAppCalc.setCalculator(edgeWidthCalculator);

        vs.setEdgeAppearanceCalculator(edgeAppCalc);


    }


    private void createNodeAppearance(CyNetwork network,String prefix){

        Color max_red = new Color(255,0,0);
        Color light_red1 = new Color(255,102,102);
        Color light_red2 = new Color(255,179,179);
        Color max_blue = new Color(0,100,255);
        Color light_blue1 = new Color(102,162,255);
        Color light_blue2 = new Color(179,208,255);
        Color overColor = Color.WHITE;

        // Create boundary conditions                  less than,   equals,  greater than
        BoundaryRangeValues bv3a = new BoundaryRangeValues(max_blue,max_blue,max_blue);
        BoundaryRangeValues bv3b = new BoundaryRangeValues(light_blue2, light_blue2, max_blue);
        BoundaryRangeValues bv3c = new BoundaryRangeValues(light_blue1, light_blue1,light_blue2);
        BoundaryRangeValues bv3d = new BoundaryRangeValues(overColor, overColor, light_blue1);
        BoundaryRangeValues bv3e = new BoundaryRangeValues(overColor, overColor,overColor);
        BoundaryRangeValues bv3f = new BoundaryRangeValues(overColor, overColor, light_red2);
        BoundaryRangeValues bv3g = new BoundaryRangeValues(light_red2, light_red2, light_red1);
        BoundaryRangeValues bv3h = new BoundaryRangeValues(light_red1, light_red1, max_red);
        BoundaryRangeValues bv3i = new BoundaryRangeValues(max_red, max_red, max_red);


        NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();

        //set the default node appearance
        NodeAppearance nodeAppear = new NodeAppearance();
        nodeAppear.set(VisualPropertyType.NODE_FILL_COLOR, Color.orange);
        nodeAppear.set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);
        nodeAppear.set(VisualPropertyType.NODE_SIZE, new Double(35.0));
        nodeAppCalc.setDefaultAppearance(nodeAppear);

        // Passthrough Mapping - set node label
        PassThroughMapping pm = new PassThroughMapping(new String(), prefix + EnrichmentMapVisualStyle.FORMATTED_NAME);
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
       vs.setNodeAppearanceCalculator(nodeAppCalc);


    }

}
