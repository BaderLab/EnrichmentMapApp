package org.baderlab.csplugins.enrichmentmap.style;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Paint;
import java.util.Arrays;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Discrete;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Colors;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.StyleUpdateScope;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JukitoRunner.class)
public class ApplyStyleTest {
	
	private static final String prefix = "EM1_";

	
	public static class TestModule extends JukitoModule {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected void configureTest() {
			VisualProperty customPaint1 = mock(VisualProperty.class);
			VisualLexicon visualLexicon = mock(VisualLexicon.class);
			when(visualLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1")).thenReturn(customPaint1);
			bind(VisualLexicon.class).toInstance(visualLexicon);
			
			var renderingEngineManager = mock(RenderingEngineManager.class);
			when(renderingEngineManager.getDefaultVisualLexicon()).thenReturn(visualLexicon);
			bind(RenderingEngineManager.class).toInstance(renderingEngineManager);
			
			var cmFactory = mock(VisualMappingFunctionFactory.class);
			bind(VisualMappingFunctionFactory.class).annotatedWith(Continuous.class).toInstance(cmFactory);
			var dmFactory = mock(VisualMappingFunctionFactory.class);
			bind(VisualMappingFunctionFactory.class).annotatedWith(Discrete.class).toInstance(dmFactory);
			var pmFactory = mock(VisualMappingFunctionFactory.class);
			bind(VisualMappingFunctionFactory.class).annotatedWith(Passthrough.class).toInstance(pmFactory);
			
			EMCreationParameters params = mock(EMCreationParameters.class);
			when(params.getCreateDistinctEdges()).thenReturn(true);
			when(params.getAttributePrefix()).thenReturn(prefix);
			when(params.getSimilarityCutoff()).thenReturn(0.25);
			
			EMDataSet dataSet1 = mock(EMDataSet.class);
			when(dataSet1.getName()).thenReturn("DataSet1");
			when(dataSet1.getColor()).thenReturn(Color.BLUE);
			
			EMDataSet dataSet2 = mock(EMDataSet.class);
			when(dataSet2.getName()).thenReturn("DataSet2");
			when(dataSet2.getColor()).thenReturn(Color.RED);
			
			EnrichmentMap em = mock(EnrichmentMap.class);
			when(em.getParams()).thenReturn(params);
			when(em.getDataSetList()).thenReturn(Arrays.asList(dataSet1, dataSet2));
			when(em.getDataSetCount()).thenReturn(2);
			
			bind(EnrichmentMap.class).toInstance(em);
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void applyStyle_ALL(
			EMStyleBuilder styleBuilder,
			VisualLexicon visualLexicon,
			EnrichmentMap em,
			@Continuous VisualMappingFunctionFactory cmFactory, 
			@Discrete VisualMappingFunctionFactory dmFactory,
			@Passthrough  VisualMappingFunctionFactory pmFactory
	) {
		DiscreteMapping<String,Paint> edgeUnselectedPaintMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.EDGE_DATASET.with(prefix), String.class, EDGE_UNSELECTED_PAINT)).thenReturn(edgeUnselectedPaintMapping);
		DiscreteMapping<String,Paint> edgeStrokeUnselectedPaintMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.EDGE_DATASET.with(prefix), String.class, EDGE_STROKE_UNSELECTED_PAINT)).thenReturn(edgeStrokeUnselectedPaintMapping);
		
		DiscreteMapping<String,LineType> edgeLineTypeMaping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(CyEdge.INTERACTION, String.class, EDGE_LINE_TYPE)).thenReturn(edgeLineTypeMaping);
		
		ContinuousMapping<Double,Double> edgeWidthMapping = mock(ContinuousMapping.class);
		when(cmFactory.createVisualMappingFunction(Columns.EDGE_SIMILARITY_COEFF.with(prefix), Double.class, EDGE_WIDTH)).thenReturn(edgeWidthMapping);
		
		DiscreteMapping<String,NodeShape> nodeShapeMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix), String.class, NODE_SHAPE)).thenReturn(nodeShapeMapping);
		
		ContinuousMapping<Integer,Double> nodeSizeMapping = mock(ContinuousMapping.class);
		when(cmFactory.createVisualMappingFunction(Columns.NODE_GS_SIZE.with(prefix), Integer.class, NODE_SIZE)).thenReturn(nodeSizeMapping);
		
		DiscreteMapping<String,Paint> nodeColorMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix), String.class, NODE_FILL_COLOR)).thenReturn(nodeColorMapping);
		
		DiscreteMapping<String,Paint> nodeBorderPaintMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix), String.class, NODE_BORDER_PAINT)).thenReturn(nodeBorderPaintMapping);
		
		PassthroughMapping<String,String> nodeLabelMapping = mock(PassthroughMapping.class);
		when(pmFactory.createVisualMappingFunction(Columns.NODE_GS_DESCR.with(prefix), String.class, NODE_LABEL)).thenReturn(nodeLabelMapping);
		
		PassthroughMapping<String,String> nodeTooltipMapping = mock(PassthroughMapping.class);
		when(pmFactory.createVisualMappingFunction(Columns.NODE_GS_DESCR.with(prefix), String.class, NODE_TOOLTIP)).thenReturn(nodeTooltipMapping);
		
		
		// Create the style
		VisualStyle vs = mock(VisualStyle.class);
		CyNetworkView netView = mock(CyNetworkView.class);
		CyCustomGraphics2 chart = mock(CyCustomGraphics2.class);
		EMStyleOptions options = new EMStyleOptions(netView, em);
		
		styleBuilder.updateStyle(vs, options, chart, StyleUpdateScope.ALL);
		
		// setEdgeDefaults()
		verify(vs).setDefaultValue(EDGE_TRANSPARENCY, EMStyleBuilder.DEF_EDGE_TRANSPARENCY);
		verify(vs).setDefaultValue(EDGE_LABEL_TRANSPARENCY, EMStyleBuilder.DEF_EDGE_TRANSPARENCY);
		
		// setEdgePaint()
		verify(edgeUnselectedPaintMapping).putMapValue("DataSet1", Color.BLUE);
		verify(edgeUnselectedPaintMapping).putMapValue("DataSet2", Color.RED);
		verify(edgeStrokeUnselectedPaintMapping).putMapValue("DataSet1", Color.BLUE);
		verify(edgeStrokeUnselectedPaintMapping).putMapValue("DataSet2", Color.RED);
		
		// setEdgeLineType()
		verify(edgeLineTypeMaping).putMapValue(Columns.EDGE_DATASET_VALUE_COMPOUND, LineTypeVisualProperty.SOLID);
		verify(edgeLineTypeMaping).putMapValue(Columns.EDGE_INTERACTION_VALUE_SIG,  LineTypeVisualProperty.EQUAL_DASH);
		
		// setEdgeWidth()
		verify(edgeWidthMapping).addPoint(0.25, new BoundaryRangeValues<>(0.5, 1.0, 1.0));
		verify(edgeWidthMapping).addPoint(1.00, new BoundaryRangeValues<>(5.0, 5.0, 6.0));
		
		// setNodeShapes()
		verify(nodeShapeMapping).putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, NodeShapeVisualProperty.ELLIPSE);
		verify(nodeShapeMapping).putMapValue(Columns.NODE_GS_TYPE_SIGNATURE,  NodeShapeVisualProperty.DIAMOND);
		
		// setNodeSize()
		verify(nodeSizeMapping).addPoint(2,   new BoundaryRangeValues<>(20.0, 20.0, 20.0));
		verify(nodeSizeMapping).addPoint(500, new BoundaryRangeValues<>(60.0, 60.0, 60.0));
		
		// setNodeChart()
		VisualProperty customPaint1 = visualLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		verify(vs).setDefaultValue(customPaint1, chart);
		
		// setNodeColors()
		verify(nodeColorMapping).putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, Colors.DEF_NODE_COLOR);
		verify(nodeColorMapping).putMapValue(Columns.NODE_GS_TYPE_SIGNATURE,  Colors.SIG_NODE_COLOR);
		
		// setNodeDefaults()
		verify(vs).setDefaultValue(NODE_FILL_COLOR, Colors.DEF_NODE_COLOR);
		verify(vs).setDefaultValue(NODE_BORDER_PAINT, Colors.DEF_NODE_BORDER_COLOR);
		verify(vs).setDefaultValue(NODE_BORDER_WIDTH, EMStyleBuilder.DEF_NODE_BORDER_WIDTH);
		verify(vs).setDefaultValue(NODE_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		verify(vs).setDefaultValue(NODE_BORDER_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		verify(vs).setDefaultValue(NODE_LABEL_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		
		// setNodeBorderColors()
		verify(nodeBorderPaintMapping).putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, Colors.DEF_NODE_BORDER_COLOR);
		verify(nodeBorderPaintMapping).putMapValue(Columns.NODE_GS_TYPE_SIGNATURE,  Colors.SIG_NODE_BORDER_COLOR);
		
		// setNodeLabels()
		verify(vs).addVisualMappingFunction(nodeLabelMapping);
		
		// setNodeTooltip()
		verify(vs).addVisualMappingFunction(nodeTooltipMapping);
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void applyStyle_ONLY_EDGE_WIDTH(
			EMStyleBuilder styleBuilder,
			EnrichmentMap em,
			@Continuous VisualMappingFunctionFactory cmFactory
	) {
		ContinuousMapping<Double,Double> edgeWidthMapping = mock(ContinuousMapping.class);
		when(cmFactory.createVisualMappingFunction(Columns.EDGE_SIMILARITY_COEFF.with(prefix), Double.class, EDGE_WIDTH)).thenReturn(edgeWidthMapping);
		
		VisualStyle vs = mock(VisualStyle.class);
		CyNetworkView netView = mock(CyNetworkView.class);
		CyCustomGraphics2 chart = mock(CyCustomGraphics2.class);
		EMStyleOptions options = new EMStyleOptions(netView, em);
		
		// Create the style
		styleBuilder.updateStyle(vs, options, chart, StyleUpdateScope.ONLY_EDGE_WIDTH);

		// setEdgeWidth()
		verify(edgeWidthMapping).addPoint(0.25, new BoundaryRangeValues<>(0.5, 1.0, 1.0));
		verify(edgeWidthMapping).addPoint(1.00, new BoundaryRangeValues<>(5.0, 5.0, 6.0));
		
		// Verify the only mapping that is created is for edge width
		verify(vs, times(1)).addVisualMappingFunction(any());
		verify(vs, never()).setDefaultValue(any(), any());
		verify(vs).addVisualMappingFunction(edgeWidthMapping);
	}
	
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void applyStyle_ONLY_CHARTS(
			EMStyleBuilder styleBuilder,
			VisualLexicon visualLexicon,
			EnrichmentMap em,
			@Continuous VisualMappingFunctionFactory cmFactory, 
			@Discrete VisualMappingFunctionFactory dmFactory
	) {

		DiscreteMapping<String,NodeShape> nodeShapeMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix), String.class, NODE_SHAPE)).thenReturn(nodeShapeMapping);
		
		ContinuousMapping<Integer,Double> nodeSizeMapping = mock(ContinuousMapping.class);
		when(cmFactory.createVisualMappingFunction(Columns.NODE_GS_SIZE.with(prefix), Integer.class, NODE_SIZE)).thenReturn(nodeSizeMapping);
		
		// Create the style
		VisualStyle vs = mock(VisualStyle.class);
		CyNetworkView netView = mock(CyNetworkView.class);
		CyCustomGraphics2 chart = mock(CyCustomGraphics2.class);
		EMStyleOptions options = new EMStyleOptions(netView, em);
		
		styleBuilder.updateStyle(vs, options, chart, StyleUpdateScope.ONLY_CHARTS);
		
				
		// setNodeShapes()
		verify(nodeShapeMapping).putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, NodeShapeVisualProperty.ELLIPSE);
		verify(nodeShapeMapping).putMapValue(Columns.NODE_GS_TYPE_SIGNATURE,  NodeShapeVisualProperty.DIAMOND);
		
		// setNodeSize()
		verify(nodeSizeMapping).addPoint(2,   new BoundaryRangeValues<>(20.0, 20.0, 20.0));
		verify(nodeSizeMapping).addPoint(500, new BoundaryRangeValues<>(60.0, 60.0, 60.0));
		
		// setNodeChart()
		VisualProperty customPaint1 = visualLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		verify(vs).setDefaultValue(customPaint1, chart);
		
		// Verify that nothing else was updated
		verify(vs, times(2)).addVisualMappingFunction(any());
		verify(vs, times(3)).setDefaultValue(any(), any());
		verify(vs).addVisualMappingFunction(nodeShapeMapping);
		verify(vs).addVisualMappingFunction(nodeSizeMapping);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void applyStyle_ONLY_DATASETS(
			EMStyleBuilder styleBuilder,
			EnrichmentMap em,
			@Continuous VisualMappingFunctionFactory cmFactory, 
			@Discrete VisualMappingFunctionFactory dmFactory,
			@Passthrough  VisualMappingFunctionFactory pmFactory
	) {
		DiscreteMapping<String,Paint> edgeUnselectedPaintMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.EDGE_DATASET.with(prefix), String.class, EDGE_UNSELECTED_PAINT)).thenReturn(edgeUnselectedPaintMapping);
		DiscreteMapping<String,Paint> edgeStrokeUnselectedPaintMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.EDGE_DATASET.with(prefix), String.class, EDGE_STROKE_UNSELECTED_PAINT)).thenReturn(edgeStrokeUnselectedPaintMapping);
		
		DiscreteMapping<String,Paint> nodeColorMapping = mock(DiscreteMapping.class);
		when(dmFactory.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix), String.class, NODE_FILL_COLOR)).thenReturn(nodeColorMapping);
		
		// Create the style
		VisualStyle vs = mock(VisualStyle.class);
		CyNetworkView netView = mock(CyNetworkView.class);
		CyCustomGraphics2 chart = mock(CyCustomGraphics2.class);
		EMStyleOptions options = new EMStyleOptions(netView, em);
		
		styleBuilder.updateStyle(vs, options, chart, StyleUpdateScope.ONLY_DATASETS);
		
		// setEdgePaint()
		verify(edgeUnselectedPaintMapping).putMapValue("DataSet1", Color.BLUE);
		verify(edgeUnselectedPaintMapping).putMapValue("DataSet2", Color.RED);
		verify(edgeStrokeUnselectedPaintMapping).putMapValue("DataSet1", Color.BLUE);
		verify(edgeStrokeUnselectedPaintMapping).putMapValue("DataSet2", Color.RED);
		
		// setNodeColors()
		verify(nodeColorMapping).putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, Colors.DEF_NODE_COLOR);
		verify(nodeColorMapping).putMapValue(Columns.NODE_GS_TYPE_SIGNATURE,  Colors.SIG_NODE_COLOR);
		
		// Verify that nothing else was updated
		verify(vs, times(3)).addVisualMappingFunction(any());
		verify(vs).addVisualMappingFunction(edgeUnselectedPaintMapping);
		verify(vs).addVisualMappingFunction(edgeStrokeUnselectedPaintMapping);
		verify(vs).addVisualMappingFunction(nodeColorMapping);
	}
	

	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void applyStyle_PUBLICATION_READY_true(
			EMStyleBuilder styleBuilder,
			EnrichmentMap em
	) {
		VisualStyle vs = mock(VisualStyle.class);
		CyNetworkView netView = mock(CyNetworkView.class);
		CyCustomGraphics2 chart = mock(CyCustomGraphics2.class);
		
		// Set the publication ready flag
		boolean publicationReady = true;
		EMStyleOptions options = new EMStyleOptions(netView, em, em.getDataSetList(), null, false, publicationReady);
		styleBuilder.updateStyle(vs, options, chart, StyleUpdateScope.PUBLICATION_READY);
		
		verify(vs, never()).addVisualMappingFunction(any());
		verify(vs, times(1)).removeVisualMappingFunction(any());
		verify(vs, times(2)).setDefaultValue(any(), any());
		verify(vs).removeVisualMappingFunction(NODE_LABEL);
		verify(vs).setDefaultValue(NODE_LABEL, "");
		verify(vs).setDefaultValue(NETWORK_BACKGROUND_PAINT, Color.WHITE);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void applyStyle_PUBLICATION_READY_false(
			EMStyleBuilder styleBuilder,
			EnrichmentMap em,
			@Continuous VisualMappingFunctionFactory cmFactory, 
			@Discrete VisualMappingFunctionFactory dmFactory,
			@Passthrough  VisualMappingFunctionFactory pmFactory
	) {
		PassthroughMapping<String,String> nodeLabelMapping = mock(PassthroughMapping.class);
		when(pmFactory.createVisualMappingFunction(Columns.NODE_GS_DESCR.with(prefix), String.class, NODE_LABEL)).thenReturn(nodeLabelMapping);
		
		VisualStyle vs = mock(VisualStyle.class);
		CyNetworkView netView = mock(CyNetworkView.class);
		CyCustomGraphics2 chart = mock(CyCustomGraphics2.class);
		
		// Set the publication ready flag
		boolean publicationReady = false;
		EMStyleOptions options = new EMStyleOptions(netView, em, em.getDataSetList(), null, false, publicationReady);
		styleBuilder.updateStyle(vs, options, chart, StyleUpdateScope.PUBLICATION_READY);
		
		verify(vs, times(1)).addVisualMappingFunction(any());
		verify(vs, times(1)).setDefaultValue(any(), any());
		
		verify(vs).addVisualMappingFunction(nodeLabelMapping);
		verify(vs).setDefaultValue(NETWORK_BACKGROUND_PAINT, Color.WHITE);
	}
}

