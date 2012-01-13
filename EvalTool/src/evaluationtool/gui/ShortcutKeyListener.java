package evaluationtool.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import evaluationtool.intervaldata.DataSet;
import evaluationtool.intervaldata.IntervalData;

public class ShortcutKeyListener implements KeyListener{
	
	EvalGUI gui;
	
	public ShortcutKeyListener(EvalGUI gui) {
		this.gui = gui;
	}
	
	public void keyPressed(KeyEvent ke) {
		
		if(ke.getKeyCode() == 39){
			gui.skipFrame();
		}
		else if(ke.getKeyCode() == 32){
			gui.playpause();
		}
		
		if(ke.isControlDown() && ke.getKeyCode() >= 48 && ke.getKeyCode() <= 57){
			
			IntervalData data;
			int activity = 0;
			
			// 0 is activity 10
			if(ke.getKeyCode() == 48){
				activity = DataSet.NO_ACTIVITY;
			}
			else{
				activity = ke.getKeyCode() - 49;
			}
			
			// If an interval track is unlocked, add activity
			for(int i = 0; i < gui.getModel().getLoadedDataTracks().size(); i++){

				if(gui.getModel().getLoadedDataTracks().get(i) instanceof IntervalData){
					data = (IntervalData) gui.getModel().getLoadedDataTracks().get(i);
					if(!data.isLocked()){
						if(activity >= 0)
							data.addEventAtCurrentPosition(activity);
						else if(activity == DataSet.NO_ACTIVITY){
							data.deleteActivityAtCurrentPosition();
						}
					}
				}
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {}


	public void keyTyped(KeyEvent ke) {}

}
