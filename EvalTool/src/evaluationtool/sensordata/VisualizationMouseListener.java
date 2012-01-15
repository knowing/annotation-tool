package evaluationtool.sensordata;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;

import evaluationtool.gui.TrackMouseListener;
import evaluationtool.gui.TrackVisualization;
import evaluationtool.gui.Visualization;
import evaluationtool.sensordata.SensorTrackVisualization;

/**
 * Allows the manipulation of tracks with the mouse
 * @author anfi
 *
 */
public class VisualizationMouseListener extends TrackMouseListener implements MouseWheelListener, MouseListener, MouseMotionListener{
	
	public VisualizationMouseListener(Visualization s, TrackVisualization tv) {
		super(s, tv);
	}

	boolean shiftingTime = false;
	int tempMouseX = 0;
	int tempMouseY = 0;

	/**
	 * Toggles between compact and expanded view on MMB
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		
		if(e.getButton() == MouseEvent.BUTTON2){
				((SensorTrackVisualization)track).toggleCompactView();
		}
	}
}
