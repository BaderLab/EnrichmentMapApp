
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Iterator;
import java.util.Hashtable;
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

    private int min;
    private int max;
    private NumberRangeModel rangeModel;

    private double precision = 1000.0;

    private JLabel label;

    public SliderBarPanel(double min, double max, String sliderLabel, EnrichmentMapParameters params,String attrib1, String attrib2, int desired_width) {
        if((min <= 1) && (max <= 1)){
            this.min = (int)(min*precision);
            this.max = (int)(max*precision);
        }
        else{
           this.min = (int)min;
           this.max = (int)max;
        }
        this.sliderLabel = sliderLabel;

        label = new JLabel(sliderLabel);
        initPanel(params, attrib1, attrib2,desired_width);
    }

    public void initPanel(EnrichmentMapParameters params,String attrib1, String attrib2, int desired_width){



        //rangeModel = new NumberRangeModel(min, max, min, max);
        //JRangeSliderExtended slider = new JRangeSliderExtended(rangeModel,JRangeSlider.HORIZONTAL,
        //                                                       JRangeSlider.LEFTRIGHT_TOPBOTTOM);

        JSlider slider = new JSlider(JSlider.HORIZONTAL,
                                      min, max, max);


        slider.addChangeListener(new SliderBarActionListener(this,params, attrib1,attrib2));

        slider.setMajorTickSpacing((max-min)/5);
        slider.setPaintTicks(true);

        //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer( min ), new JLabel(""+ min/precision));
        labelTable.put( new Integer( max ), new JLabel("" + max/precision));
        slider.setLabelTable( labelTable );

        slider.setPaintLabels(true);


        Dimension currentsize = slider.getPreferredSize();
        currentsize.width = desired_width;
        slider.setPreferredSize(currentsize);

        //JLabel label = new JLabel(sliderLabel);

        this.setLayout(new GridLayout(2,1));

        this.add(label);

        this.add(slider);

        this.revalidate();
    }

    public void setLabel(int current_value) {
        label.setText(sliderLabel + " --> " + current_value/precision);

        this.revalidate();
    }

    public double getPrecision() {
        return precision;
    }

    public double getMin() {
        return min/precision;
    }

    public void setMin(double min) {
        this.min = (int)(min * precision);
    }

    public double getMax() {
        return max/precision;
    }

    public void setMax(double max) {
        this.max = (int) (max*precision);
    }

    public NumberRangeModel getRangeModel() {
        return rangeModel;
    }

    public void setRangeModel(NumberRangeModel rangeModel) {
        this.rangeModel = rangeModel;
    }
}
