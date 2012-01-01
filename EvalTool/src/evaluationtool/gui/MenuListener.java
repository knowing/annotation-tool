package evaluationtool.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;

import evaluationtool.projecthandling.ProjectFileHandler;

public class MenuListener implements ActionListener {

	EvalGUI gui;
	
	MenuListener(EvalGUI gui){
		this.gui = gui;
	}
	public void actionPerformed(ActionEvent ae) {
		if(ae.getActionCommand().equals("openfile")){
			/**
			  * Show a JFileChooser and load the selected file
			  */
				  JFileChooser chooser = new JFileChooser();

				  // If ok has been clicked, load the file
				  if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){	 
					  String path = chooser.getSelectedFile().getAbsolutePath();
					  gui.getModel().loadFile(path);
				  }
			  }
		else if(ae.getActionCommand().equals("createtrack")){
			gui.getModel().addIntervalTrack();
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
		else if(ae.getActionCommand().equals("mute")){
			gui.mute();
		}
		else if(ae.getActionCommand().equals("save")){
			// If there is no project path yet, ask for one
			if(gui.getModel().getProjectPath().equals("")){
				String projectpath = ProjectFileHandler.showSaveDialog(gui);	
				gui.getModel().setProjectPath(projectpath);
			}
			else{
				ProjectFileHandler.saveCurrentProject(gui);
			}
		}
		else if(ae.getActionCommand().equals("saveas")){
			String projectpath = ProjectFileHandler.showSaveDialog(gui);	
			gui.getModel().setProjectPath(projectpath);
		}
	}
	
}
