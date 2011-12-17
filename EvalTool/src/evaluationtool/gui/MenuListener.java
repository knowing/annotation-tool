package evaluationtool.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuListener implements ActionListener {

	EvalGUI gui;
	
	MenuListener(EvalGUI gui){
		this.gui = gui;
	}
	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("openfile")){
			gui.loadFile();
		}
		else if(ae.getActionCommand().equals("exit")){
			System.exit(0);
		}
		else if(ae.getActionCommand().equals("playpause")){
			gui.playpause();
		}
		else if(ae.getActionCommand().equals("stop")){
			gui.stop();
		}
		else if(ae.getActionCommand().equals("skipframe")){
			gui.skipFrame();
		}
	}
	
}
