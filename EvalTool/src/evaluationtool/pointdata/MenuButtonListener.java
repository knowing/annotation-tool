package evaluationtool.pointdata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuButtonListener implements ActionListener{

	SensorDataVisualization source;
	
	MenuButtonListener(SensorDataVisualization s){
		source = s;
	}
	
	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("Expanded view") || ae.getActionCommand().equals("Compact view")){
			source.toggleCompactView();
		}
		else if(ae.getActionCommand().equals("Remove")){
			source.remove();
		}
	}
}
