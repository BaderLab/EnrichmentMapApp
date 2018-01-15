// $Id$
//------------------------------------------------------------------------------
/** Copyright (c) 2008 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/


// imports

package org.baderlab.csplugins.org.mskcc.colorgradient;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * Class which renders data type color gradient.
 *
 * @author Benjamin Gross
 */
@SuppressWarnings("serial")
public class ColorGradientWidget extends JPanel {

	/**
	 * Legend Position Enumeration.
	 */
	public static enum LEGEND_POSITION {

		// data types
		TOP("Top"),
		RIGHT("Right"),
		BOTTOM("Bottom"),
		LEFT("Left"),
		NA("NA");

		// string ref for readable name
		private String type;

		// constructor
		LEGEND_POSITION(String type) { this.type = type; }

		// method to get enum readable name
		public String toString() { return type; }
	}

	// some statics
	private static final int POSITION_LEGEND_WIDTH = 15;
	private static final int HSPACER = 5;
	private static final int VSPACER = 3;


	// other required refs
	private Image img;
	private String title;
	private String cookedTitle;
	private Dimension cookedTitleDimension;
	private Color borderColor;
	private ColorGradientTheme colorGradientTheme;
	private ColorGradientRange colorGradientRange;
	private Rectangle minimumConditionGradientRectangle;
	private Rectangle centerConditionGradientRectangle;
	private Rectangle maximumConditionGradientRectangle;
	private final boolean isLegend;
	private final LEGEND_POSITION legendPosition;
	private final boolean renderPositionLegend;
	private Dimension minimumConditionValueDimension;
	private Dimension averageValueDimension;
	private Dimension maximumConditionValueDimension;
	private String minimumConditionValueString;
	private String averageValueString;
	private String maximumConditionValueString;
	private int maxStringHeight;
	private int gradientHeight;
	private int gradientWidth;
	private int positionLegendWidth;

	public static ColorGradientWidget getInstance(String title, ColorGradientTheme colorGradientTheme,
			ColorGradientRange colorGradientRange, boolean isLegend, LEGEND_POSITION legendPosition) {
		return new ColorGradientWidget(title, colorGradientTheme, colorGradientRange, isLegend, legendPosition);
	}

	/**
	 * Our implementation of Component setBounds().  If we don't do this, the
	 * individual canvas do not get rendered.
	 */
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);

		if ((width > 0) && (height > 0))
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Gets colorGradientTheme loaded.
	 *
	 * @return ColorGradientTheme
	 */
	public ColorGradientTheme getColorGradientTheme() {
		return colorGradientTheme;
	}

	/**
	 * Gets colorGradientRange loaded.
	 *
	 * @return ColorGradientRange
	 */
	public ColorGradientRange getColorGradientRange() {
		return colorGradientRange;
	}


//	/**
//	 * Let layout manager know our minimum size.
//	 *
//	 * @return Dimension
//	 */ 
//    public Dimension getMinimumSize() {
//		if (minimumConditionValueDimension != null) {
//			int legendWidth = (isLegend && legendPosition != LEGEND_POSITION.NA) ? POSITION_LEGEND_WIDTH + HSPACER : 0;
//			int minWidth = (horizontalMargin * 2 +
//							legendWidth +
//							(int)minimumConditionValueDimension.getWidth() + HSPACER +
//							(int)averageValueDimension.getWidth() + HSPACER +
//							(int)maximumConditionValueDimension.getWidth());
//			return new Dimension(minWidth, (variableHeight) ? 0 : widgetHeight);
//							
//		}
//		else {
//			return new Dimension((variableWidth) ? 0 : widgetWidth, (variableHeight) ? 0 : widgetHeight);
//		}
//    }
//
//	/**
//	 * Let layout manager know our preferred size.
//	 *
//	 * @return Dimension
//	 */ 
//    public Dimension getPreferredSize() {
//        return getMinimumSize();
//    }

	/**
	 * Method to export/print color gradient widget.
	 *
	 * @param g Graphics
	 * @param renderStrings boolean
	 */
	public void export(Graphics g, boolean renderStrings) {
		// paint bg white
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		renderComponent(g, renderStrings);
	}

	@Override
    public void paintComponent(Graphics g) {
		renderComponent(g, true);
	}

	/**
	 * Gets condition value strings as list, size 3:
	 *
	 * List(0): minimum
	 * List(1): average
	 * List(2): max
	 *
	 * @return List<String>
	 */
	 public List<String> getConditionValueStrings() {
		 // list to return
		 List<String> toReturn = new ArrayList<>();

		 toReturn.add(minimumConditionValueString);
		 toReturn.add(averageValueString);
		 toReturn.add(maximumConditionValueString);

		 return toReturn;
	 }

	private ColorGradientWidget(String title, ColorGradientTheme colorGradientTheme,
			ColorGradientRange colorGradientRange, boolean isLegend, LEGEND_POSITION legendPosition) {
		// init member vars
		this.title = title;
		this.borderColor = UIManager.getColor("Label.foreground");
		this.colorGradientTheme = colorGradientTheme;
		this.colorGradientRange = colorGradientRange;
		this.isLegend = isLegend;
		this.legendPosition = legendPosition;
		this.renderPositionLegend = !(legendPosition == LEGEND_POSITION.NA);

		// setup mouse listener
		if (!isLegend) attachMouseListener();

		// set condition value strings
		setConditionValueStrings();
		setOpaque(false);
	}

    /**
     * Attaches a Mouse Listener, used for Rollovers.
     */
    private void attachMouseListener() {
        this.addMouseListener(new MouseAdapter() {

            @Override
			public void mousePressed(MouseEvent e) {
				//mondrianConfiguration.setColorTheme(colorGradientTheme);
			}

            @Override
            public void mouseEntered(MouseEvent e) {
				borderColor = Color.GREEN;
				repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
				borderColor = UIManager.getColor("Label.foreground");
				repaint();
            }
        });
    }
	
	/**
	 * Sets max string height
	 */
	private void setMaxStringHeight() {
		// calc max string height
		maxStringHeight = (int)Math.max(minimumConditionValueDimension.getHeight(),
										(int)Math.max(averageValueDimension.getHeight(), maximumConditionValueDimension.getHeight()));
		maxStringHeight = (int)Math.max(maxStringHeight, cookedTitleDimension.getHeight());
	}

	/**
	 * Sets position legend width.
	 */
	private void setPositionLegendDimensions() {
		if (renderPositionLegend) {
			positionLegendWidth = 15 + HSPACER;
		} else {
			positionLegendWidth = 0;
		}
	}

	/**
	 * Set cooked title string.
	 *
	 * @param g2d Graphics2D
	 */
	private void setCookedTitleString(Graphics2D g2d) {
		if (title != null) {
			// setup some vars used below
			int width = getSize().width;
			width -=  positionLegendWidth;
			FontMetrics fontMetrics = g2d.getFontMetrics();
	
			String tmpStr = "";
			for (int lc = 0; lc <= title.length(); lc++) {
				tmpStr = title.substring(0, lc);
				
				if (fontMetrics.stringWidth(tmpStr) <= width) {
					cookedTitle = tmpStr;
				} else {
					// we've gone over, replace last 3 chars with "."
					cookedTitle = tmpStr.substring(0, lc - 4);
					cookedTitle += "...";
					break;
				}
			}
		} else {
			cookedTitle = null;
		}
	}

	private void setCookedTitleStringDimension(Graphics2D g2d) {
		// Rectangle reference
		if (cookedTitle != null) {
			// get graphics context font metrics
			FontMetrics fontMetrics = g2d.getFontMetrics();
			
			// min value string dimensions
			Rectangle2D rect = fontMetrics.getStringBounds(cookedTitle, g2d);
			cookedTitleDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());
		} else {
			cookedTitleDimension = new Dimension();
		}
	}

	private void setConditionValueStrings() {
		// we only want 2 significant digits
		final NumberFormat formatter = new DecimalFormat("#######.##");
		minimumConditionValueString = formatter.format(colorGradientRange.getMinValue());
		averageValueString = formatter.format(colorGradientRange.getCenterLowValue() + (colorGradientRange.getCenterHighValue() - colorGradientRange.getCenterLowValue()) / 2);
		maximumConditionValueString = formatter.format(colorGradientRange.getMaxValue());
	}

	/**
	 * Sets the min and max condition value strings dimensions.
	 */
	private void setConditionValueStringDimensions(Graphics2D g2d) {
		// Rectangle reference
		Rectangle2D rect;

		// get graphics context font metrics
		FontMetrics fontMetrics = g2d.getFontMetrics();
		
		// min value string dimensions
		rect = fontMetrics.getStringBounds(minimumConditionValueString, g2d);
		minimumConditionValueDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());

		// average value dimension
		rect = fontMetrics.getStringBounds(averageValueString, g2d);
		averageValueDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());

		// max value string dimensions
		rect = fontMetrics.getStringBounds(maximumConditionValueString, g2d);
		maximumConditionValueDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());
	}

	/**
	 * Computes min, max, legend rectangles
	 */
	private void computeGradientRectangles() {
		// get widget dimension
		int widgetWidth = getSize().width;
		int widgetHeight = getSize().height;
		gradientHeight = widgetHeight;
		gradientWidth = widgetWidth;

		// minimum rectangle
		minimumConditionGradientRectangle = new Rectangle(0, 0, widgetWidth / 2, gradientHeight);
		// maximum rectangle
		maximumConditionGradientRectangle = new Rectangle(widgetWidth / 2, 0, widgetWidth / 20, gradientHeight);
	}

	/**
	 * Computes min, center, max, legend rectangles
	 */
	private void computeLegendGradientRectangles() {
		// get widget dimension
		int widgetWidth = getSize().width;
		int widgetHeight = getSize().height;

		// set gradient height
		gradientWidth = widgetWidth - positionLegendWidth;
		gradientHeight = widgetHeight - maxStringHeight - VSPACER;
		if (cookedTitle != null && cookedTitle.length() > 0) {
			gradientHeight -= maxStringHeight + VSPACER;
		}

		// set rectangle y
		int rectYPos = VSPACER;
		if (cookedTitle != null && cookedTitle.length() > 0) {
			rectYPos += maxStringHeight + VSPACER;
		}

		// compute normals
		double centerLowNormal = (colorGradientRange.getCenterLowValue() - colorGradientRange.getMinValue()) / (colorGradientRange.getMaxValue() - colorGradientRange.getMinValue());
		double centerHighNormal = (colorGradientRange.getCenterHighValue() - colorGradientRange.getMinValue()) / (colorGradientRange.getMaxValue() - colorGradientRange.getMinValue());

		// minimum to center low rectangle
		int minRectXPos = positionLegendWidth;
		int minRectWidth = (int)(centerLowNormal * gradientWidth);
		minimumConditionGradientRectangle = new Rectangle(minRectXPos,
														  rectYPos,
														  minRectWidth,
														  gradientHeight);

		// center low to center high rectangle
		int centerRectXPos = minRectXPos + minRectWidth;
		int centerRectWidth = (int)(centerHighNormal * gradientWidth - minRectWidth);
		centerConditionGradientRectangle = new Rectangle(centerRectXPos,
														 rectYPos, 
														 centerRectWidth,
														 gradientHeight);

		// center high to maximum rectangle
		int maxRectXPos = centerRectXPos + centerRectWidth;
		maximumConditionGradientRectangle = new Rectangle(maxRectXPos,
														  rectYPos,
														  gradientWidth - centerRectWidth - minRectWidth,
														  gradientHeight);
	}

	/**
	 * This is where we render the gradient.
	 */
    private void renderComponent(Graphics g, boolean renderStrings) {
		if (img != null) {
			// set our graphics context
			Graphics2D g2d = ((BufferedImage) img).createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// clear background
			clearImage(g2d);

			// save font
			Font savedFont = g2d.getFont();

			// set new font
			g2d.setFont(UIManager.getFont("Label.font").deriveFont(LookAndFeelUtil.getSmallFontSize()));

			if (isLegend) {
				setPositionLegendDimensions(); // should come first
				setCookedTitleString(g2d);
				setCookedTitleStringDimension(g2d);
				setConditionValueStringDimensions(g2d);
				setMaxStringHeight();
				computeLegendGradientRectangles();
				
				if (renderPositionLegend)
					renderPositionLegend(g2d);
				
				renderLegendGradient(g2d);
				
				if (renderStrings) {
					renderCookedTitleString(g2d);
					renderConditionValueStrings(g2d);
				}
			} else {
				computeGradientRectangles();
				renderGradient(g2d);
			}

			// restore font
			g2d.setFont(savedFont);

			// render image
			g.drawImage(img, 0, 0, null);
			
			g2d.dispose();
		}
    }

	private void renderPositionLegend(Graphics2D g2d) {
		final int renderWidth = positionLegendWidth-HSPACER;
		final int renderHeight = renderWidth;

		final int xPos = 0;
		int gradientCenter = VSPACER;
		if (cookedTitle != null && cookedTitle.length() > 0) {
			gradientCenter += maxStringHeight + VSPACER;
		}
		gradientCenter += gradientHeight / 2;
		final int yPos = gradientCenter - renderHeight / 2;

		// set num data types
		/*final DataTypeMatrixManager dataTypeMatrixManager = DataTypeMatrixManager.getInstance();
		final java.util.Vector<String> dataTypes = dataTypeMatrixManager.getLoadedDataTypes(false);

		// set index
		final int index = (dataTypes.size() == 2) ? ((legendPosition == LEGEND_POSITION.LEFT) ? 0 : 1) : legendPosition.ordinal();

		// render background
		g2d.setPaint(Color.WHITE);
		g2d.fillRect(xPos, yPos, renderWidth-1, renderHeight-1);
		// render triangle
		g2d.setPaint(Color.GRAY);
		final Shape triangle =
			((HeatmapWidget.HeatmapWidgetCellRenderer)heatmapWidget.getDefaultRenderer(Color.class)).getShape(dataTypes.size(),
																											  index, xPos, yPos,
																											  renderWidth, renderHeight);
		g2d.fill(triangle);
		// render border
		g2d.setPaint(Color.BLACK);
		final Rectangle rect = new Rectangle(xPos, yPos, renderWidth-1, renderHeight-1);
		g2d.draw(rect);
        */
	}

	private void renderGradient(Graphics2D g2d) {
		// create the gradient from min to center
		GradientPaint gradientLow =
			new GradientPaint((float) minimumConditionGradientRectangle.getX(),
							  (float) minimumConditionGradientRectangle.getY(),
							  colorGradientTheme.getMinColor(),
							  (float) maximumConditionGradientRectangle.getX(),
							  (float) minimumConditionGradientRectangle.getY(),
							  colorGradientTheme.getCenterColor());
		
		g2d.setPaint(gradientLow);
		g2d.fillRect((int) minimumConditionGradientRectangle.getX(),
					 (int) minimumConditionGradientRectangle.getY(),
					 (int) minimumConditionGradientRectangle.getWidth(),
					 (int) minimumConditionGradientRectangle.getHeight());

		// create the gradient from center to max
		final GradientPaint gradientHigh = new GradientPaint((float) maximumConditionGradientRectangle.getX(),
															 (float) maximumConditionGradientRectangle.getY(),
															 colorGradientTheme.getCenterColor(),
															 (float) (maximumConditionGradientRectangle.getX()
																	  + maximumConditionGradientRectangle.getWidth()),
															 (float) maximumConditionGradientRectangle.getY(),
															 colorGradientTheme.getMaxColor());
		g2d.setPaint(gradientHigh);
		g2d.fillRect((int) maximumConditionGradientRectangle.getX(),
					 (int) maximumConditionGradientRectangle.getY(),
					 (int) maximumConditionGradientRectangle.getWidth(),
					 (int) maximumConditionGradientRectangle.getHeight());


		// draw outline around gradient
		Rectangle rect = new Rectangle(0, 0, gradientWidth - 1, gradientHeight - 1);
		g2d.setPaint(borderColor);
		g2d.draw(rect);
	}

	private void renderLegendGradient(Graphics2D g2d) {
		// minimum gradient
		final GradientPaint gradientLow = new GradientPaint((float)minimumConditionGradientRectangle.getX(),
															(float)minimumConditionGradientRectangle.getY(),
															colorGradientTheme.getMinColor(),
															(float)centerConditionGradientRectangle.getX(),
															(float)centerConditionGradientRectangle.getY(),
															colorGradientTheme.getCenterColor());
		
		g2d.setPaint(gradientLow);
		g2d.fillRect((int) minimumConditionGradientRectangle.getX(),
					 (int) minimumConditionGradientRectangle.getY(),
					 (int) minimumConditionGradientRectangle.getWidth(),
					 (int) minimumConditionGradientRectangle.getHeight());

		// center gradient
		g2d.setPaint(colorGradientTheme.getCenterColor());
		g2d.fillRect((int) centerConditionGradientRectangle.getX(),
					 (int) centerConditionGradientRectangle.getY(),
					 (int) centerConditionGradientRectangle.getWidth(),
					 (int) centerConditionGradientRectangle.getHeight());

		// max gradient
		final GradientPaint gradientHigh = new GradientPaint((float)centerConditionGradientRectangle.getX(),
															 (float)centerConditionGradientRectangle.getY(),
															 colorGradientTheme.getCenterColor(),
															 (float)(maximumConditionGradientRectangle.getX() + maximumConditionGradientRectangle.getWidth()),
															 (float)maximumConditionGradientRectangle.getY(),
															 colorGradientTheme.getMaxColor());
		g2d.setPaint(gradientHigh);
		g2d.fillRect((int) maximumConditionGradientRectangle.getX(),
					 (int) maximumConditionGradientRectangle.getY(),
					 (int) maximumConditionGradientRectangle.getWidth(),
					 (int) maximumConditionGradientRectangle.getHeight());


		// draw outline around gradient - use any rectangle for starting y
		final Rectangle rect = new Rectangle(positionLegendWidth,
											 (int)minimumConditionGradientRectangle.getY(),
											 gradientWidth - 1,
											 gradientHeight - 1);
		g2d.setPaint(borderColor);
		g2d.draw(rect);
	}

	private void renderCookedTitleString(Graphics2D g2d) {
		if (cookedTitle == null)
			return;
		
		// set the paint
		g2d.setPaint(UIManager.getColor("Label.foreground"));

		// compute drawstring x pos - centered over gradient
		int startingPos = positionLegendWidth;
		int width = getSize().width;
		width -=  positionLegendWidth;
		int xPos = startingPos + width / 2 - (int)cookedTitleDimension.getWidth() / 2;

		// compute drawstring y pos
		int yPos = maxStringHeight;

		// render min string - above gradient
		g2d.drawString(cookedTitle, xPos, yPos);
	}

	private void renderConditionValueStrings(Graphics2D g2d) {
		// set the paint
		g2d.setPaint(UIManager.getColor("Label.foreground"));

		// compute drawstring y pos - we can use the height of any gradient rectangle
		int yPos = VSPACER + gradientHeight + maxStringHeight;
		
		if (cookedTitle != null && cookedTitle.length() > 0)
			yPos += VSPACER + maxStringHeight;

		// render min string - above gradient
		g2d.drawString(minimumConditionValueString, positionLegendWidth, yPos);

		// render center low string - below gradient
		g2d.drawString(averageValueString,
					   (positionLegendWidth + gradientWidth / 2) - (int)averageValueDimension.getWidth() / 2, yPos);

		// render max string - above gradient
		g2d.drawString(maximumConditionValueString,
				getSize().width - (int)maximumConditionValueDimension.getWidth(), yPos);
	}

	/**
	 * Utility function to clean the background of the image.
	 */
	private void clearImage(Graphics2D image2D) {
		// set the alpha composite on the image, and clear its area
		Composite origComposite = image2D.getComposite();
		image2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		image2D.setComposite(origComposite);
	}
}
