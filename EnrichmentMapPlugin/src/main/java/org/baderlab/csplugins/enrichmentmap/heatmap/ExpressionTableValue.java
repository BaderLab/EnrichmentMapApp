package org.baderlab.csplugins.enrichmentmap.heatmap;

import java.awt.Color;
/* object to represent both value and color of the expression values in the heatmap
 *  used so we can render both value and color in the heatmap.
 */
public class ExpressionTableValue {
	private Double expression_value;
	private Color expression_color;

	public ExpressionTableValue(Double expression_value, Color expression_color) {
		super();
		this.expression_value = expression_value;
		this.expression_color = expression_color;
	}
	public Double getExpression_value() {
		return expression_value;
	}
	public void setExpression_value(Double expression_value) {
		this.expression_value = expression_value;
	}
	public Color getExpression_color() {
		return expression_color;
	}
	public void setExpression_color(Color expression_color) {
		this.expression_color = expression_color;
	}
}
