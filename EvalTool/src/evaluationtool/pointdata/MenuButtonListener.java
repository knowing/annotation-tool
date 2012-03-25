package evaluationtool.pointdata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;


public class MenuButtonListener implements ActionListener{

	PointDataVisualization source;
	
	MenuButtonListener(PointDataVisualization s){
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
		else if(ae.getActionCommand().equals("changename")){
			String newname = JOptionPane.showInputDialog(source, "Rename track, include file extension", source.getName());
			if(newname.equals("")){
				JOptionPane.showMessageDialog(source, "Name must not be empty", "Error", JOptionPane.ERROR_MESSAGE);
			}
			else{
				source.rename(newname);
			}
		}
	}
}
