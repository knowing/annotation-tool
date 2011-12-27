package evaluationtool;

import javax.swing.UIManager;

import evaluationtool.gui.EvalGUI;

public class EvalStarter {
	public static void main(String[] args) {
		// Set OS default components
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {/* Never mind */
		}

		// Windows just works with 32bit
		// Path root = Paths.get("C:","Users","muki","Development","target_platform","vlcj");
		// System.load(root.resolve("libvlccore.dll").toString());
		// System.load(root.resolve("libvlc.dll").toString());
		
		// Ubuntu 64 bit
		// System.load("/home/muki/Development/vlcj/libvlccore.so");
		// System.load("/home/muki/Development/vlcj/libvlc.so");
		DataModel model = new DataModel();
		EvalGUI gui = new EvalGUI(model);
		model.loadConfiguration();
	}
}
