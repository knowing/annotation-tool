package evaluationtool.intervaldata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MenuButtonListener implements ActionListener{

	IntervalDataVisualization source;
	
	MenuButtonListener(IntervalDataVisualization s){
		source = s;
	}
	
	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("togglelocked")){
			source.toggleLocked();
		}
		else if(ae.getActionCommand().equals("Remove")){
			source.remove();
		}
		else if(ae.getActionCommand().equals("options")){
			source.showOptionsDialog();
		}
	}
}
