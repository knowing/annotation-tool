package evaluationtool.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ShortcutKeyListener implements KeyListener{
	
	EvalGUI gui;
	
	public ShortcutKeyListener(EvalGUI gui) {
		this.gui = gui;
	}
	
	public void keyPressed(KeyEvent ke) {
		System.out.println(ke.getKeyCode());
		
		if(ke.getKeyCode() == 39){
			gui.skipFrame();
		}
		else if(ke.getKeyCode() == 32){
			gui.playpause();
		}
	}

	public void keyReleased(KeyEvent arg0) {}


	public void keyTyped(KeyEvent ke) {}

}
