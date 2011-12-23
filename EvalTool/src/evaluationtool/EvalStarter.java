package evaluationtool;

import javax.swing.UIManager;
import evaluationtool.gui.*;

public class EvalStarter {
	public static void main(String[] args){
		// Set OS default components
	    try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} 
	    catch (Exception e) {/* Never mind */}
	    
	    System.load("/home/muki/Development/vlcj/libvlccore.so");
	    System.load("/home/muki/Development/vlcj/libvlc.so");
		EvalGUI gui = new EvalGUI(new DataModel());
	}
}
