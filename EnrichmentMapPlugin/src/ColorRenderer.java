

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by
 * User: risserlin
 * Date: Feb 2, 2009
 * Time: 9:50:34 AM
 */
public class ColorRenderer extends JLabel
                           implements TableCellRenderer {

    public ColorRenderer() {
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object color,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {

        Color newColor = (Color) color;

        setBackground(newColor);
        return this;
    }
}
