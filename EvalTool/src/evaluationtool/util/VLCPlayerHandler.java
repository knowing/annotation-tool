package evaluationtool.util;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.jna.NativeLibrary;

import evaluationtool.DataModel;

public class VLCPlayerHandler {
	/**
	  * Sets VLC path. 
	  * Needed files are (Windows):
	  * - libvlc.dll
	  * - libvlccore.dll
	  * - /plugins
	  */
	public static void initLibVlc(DataModel model, JFrame gui){
	  
	  JFileChooser chooser = new JFileChooser();
	  chooser.setDialogTitle("Please select a directory with VLC 1.2 or higher");
	  
	  // We need a directory
	  chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	  
	  // If ok has been clicked, load the file
	  if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){	 
		  String path = chooser.getSelectedFile().getAbsolutePath();
		  
		  if(!model.setVLCPath(path)){
			  JOptionPane.showMessageDialog(gui, "This program does not work without VLC. Quitting.", "Error", JOptionPane.ERROR_MESSAGE);	
			   System.exit(0);
		  }
	  }  
	  else{
		   JOptionPane.showMessageDialog(gui, "This program does not work without VLC. Quitting.", "Error", JOptionPane.ERROR_MESSAGE);	
		   System.exit(0);
	  }
 }
}
