package evaluationtool.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class VideoFrameListener implements KeyListener{

	EvalGUI gui;
	
	VideoFrameListener(EvalGUI gui){
		this.gui = gui;
	}

	public void keyPressed(KeyEvent arg0) {}

	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent ke) {
		
		System.out.println("Key: " + ke.getKeyChar() + ", " + ke.getKeyCode());
		
		if(ke.getKeyChar() == 'f'){
			System.out.println("Forward one frame");
			gui.skipFrame();
		}

	}
}
