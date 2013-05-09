package org.baderlab.csplugins.enrichmentmap.heatmap;


import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;

/*import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.DefaultFontMapper;
*/

import javax.swing.*;
import javax.swing.table.JTableHeader;


/**
 * Created by IntelliJ IDEA.
 * User: risserlin
 * Date: 11-05-30
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class HeatMapExporter {

public void export(final JPanel legendpanel, final JTable jtable1, final JTableHeader header,   final FileOutputStream stream)
			throws IOException {

		//final DingNetworkView dView = (DingNetworkView) view;
		//dView.setPrintingTextAsShape(!exportTextAsFont);

		//final InternalFrameComponent ifc = Cytoscape.getDesktop()
				//.getNetworkViewManager().getInternalFrameComponent(view);

/*        Rectangle pageSize;
        if(jtable1.getHeight() > PageSize.LETTER.getHeight())
		    pageSize = new Rectangle(PageSize.LETTER.getWidth(), jtable1.getHeight());
        else
            pageSize = new Rectangle(PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight());
		Document document = new Document(pageSize);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, stream);
			try {
				document.open();

				PdfContentByte cb = writer.getDirectContent();
                float width  = pageSize.getWidth();
                float height_header = 25;
                float height = pageSize.getHeight();


                //print the legend
                //PdfTemplate legend = cb.createTemplate(width,height_header);
				//Graphics2D g = legend.createGraphics(width, height_header);
				//double imageScale = width / ((double) legendpanel.getWidth());
				//g.scale(imageScale, imageScale);
                //legendpanel.paint(g);
				//g.dispose();
                //cb.addTemplate(legend, 0, (pageSize.getHeight()-25) );

                double imageScale2 = Math.min(width / ((double) jtable1.getWidth()),
                        height / ((double) jtable1.getHeight()));

                //create another object for the Table
                PdfTemplate expres = cb.createTemplate(width,height);
                Graphics2D g2 = cb.createGraphics(width, height);
                //double imageScale2 = width / ((double) jtable1.getWidth());
				g2.scale(imageScale2, imageScale2);
                jtable1.paint(g2);
				g2.dispose();
                cb.addTemplate(expres, 0,0);

                 //create another object for the Table header
                PdfTemplate headertemp = cb.createTemplate(width,height_header);
                Graphics2D g3 = cb.createGraphics(width, height_header);
				g3.scale(imageScale2, imageScale2);
                header.paint(g3);
				g3.dispose();
                cb.addTemplate(headertemp, 0,25 );





			} finally {
				if (document != null) {
					document.close();
				}
				if (writer != null) {
					writer.close();
				}
			}
		} catch (DocumentException exp) {
			throw new IOException(exp.getMessage());
		}*/
	}


}
