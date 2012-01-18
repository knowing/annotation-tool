package evaluationtool.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import evaluationtool.intervaldata.Activity;
import evaluationtool.intervaldata.IntervalData;
import evaluationtool.pointdata.PointData;

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
			
			int activity = 0;
			
			// 0 is activity 10
			if(ke.getKeyCode() == 48){
				activity = Activity.NO_ACTIVITY;
			}
			else{
				activity = ke.getKeyCode() - 49;
			}
			
			// If an interval track is unlocked, add activity, for an unlocked point track, add point
			for(int i = 0; i < gui.getModel().getLoadedDataTracks().size(); i++){

				if(gui.getModel().getLoadedDataTracks().get(i) instanceof IntervalData){
					IntervalData data = (IntervalData) gui.getModel().getLoadedDataTracks().get(i);
					if(!data.isLocked()){
						System.out.println("Changing activity");
						if(activity >= 0)
							data.toggleEventAtCurrentPosition(activity);
						else if(activity == Activity.NO_ACTIVITY){
							data.deleteActivityAtCurrentPosition();
						}
					}
				}
				else if(gui.getModel().getLoadedDataTracks().get(i) instanceof PointData){
					PointData data = (PointData) gui.getModel().getLoadedDataTracks().get(i);
					if(!data.isLocked()){
							System.out.println("Adding point");
							data.addPointAtCurrentPosition();
					}
				}
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {}


	public void keyTyped(KeyEvent ke) {}

}
