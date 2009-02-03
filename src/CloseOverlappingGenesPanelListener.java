/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 11:04:18 AM
 */
import java.awt.event.*;
public class CloseOverlappingGenesPanelListener implements ActionListener {
    OverlappingGenesPanel result;
    /** Creates a new instance of CloseResultPanelListener */
    public CloseOverlappingGenesPanelListener(OverlappingGenesPanel result) {
        this.result=result;

    }
    public void actionPerformed(ActionEvent e){

        result.parentFrame.removeTab(result);

    }

}
