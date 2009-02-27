
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Iterator;
import java.awt.*;

import giny.model.Node;


/**
 * Created by
 * User: risserlin
 * Date: Feb 24, 2009
 * Time: 10:58:55 AM
 */
public class SliderBarPanel extends JPanel {

    private String sliderLabel;

    private double min;
    private double max;
    private NumberRangeModel rangeModel;

    public SliderBarPanel(double min, double max, String sliderLabel, EnrichmentMapParameters params,String attrib1, String attrib2, int desired_width) {
        this.min = min;
        this.max = max;
        this.sliderLabel = sliderLabel;
        initPanel(params, attrib1, attrib2,desired_width);
    }

    public void initPanel(EnrichmentMapParameters params,String attrib1, String attrib2, int desired_width){



        rangeModel = new NumberRangeModel(min, max, min, max);
        JRangeSliderExtended slider = new JRangeSliderExtended(rangeModel,JRangeSlider.HORIZONTAL,
                                                               JRangeSlider.LEFTRIGHT_TOPBOTTOM);

        slider.addChangeListener(new SliderBarActionListener(this,params, attrib1,attrib2));

        Dimension currentsize = slider.getPreferredSize();
        currentsize.width = desired_width;
        slider.setPreferredSize(currentsize);

        JLabel label = new JLabel(sliderLabel);

        this.setLayout(new GridLayout(2,1));

        this.add(label);

        this.add(slider);

        this.revalidate();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public NumberRangeModel getRangeModel() {
        return rangeModel;
    }

    public void setRangeModel(NumberRangeModel rangeModel) {
        this.rangeModel = rangeModel;
    }
}
